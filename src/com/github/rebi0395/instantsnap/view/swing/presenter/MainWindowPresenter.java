package com.github.rebi0395.instantsnap.view.swing.presenter;

import static com.github.rebi0395.instantsnap.common.AppConstant.ICON_APP;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.app.InstantSnapApp;
import com.github.rebi0395.instantsnap.app.InstantSnapCoordinator;
import com.github.rebi0395.instantsnap.common.MessageKeyConstant;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.model.enums.SnapAreaType;
import com.github.rebi0395.instantsnap.model.enums.SnapRouteType;
import com.github.rebi0395.instantsnap.service.ScreenCaptureExclusionService;
import com.github.rebi0395.instantsnap.service.SettingsFileWatcher.SettingsChangeListener;
import com.github.rebi0395.instantsnap.service.TriggerKeyWatcher.ExitKeyEventListener;
import com.github.rebi0395.instantsnap.view.swing.panel.MainWindowPanel;

/**
 * Swingメインウィンドウの生成、表示、表示内容の更新を行うプレゼンターです。
 * 利用者の操作を受け取り、実務処理をInstantSnapCoordinatorへ振り分けます。
 * 生成・表示・更新はSwingイベントディスパッチスレッド（EDT）上で行います。
 */
public final class MainWindowPresenter implements ExitKeyEventListener, SettingsChangeListener {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(MainWindowPresenter.class);

	/** Swingメインウィンドウのタイトル */
	private static final String WINDOW_TITLE = "InstantSnap";

	/** 設定画面の起動とアプリ固有の終了処理を呼び出すクラス */
	private final InstantSnapApp app;

	/** 撮影、設定反映、実務サービスの終了を振り分けるクラス */
	private final InstantSnapCoordinator coordinator;

	/** Swingメインウィンドウ */
	private final JFrame frame;

	/** メインウィンドウに表示するパネル */
	private final MainWindowPanel panel;

	/** 言語別メッセージを取得するクラス */
	private final MessageKeyConverter converter;

	/** メインウィンドウの撮影対象への表示・非表示を切り替えるサービス */
	private final ScreenCaptureExclusionService screenCaptureExclusionService;

	/** InstantSnapのウィンドウを撮影画像へ含める場合true */
	private boolean includeSettingWindow;

	/** 終了確認と終了処理の二重実行を防ぐフラグ */
	private volatile boolean exitInProgress;

	/**
	 * 現在の設定値を使ってSwingメインウィンドウを生成します。
	 *
	 * @param app 設定画面の起動とアプリ固有の終了処理を管理するクラス
	 * @param coordinator 撮影や設定反映を振り分けるクラス
	 */
	public MainWindowPresenter(
			InstantSnapApp app,
			InstantSnapCoordinator coordinator) {
		this.app = app;
		this.coordinator = coordinator;
		ConfigBean config = coordinator.getConfigBean();
		this.converter = coordinator.getMessageKeyConverter();
		this.screenCaptureExclusionService = new ScreenCaptureExclusionService();
		this.includeSettingWindow = config.getIsIncludeSettingWindow();
		this.panel = new MainWindowPanel(config, this);
		this.frame = createFrame();
	}

	/**
	 * メインウィンドウを表示します。
	 */
	public void show() {
		frame.setVisible(true);
		applyScreenCaptureSetting();
	}

	/**
	 * 設定ボタンからJavaFX設定画面の起動をAppへ要求します。
	 */
	public void openSettings() {
		app.openSettings();
	}

	/**
	 * 撮影ボタンから通常撮影をCoordinatorへ要求します。
	 */
	public void requestSnap() {
		coordinator.requestSnap(SnapRouteType.MAIN_BUTTON);
	}

	/**
	 * 範囲選択ボタンから撮影範囲の選択をCoordinatorへ要求します。
	 */
	public void requestRangeSelection() {
		coordinator.requestRangeSelection();
	}

	/**
	 * ミニウィンドウで選択された言語をCoordinatorで保存し、表示へ反映します。
	 *
	 * @param languageType 選択された言語
	 */
	public void changeLanguage(LanguageType languageType) {
		if (!coordinator.updateLanguage(languageType)) {
			Toolkit.getDefaultToolkit().beep();
		}
		refresh(coordinator.getConfigBean());
	}

	/**
	 * ミニウィンドウで選択された撮影範囲をCoordinatorで保存し、表示へ反映します。
	 *
	 * @param snapAreaType 選択された撮影範囲
	 */
	public void changeSnapArea(SnapAreaType snapAreaType) {
		if (!coordinator.updateSnapArea(snapAreaType)) {
			Toolkit.getDefaultToolkit().beep();
		}
		refresh(coordinator.getConfigBean());
	}

	/**
	 * 現在設定されている保存先フォルダをCoordinatorへ要求します。
	 */
	public void openSaveDirectory() {
		coordinator.openSaveDirectory();
	}

	/**
	 * ミニウィンドウで変更された通知表示設定をCoordinatorで保存し、表示へ反映します。
	 *
	 * @param enabled 撮影後の通知表示を有効にする場合true
	 */
	public void changeNotificationEnabled(boolean enabled) {
		if (!coordinator.updateNotificationEnabled(enabled)) {
			Toolkit.getDefaultToolkit().beep();
		}
		refresh(coordinator.getConfigBean());
	}

