package com.github.rebi0395.instantsnap.view.swing.presenter;

import static com.github.rebi0395.instantsnap.common.AppConstant.ICON_APP;

import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.app.InstantSnapApp;
import com.github.rebi0395.instantsnap.common.MessageKeyConstant;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.service.ScreenCaptureExclusionService;
import com.github.rebi0395.instantsnap.view.swing.panel.MainWindowPanel;

/**
 * Swingメインウィンドウの生成、表示、表示内容の更新を行うプレゼンターです。
 * 利用者の操作を受け取り、InstantSnapAppの対応する処理を呼び出します。
 * 生成・表示・更新はSwingイベントディスパッチスレッド（EDT）上で行います。
 */
public final class MainWindowPresenter {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(MainWindowPresenter.class);

	/** Swingメインウィンドウのタイトル */
	private static final String WINDOW_TITLE = "InstantSnap";

	/** メインウィンドウから要求されたアプリ処理の呼び出し先 */
	private final InstantSnapApp app;

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

	/**
	 * 現在の設定値を使ってSwingメインウィンドウを生成します。
	 *
	 * @param app メインウィンドウからアプリ処理を呼び出すクラス
	 * @param config 現在の設定値
	 * @param converter 言語別メッセージの取得クラス
	 */
	public MainWindowPresenter(
			InstantSnapApp app,
			ConfigBean config,
			MessageKeyConverter converter) {
		this.app = app;
		this.converter = converter;
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
	 * 撮影ボタンから通常撮影をAppへ要求します。
	 */
	public void requestSnap() {
		app.requestSnapFromMainWindow();
	}

	/**
	 * 範囲選択ボタンから撮影範囲の選択をAppへ要求します。
	 */
	public void requestRangeSelection() {
		app.requestRangeSelection();
	}

	/**
	 * ミニウィンドウで選択された言語の保存と反映をAppへ要求します。
	 *
	 * @param languageType 選択された言語
	 */
	public void changeLanguage(LanguageType languageType) {
		app.changeLanguage(languageType);
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
	public boolean confirmExit() {
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
	 * 所有するパネルを配置し、終了要求をAppへ渡すウィンドウを生成します。
	 *
	 * @return 表示前のメインウィンドウ
	 */
	private JFrame createFrame() {
		JFrame mainFrame = new JFrame(WINDOW_TITLE);
		mainFrame.setName("InstantSnapMainWindow");
		// 閉じる操作だけでは破棄せず、Appで確認と常駐リソースの解放を行う。
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.setContentPane(panel);
		mainFrame.setResizable(false);
		mainFrame.setAlwaysOnTop(true);
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		setApplicationIcon(mainFrame);

		mainFrame.addWindowListener(new WindowAdapter() {
			/**
			 * ウィンドウの閉じる操作をAppの終了確認へ渡します。
			 *
			 * @param event 閉じる操作のイベント
			 */
			@Override
			public void windowClosing(WindowEvent event) {
				app.requestExit();
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
