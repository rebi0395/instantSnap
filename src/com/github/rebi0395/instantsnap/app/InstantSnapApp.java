package com.github.rebi0395.instantsnap.app;

import static com.github.rebi0395.instantsnap.common.AppConstant.PROPERTIES_PATH;

import java.awt.Toolkit;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.DpiUtil;
import com.github.rebi0395.instantsnap.common.MessageKeyConstant;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.common.OsUtil;
import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.repository.PropertiesRepository;
import com.github.rebi0395.instantsnap.service.SettingsFileWatcher;
import com.github.rebi0395.instantsnap.service.SettingsFileWatcher.SettingsChangeListener;
import com.github.rebi0395.instantsnap.service.TriggerKeyWatcher;
import com.github.rebi0395.instantsnap.service.TriggerKeyWatcher.KeyEventListener;
import com.github.rebi0395.instantsnap.view.swing.presenter.MainWindowPresenter;

/**
 * InstantSnap常駐プロセスの起動、画面と各サービスの結線、終了処理を管理します。
 * 撮影などの機能はCoordinatorへ、Swing画面の操作はMainWindowPresenterへ委譲します。
 * JavaFX設定画面は子プロセスで起動し、両プロセスは設定ファイルを介して変更を共有します。
 */
public final class InstantSnapApp implements KeyEventListener, SettingsChangeListener {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(InstantSnapApp.class);

	/** 二重起動防止用ロックの管理クラス */
	private final SingleInstanceManager instanceManager;

	/** 撮影処理を管理するCoordinator */
	private final InstantSnapCoordinator coordinator;

	/** 撮影、範囲選択、終了のショートカットキーを監視するクラス */
	private final TriggerKeyWatcher triggerKeyWatcher;

	/** Swingメインウィンドウの表示と更新を管理するプレゼンター */
	private final MainWindowPresenter mainWindowPresenter;

	/** 設定ファイルの変更を監視するサービス */
	private final SettingsFileWatcher settingsFileWatcher;

	/** 起動中のJavaFX設定プロセス */
	private volatile Process settingsProcess;

	/** 終了処理の二重実行を防ぐフラグ */
	private volatile boolean shuttingDown;

	/**
	 * 二重起動ロックを取得し、EDT上で常駐処理とメインウィンドウを起動します。
	 * ロックを取得できない場合は案内を表示し、起動を中止します。
	 */
	public static void launch() {
		LanguageType language = PropertiesRepository.readLanguageOnly();
		MessageKeyConverter startupConverter = new MessageKeyConverter(language);
		SingleInstanceManager instanceManager = new SingleInstanceManager();

		try {
			if (!instanceManager.tryLock()) {
				showSingleInstanceMessage(startupConverter);
				return;
			}

			DpiUtil.enablePerMonitorV2();
			applySystemLookAndFeel();

			// Swingコンポーネントの生成と表示はイベントディスパッチスレッドで行う。
			SwingUtilities.invokeLater(() -> {
				try {
					InstantSnapApp app = new InstantSnapApp(instanceManager);
					app.show();
				} catch (Exception e) {
					log.error("InstantSnapの起動に失敗しました", e);
					instanceManager.close();
					showStartupError(startupConverter);
				}
			});
		} catch (Exception e) {
			log.error("InstantSnapの起動に失敗しました", e);
			instanceManager.close();
			showStartupError(startupConverter);
		}
	}