	/**
	 * 設定ファイル監視スレッドからの通知をEDTへ引き継ぎます。
	 * 終了処理中の更新通知は無視します。
	 */
	@Override
	public void onSettingsFileChanged() {
		if (exitInProgress) {
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				reloadSettings();
			}
		});
	}

	/**
	 * 設定ファイルを再読込し、最新の設定をメインウィンドウへ反映します。
	 */
	private void reloadSettings() {
		if (exitInProgress) {
			return;
		}
		if (coordinator.reloadConfig()) {
			refresh(coordinator.getConfigBean());
		}
	}

	/**
	 * 終了要求をEDTへ集約して確認画面を表示します。
	 * 承諾後はCoordinatorの実務サービスを先に止め、最後にApp固有の終了処理を呼び出します。
	 */
	@Override
	public void requestExit() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					requestExit();
				}
			});
			return;
		}
		if (exitInProgress) {
			return;
		}

		exitInProgress = true;
		Toolkit.getDefaultToolkit().beep();
		if (!confirmExit()) {
			exitInProgress = false;
			return;
		}

		setActionsEnabled(false);
		coordinator.requestExit();
		app.shutdownApplication();
	}

	/**
	 * 撮影範囲を現在の表示言語へ変換します。
	 *
	 * @param snapAreaType 表示する撮影範囲
	 * @return 現在の言語に対応した表示名
	 */
	public String resolveSnapAreaDisplayName(SnapAreaType snapAreaType) {
		return converter.getMessage(snapAreaType);
	}

	/**
	 * 現在の設定値をメインウィンドウの全表示へ反映します。
	 *
	 * @param config 再読込または言語変更後の最新の設定値
	 */
	public void refresh(ConfigBean config) {
		panel.refresh(config);
		this.includeSettingWindow = config.getIsIncludeSettingWindow();
		applyScreenCaptureSetting();
	}

	/**
	 * JavaFX設定画面の起動状態に応じて設定操作を有効・無効にします。
	 *
	 * @param available 設定画面を新しく開ける場合true。起動中はfalse
	 */
	public void setSettingsAvailable(boolean available) {
		panel.setSettingsEnabled(available);
	}

	/**
	 * メインウィンドウにある全操作を有効・無効にします。
	 *
	 * @param enabled 操作を許可する場合true。終了処理中はfalse
	 */
	public void setActionsEnabled(boolean enabled) {
		panel.setActionsEnabled(enabled);
	}

	/**
	 * アプリを終了するか確認します。
	 *
	 * @return 「はい」が選択された場合true。「いいえ」やダイアログを閉じた場合false
	 */
	private boolean confirmExit() {
		String message = converter.getMessage(
				MessageKeyConstant.DIALOG_EXIT_CONFIRM_MESSAGE,
				"Exit InstantSnap?");
		String title = converter.getMessage(
				MessageKeyConstant.DIALOG_EXIT_TITLE,
				"Exit confirmation");

		int result = JOptionPane.showConfirmDialog(
				frame,
				message,
				title,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
		return result == JOptionPane.YES_OPTION;
	}

	/**
	 * JavaFX設定画面を起動できなかった場合の案内を表示します。
	 *
	 * @param language 案内の表示言語。日本語以外は英語を使用
	 */
	public void showSettingsStartError(LanguageType language) {
		String message;
		if (language == LanguageType.JAPANESE) {
			message = "設定画面を起動できませんでした。ログを確認してください。";
		} else {
			message = "Could not open Settings. Check the log for details.";
		}
		JOptionPane.showMessageDialog(frame, message, WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * メインウィンドウを閉じて画面リソースを解放します。
	 */
	public void close() {
		frame.setVisible(false);
		frame.dispose();
	}

	/**
	 * 所有するパネルを配置し、終了要求をこのPresenterで受け取るウィンドウを生成します。
	 *
	 * @return 表示前のメインウィンドウ
	 */
	private JFrame createFrame() {
		JFrame mainFrame = new JFrame(WINDOW_TITLE);
		mainFrame.setName("InstantSnapMainWindow");
		// 閉じる操作だけでは破棄せず、Presenterで終了確認を行う。
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.setContentPane(panel);
		mainFrame.setResizable(false);
		mainFrame.setAlwaysOnTop(true);
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		setApplicationIcon(mainFrame);

		mainFrame.addWindowListener(new WindowAdapter() {
			/**
			 * ウィンドウの閉じる操作をPresenterの終了確認へ渡します。
			 *
			 * @param event 閉じる操作のイベント
			 */
			@Override
			public void windowClosing(WindowEvent event) {
				requestExit();
			}
		});
		return mainFrame;
	}

	/**
	 * 最新の設定値に従って、メインウィンドウを撮影対象へ含めるか切り替えます。
	 * ウィンドウを隠さずに切り替えるため、撮影時のちらつきやフォーカス移動は発生しません。
	 */
	private void applyScreenCaptureSetting() {
		boolean excluded = !includeSettingWindow;
		screenCaptureExclusionService.setExcluded(frame, excluded);
	}

	/**
	 * メインウィンドウへアプリアイコンを設定します。
	 * 読込に失敗した場合はログを残し、標準アイコンで起動を継続します。
	 *
	 * @param targetFrame アイコンの設定先
	 */
	private void setApplicationIcon(JFrame targetFrame) {
		try (InputStream iconStream = getClass().getResourceAsStream(ICON_APP)) {
			if (iconStream == null) {
				log.warn("アプリケーションアイコンが見つかりません: {}", ICON_APP);
				return;
			}
			Image image = ImageIO.read(iconStream);
			if (image != null) {
				targetFrame.setIconImage(image);
			}
		} catch (IOException e) {
			log.warn("アプリケーションアイコンの読込に失敗しました", e);
		}
	}

}
