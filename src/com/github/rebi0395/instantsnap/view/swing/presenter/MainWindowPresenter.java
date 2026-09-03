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

import com.github.rebi0395.instantsnap.common.MessageKeyConstant;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.KeyCombinationBean;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.view.swing.panel.MainWindowPanel;
import com.github.rebi0395.instantsnap.view.swing.panel.MainWindowPanel.LanguageChangeListener;

/**
 * Swingメインウィンドウの生成、表示、表示内容の更新を行うプレゼンターです。
 * アプリの起動や撮影処理は持たず、利用者の操作を登録された通知先へ渡します。
 */
public final class MainWindowPresenter {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(MainWindowPresenter.class);

	/** Swingメインウィンドウのタイトル */
	private static final String WINDOW_TITLE = "InstantSnap";

	/** Swingメインウィンドウ */
	private final JFrame frame;

	/** メインウィンドウに表示するパネル */
	private final MainWindowPanel panel;

	/** 言語別メッセージを取得するクラス */
	private final MessageKeyConverter converter;

	/**
	 * 現在の設定値を使ってSwingメインウィンドウを生成します。
	 *
	 * @param config 現在の設定値
	 * @param converter 言語別メッセージの取得クラス
	 * @param languageChangeListener 言語変更時の通知先
	 * @param settingsAction 設定ボタン押下時の処理
	 * @param captureAction 撮影ボタン押下時の処理
	 * @param rangeSelectionAction 範囲選択ボタン押下時の処理
	 * @param exitAction ウィンドウを閉じようとした時の処理
	 */
	public MainWindowPresenter(
			ConfigBean config,
			MessageKeyConverter converter,
			LanguageChangeListener languageChangeListener,
			Runnable settingsAction,
			Runnable captureAction,
			Runnable rangeSelectionAction,
			Runnable exitAction) {
		this.converter = converter;

		UiTexts texts = resolveUiTexts(config.getLanguageType());
		this.panel = new MainWindowPanel(
				texts.settings(),
				texts.capture(),
				texts.rangeSelection(),
				resolveShortcutText(config.getSnapKeyBean(), texts.notSet()),
				resolveShortcutText(config.getSelectionKeyBean(), texts.notSet()),
				config.getLanguageType(),
				languageChangeListener,
				settingsAction,
				captureAction,
				rangeSelectionAction);
		this.frame = createFrame(panel, exitAction);
	}

	/** メインウィンドウを表示します。 */
	public void show() {
		frame.setVisible(true);
	}

	/** 現在の設定値をメインウィンドウの全表示へ反映します。 */
	public void refresh(ConfigBean config) {
		UiTexts texts = resolveUiTexts(config.getLanguageType());
		panel.updateButtonTexts(texts.settings(), texts.capture(), texts.rangeSelection());
		panel.updateShortcutTexts(
				resolveShortcutText(config.getSnapKeyBean(), texts.notSet()),
				resolveShortcutText(config.getSelectionKeyBean(), texts.notSet()));
		panel.updateLanguageSelection(config.getLanguageType());
	}

	/** JavaFX設定画面の起動状態に応じて設定操作の有効状態を変更します。 */
	public void setSettingsAvailable(boolean available) {
		panel.setSettingsEnabled(available);
		panel.setLanguageSelectionEnabled(available);
	}

	/** メインウィンドウにある全操作の有効状態を変更します。 */
	public void setActionsEnabled(boolean enabled) {
		panel.setActionsEnabled(enabled);
	}

	/**
	 * アプリを終了するか確認します。
	 *
	 * @return 終了が選択された場合true
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

	/** JavaFX設定画面を起動できなかった場合の案内を表示します。 */
	public void showSettingsStartError(LanguageType language) {
		String message;
		if (language == LanguageType.JAPANESE) {
			message = "設定画面を起動できませんでした。ログを確認してください。";
		} else {
			message = "Could not open Settings. Check the log for details.";
		}
		JOptionPane.showMessageDialog(frame, message, WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
	}

	/** メインウィンドウを閉じて画面リソースを解放します。 */
	public void close() {
		frame.setVisible(false);
		frame.dispose();
	}

	/** Swingメインウィンドウを生成し、終了要求を登録します。 */
	private JFrame createFrame(MainWindowPanel mainPanel, Runnable exitAction) {
		JFrame mainFrame = new JFrame(WINDOW_TITLE);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.setContentPane(mainPanel);
		mainFrame.setResizable(false);
		mainFrame.setAlwaysOnTop(true);
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		setApplicationIcon(mainFrame);

		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				exitAction.run();
			}
		});
		return mainFrame;
	}

	/** キー設定が未定義の場合は代替文言を返します。 */
	private String resolveShortcutText(KeyCombinationBean keyCombination, String notSetText) {
		if (keyCombination == null || !keyCombination.isTriggerKeyDefined()) {
			return notSetText;
		}
		return keyCombination.resolveKeyCombinationName();
	}

	/** 言語設定に対応したメインウィンドウ用の短い表示名を返します。 */
	private UiTexts resolveUiTexts(LanguageType language) {
		if (language == LanguageType.JAPANESE) {
			return new UiTexts("設定", "撮影", "範囲選択", "未設定");
		}
		return new UiTexts("Settings", "Capture", "Select area", "Not set");
	}

	/** メインウィンドウへInstantSnapのアプリアイコンを設定します。 */
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

	/** メインウィンドウで使う短い文言をまとめたレコードです。 */
	private record UiTexts(String settings, String capture, String rangeSelection, String notSet) {
	}
}