	/**
	 * 常駐処理、Swingメインウィンドウ、設定ファイル監視を生成して結線します。
	 *
	 * @param instanceManager 起動時にロックを取得済みの管理クラス
	 * @throws IOException 設定ファイルの監視を開始できない場合
	 * @throws ServiceException トリガーキー監視を開始できない場合
	 */
	private InstantSnapApp(SingleInstanceManager instanceManager) throws IOException, ServiceException {
		this.instanceManager = instanceManager;
		this.coordinator = new InstantSnapCoordinator();

		ConfigBean config = coordinator.getConfigBean();
		this.mainWindowPresenter = new MainWindowPresenter(
				this,
				config,
				coordinator.getMessageKeyConverter());

		SettingsFileWatcher createdSettingsFileWatcher = null;
		TriggerKeyWatcher createdTriggerKeyWatcher = new TriggerKeyWatcher(config, this);
		try {
			// 設定画面と手動編集のどちらも、設定ファイルの更新として同じ経路で検知する。
			createdSettingsFileWatcher = new SettingsFileWatcher(
					PROPERTIES_PATH,
					this);
			// キーイベントはAppで受け取り、撮影要求だけをCoordinatorへ委譲する。
			createdTriggerKeyWatcher.activate();
		} catch (IOException | ServiceException e) {
			// 起動途中で失敗した場合も、ここまでに開始した監視と画面を解放する。
			if (createdSettingsFileWatcher != null) {
				createdSettingsFileWatcher.close();
			}
			stopTriggerKeyWatcher(createdTriggerKeyWatcher);
			mainWindowPresenter.close();
			coordinator.exit();
			throw e;
		}
		this.settingsFileWatcher = createdSettingsFileWatcher;
		this.triggerKeyWatcher = createdTriggerKeyWatcher;
	}

	/**
	 * Swingメインウィンドウを表示します。
	 */
	private void show() {
		mainWindowPresenter.show();
	}

