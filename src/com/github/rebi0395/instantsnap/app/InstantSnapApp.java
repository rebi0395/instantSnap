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
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.repository.PropertiesRepository;
import com.github.rebi0395.instantsnap.service.SettingsFileWatcher;
import com.github.rebi0395.instantsnap.view.swing.presenter.MainWindowPresenter;

/**
 * InstantSnap常駐プロセスの起動、画面と各サービスの結線、終了処理を管理します。
 * 撮影などの機能はCoordinatorへ、Swing画面の操作はMainWindowPresenterへ委譲します。
 */
public final class InstantSnapApp {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(InstantSnapApp.class);

	/** 二重起動防止用ロックの管理クラス */
	private final SingleInstanceManager instanceManager;

	/** 撮影処理とキー監視を管理するCoordinator */
	private final InstantSnapCoordinator coordinator;

	/** Swingメインウィンドウの表示と更新を管理するサービス */
	private final MainWindowPresenter mainWindowPresenter;

	/** 設定ファイルの変更を監視するサービス */
	private final SettingsFileWatcher settingsFileWatcher;

	/** 起動中のJavaFX設定プロセス */
	private volatile Process settingsProcess;

	/** 終了処理の二重実行を防ぐフラグ */
	private volatile boolean shuttingDown;

	/**
	 * アプリケーションを起動します
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
	 */
	private InstantSnapApp(SingleInstanceManager instanceManager) throws IOException {
		this.instanceManager = instanceManager;
		this.coordinator = new InstantSnapCoordinator();

		ConfigBean config = coordinator.getConfigBean();
		this.mainWindowPresenter = new MainWindowPresenter(
				config,
				coordinator.getMessageKeyConverter(),
				languageType -> changeLanguage(languageType),
				() -> openSettings(),
				() -> coordinator.requestSnap(),
				() -> coordinator.requestRangeSelection(),
				() -> requestExit());
		this.coordinator.setExitRequestHandler(() -> requestExit());

		try {
			// 設定画面と手動編集のどちらも、設定ファイルの更新として同じ経路で検知する。
			this.settingsFileWatcher = new SettingsFileWatcher(
					PROPERTIES_PATH,
					() -> requestSettingsReload());
		} catch (IOException e) {
			mainWindowPresenter.close();
			coordinator.exit();
			throw e;
		}
	}

	/** Swingメインウィンドウを表示します。 */
	private void show() {
		mainWindowPresenter.show();
	}

	/** JavaFX設定画面を別JVMプロセスとして起動します。 */
	private void openSettings() {
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

	/** JavaFX設定プロセスを起動するコマンドを作成します。 */
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

	/** JavaFX設定プロセスの終了を監視し、メインウィンドウの操作を再開します。 */
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

	/** 設定ファイルの変更通知をSwingイベントスレッドへ渡します。 */
	private void requestSettingsReload() {
		if (shuttingDown) {
			return;
		}
		SwingUtilities.invokeLater(() -> reloadSettings());
	}

	/** 設定ファイルを再読込し、常駐処理とメインウィンドウへ反映します。 */
	private void reloadSettings() {
		if (shuttingDown) {
			return;
		}
		if (coordinator.reloadConfig()) {
			mainWindowPresenter.refresh(coordinator.getConfigBean());
		}
	}

	/** ミニウィンドウで選択された言語を保存し、表示へ即時反映します。 */
	private void changeLanguage(LanguageType languageType) {
		if (shuttingDown) {
			return;
		}
		if (!coordinator.updateLanguage(languageType)) {
			Toolkit.getDefaultToolkit().beep();
		}
		mainWindowPresenter.refresh(coordinator.getConfigBean());
	}

	/** Coordinatorやウィンドウからの終了要求をSwingイベントスレッドへ集約します。 */
	private void requestExit() {
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

	/** 常駐リソース、子プロセス、二重起動防止ロックを順番に解放します。 */
	private void shutdown() {
		shuttingDown = true;
		mainWindowPresenter.setActionsEnabled(false);

		Process currentProcess = settingsProcess;
		if (currentProcess != null && currentProcess.isAlive()) {
			currentProcess.destroy();
		}

		mainWindowPresenter.close();
		settingsFileWatcher.close();
		coordinator.exit();
		instanceManager.close();
		System.exit(0);
	}

	/** OS標準のSwingデザインを適用します。 */
	private static void applySystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			log.warn("OS標準のLook & Feelを適用できなかったため、Swing標準表示を使用します", e);
		}
	}

	/** 二重起動時の案内を表示します。 */
	private static void showSingleInstanceMessage(MessageKeyConverter startupConverter) {
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(
				null,
				startupConverter.getMessage(MessageKeyConstant.DIALOG_SINGLE_INSTANCE_MESSAGE),
				startupConverter.getMessage(MessageKeyConstant.DIALOG_SINGLE_INSTANCE_TITLE),
				JOptionPane.INFORMATION_MESSAGE);
	}

	/** 起動処理に失敗した場合の案内を表示します。 */
	private static void showStartupError(MessageKeyConverter startupConverter) {
		Toolkit.getDefaultToolkit().beep();
		JOptionPane.showMessageDialog(
				null,
				startupConverter.getMessage(MessageKeyConstant.DIALOG_STARTUP_ERROR),
				startupConverter.getMessage(MessageKeyConstant.DIALOG_SINGLE_INSTANCE_TITLE),
				JOptionPane.ERROR_MESSAGE);
	}
}