	/**
	 * EDT上でJavaFX設定画面を別JVMプロセスとして起動します。
	 * 既に起動中の場合は音で知らせ、二つ目の設定画面は開きません。
	 * 起動に失敗した場合は操作を再び有効にし、案内を表示します。
	 */
	public void openSettings() {
		Process currentProcess = settingsProcess;
		if (currentProcess != null && currentProcess.isAlive()) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		mainWindowPresenter.setSettingsAvailable(false);
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(createSettingsCommand());
			// 設定ファイルを常駐側と同じ場所から読み書きするため、作業ディレクトリを引き継ぐ。
			processBuilder.directory(Path.of("").toAbsolutePath().toFile());
			// 子プロセスの出力は更新通知に使用しないため、バッファへ蓄積させず破棄する。
			processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
			processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);
			settingsProcess = processBuilder.start();
			watchSettingsProcess(settingsProcess);
		} catch (IOException e) {
			log.error("JavaFX設定画面の起動に失敗しました", e);
			settingsProcess = null;
			mainWindowPresenter.setSettingsAvailable(true);
			mainWindowPresenter.showSettingsStartError(coordinator.getConfigBean().getLanguageType());
		}
	}

	/**
	 * 常駐側と同じJavaランタイムとクラスパスで設定画面を起動するコマンドを作成します。
	 *
	 * @return 実行ファイルと引数を要素ごとに分けたProcessBuilder用の配列
	 * @throws IOException 使用中のJavaランタイム内に実行ファイルが存在しない場合
	 */
	private String[] createSettingsCommand() throws IOException {
		String executableName = "java";
		if (OsUtil.isWindows()) {
			executableName = "javaw.exe";
		}
		Path javaExecutable = Path.of(System.getProperty("java.home"), "bin", executableName);
		if (!Files.isRegularFile(javaExecutable)) {
			throw new IOException("Java実行ファイルが見つかりません: " + javaExecutable);
		}

		return new String[] {
				javaExecutable.toString(),
				"-cp",
				System.getProperty("java.class.path"),
				SettingsLauncher.class.getName()
		};
	}

	/**
	 * 子プロセスの終了を別スレッドで待ち、設定操作を再び有効にします。
	 * 設定値の反映はファイル監視が担当するため、ここでは再読込を行いません。
	 *
	 * @param process 起動済みのJavaFX設定プロセス
	 */
	private void watchSettingsProcess(Process process) {
		Thread watcher = new Thread(() -> {
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.warn("設定プロセスの終了待機が中断されました", e);
			} finally {
				settingsProcess = null;
				if (!shuttingDown) {
					SwingUtilities.invokeLater(() -> mainWindowPresenter.setSettingsAvailable(true));
				}
			}
		}, "instantsnap-settings-process-watcher");
		watcher.setDaemon(true);
		watcher.start();
	}

	/**
	 * ファイル監視スレッドからの変更通知を受け取り、EDTへ再読込を予約します。
	 * 終了処理中の通知は無視します。
	 */
	@Override
	public void onSettingsFileChanged() {
		if (shuttingDown) {
			return;
		}
		SwingUtilities.invokeLater(() -> reloadSettings());
	}

	/**
	 * 設定ファイルを再読込し、常駐処理とメインウィンドウへ反映します。
	 */
	private void reloadSettings() {
		if (shuttingDown) {
			return;
		}
		if (coordinator.reloadConfig()) {
			mainWindowPresenter.refresh(coordinator.getConfigBean());
		}
	}

	/**
	 * EDT上で選択言語を保存し、表示へ即時反映します。
	 * 保存に失敗した場合は音で知らせ、Coordinatorが戻した元の設定で表示を更新します。
	 *
	 * @param languageType ミニウィンドウで選択された言語
	 */
	public void changeLanguage(LanguageType languageType) {
		if (shuttingDown) {
			return;
		}
		if (!coordinator.updateLanguage(languageType)) {
			Toolkit.getDefaultToolkit().beep();
		}
		mainWindowPresenter.refresh(coordinator.getConfigBean());
	}

	/**
	 * 撮影要求をCoordinatorへ委譲します。
	 */
	@Override
	public void requestSnap() {
		if (!shuttingDown) {
			coordinator.requestSnap();
		}
	}

	/**
	 * 範囲選択要求をCoordinatorへ委譲します。
	 */
	@Override
	public void requestRangeSelection() {
		if (!shuttingDown) {
			coordinator.requestRangeSelection();
		}
	}

	/**
	 * 終了要求をSwingイベントスレッドへ集約し、終了確認画面を表示します。
	 */
	@Override
	public void requestExit() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> requestExit());
			return;
		}
		if (shuttingDown) {
			return;
		}

		if (mainWindowPresenter.confirmExit()) {
			shutdown();
		}
	}

	/**
	 * 終了確認で承諾された後、EDT上で常駐プロセスを終了します。
	 * 子プロセス、監視、撮影用リソース、二重起動ロックを順に解放します。
	 */
	private void shutdown() {
		// 新しい操作や設定の再読込を受け付けない状態にしてから解放する。
		shuttingDown = true;
		mainWindowPresenter.setActionsEnabled(false);

		Process currentProcess = settingsProcess;
		if (currentProcess != null && currentProcess.isAlive()) {
			currentProcess.destroy();
		}

		mainWindowPresenter.close();
		settingsFileWatcher.close();
		stopTriggerKeyWatcher(triggerKeyWatcher);
		coordinator.exit();
		instanceManager.close();
		System.exit(0);
	}

	/**
	 * トリガーキー監視を停止し、停止失敗時も残りの終了処理を継続します。
	 *
	 * @param targetWatcher 停止するキー監視。開始前または停止済みの場合は何もしない
	 */
	private static void stopTriggerKeyWatcher(TriggerKeyWatcher targetWatcher) {
		try {
			if (targetWatcher.isKeyListenerRunning()) {
				targetWatcher.deactivate();
			}
		} catch (ServiceException e) {
			log.error("トリガーキー監視の終了に失敗しました", e);
		}
	}

	/**
	 * OS標準のSwingデザインを適用します。
	 */
	private static void applySystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			log.warn("OS標準のLook & Feelを適用できなかったため、Swing標準表示を使用します", e);
		}
	}

	/**
	 * 二重起動時の案内を表示します。
	 *
	 * @param startupConverter 起動前に取得した言語で案内を表示する変換クラス
	 */
	private static void showSingleInstanceMessage(MessageKeyConverter startupConverter) {
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(
				null,
				startupConverter.getMessage(MessageKeyConstant.DIALOG_SINGLE_INSTANCE_MESSAGE),
				startupConverter.getMessage(MessageKeyConstant.DIALOG_SINGLE_INSTANCE_TITLE),
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * 起動処理に失敗した場合の案内を表示します。
	 *
	 * @param startupConverter 起動前に取得した言語で案内を表示する変換クラス
	 */
	private static void showStartupError(MessageKeyConverter startupConverter) {
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(
				null,
				startupConverter.getMessage(MessageKeyConstant.DIALOG_STARTUP_ERROR),
				startupConverter.getMessage(MessageKeyConstant.DIALOG_SINGLE_INSTANCE_TITLE),
				JOptionPane.ERROR_MESSAGE);
	}
}
