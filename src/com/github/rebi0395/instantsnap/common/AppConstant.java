package com.github.rebi0395.instantsnap.common;

/**
 * 定数管理クラス
 */
public final class AppConstant {

	// インスタンス化しない
	private AppConstant() {
	}

	// -- プロパティファイル --
	public static final String OPTION_FILE_NAME = "config.properties";

	// -- プロパティの項目名 --
	/** プロパティ 言語設定 */
	public static final String PROPERTY_LANGUAGE = "language";
	/** プロパティ トリガーキー設定 */
	public static final String PROPERTY_TRIGGER = "trigger";
	/** プロパティ カスタムキー */
	public static final String PROPERTY_CUSTOM_KEY = "customKey";
	/** プロパティ Altキー */
	public static final String PROPERTY_USE_ALT = "useAlt";
	/** プロパティ Ctrlキー */
	public static final String PROPERTY_USE_CTRL = "useCtrl";
	/** プロパティ Shiftキー */
	public static final String PROPERTY_USE_SHIFT = "useShift";
	/** プロパティ 撮影範囲 */
	public static final String PROPERTY_SNAP_AREA = "snapArea";
	/** プロパティ 画像保存をするか */
	public static final String PROPERTY_SAVE_IMAGE_ENABLED = "saveImageEnabled";
	/** プロパティ クリップボードコピーをするか */
	public static final String PROPERTY_CLIPBOARD_ENABLED = "clipboardEnabled";
	/** プロパティ 保存パス */
	public static final String PROPERTY_SAVE_PATH = "savePath";
	/** プロパティ ファイル名の先頭文字 */
	public static final String PROPERTY_FILE_NAME_PREFIX = "fileNamePrefix";
	/** プロパティ 拡張子 */
	public static final String PROPERTY_FORMAT = "format";
	/** プロパティ 撮影後アクション */
	public static final String PROPERTY_AFTER_SNAP_ACTION = "afterSnapAction";
	/** プロパティ 通知を表示するか */
	public static final String PROPERTY_NOTIFICATION_ENABLED = "notificationEnabled";
	/** プロパティ 撮影音を有効にするか */
	public static final String PROPERTY_SOUND_SNAP_ENABLED = "soundSnapEnabled";
	/** プロパティ 通知音を有効にするか */
	public static final String PROPERTY_SOUND_NOTIFICATION_ENABLED = "soundNotificationEnabled";

	// -- デフォルト値 --  
	/** 保存先ディレクトリのデフォルト値 */
	public static final String DEFAULT_SAVE_DIR = "./screenshots/";
	
	/** 保存先ディレクトリのデフォルト値 */
	public static final String DEFAULT_FILE_NAME_PREFIX = "";

	/** カスタムキーでCtrlキーを使用するかどうかのデフォルト値 */
	public static final boolean DEFAULT_USE_CTRL = false;

	/** カスタムキーでAltキーを使用するかどうかのデフォルト値 */
	public static final boolean DEFAULT_USE_ALT = false;

	/** カスタムキーでShiftキーを使用するかどうかのデフォルト値 */
	public static final boolean DEFAULT_USE_SHIFT = false;

	/** 画像保存を有効にするかどうかのデフォルト値 */
	public static final boolean DEFAULT_SAVE_IMAGE_ENABLED = true;

	/** クリップボード保存を有効にするかどうかのデフォルト値 */
	public static final boolean DEFAULT_CLIPBOARD_ENABLED = true;

	/** 撮影時通知を表示するかどうかのデフォルト値 */
	public static final boolean DEFAULT_NOTIFICATION_ENABLED = true;

	/** 撮影音を鳴らすかどうかのデフォルト値 */
	public static final boolean DEFAULT_SOUND_SNAP_ENABLED = false;

	/** 通知音を鳴らすかどうかのデフォルト値 */
	public static final boolean DEFAULT_SOUND_NOTIFICATION_ENABLED = true;

	// -- 外部ファイル --
	/** メインウィンドウのFXMLファイル */
	public static final String FXML_MAIN_WINDOW = "/com/github/rebi0395/instantsnap/view/fxml/MainWindow.fxml";
	/** 終了ダイアログのFXMLファイル */
	public static final String FXML_EXIT_DIALOG = "/com/github/rebi0395/instantsnap/view/fxml/ExitDialog.fxml";
	/** 通知画面のFXMLファイル */
	public static final String FXML_NOTIFICATION = "/com/github/rebi0395/instantsnap/view/fxml/SnapNotification.fxml";

	/** メインウィンドウのCSSファイル */
	public static final String CSS_MAIN_WINDOW = "/com/github/rebi0395/instantsnap/view/css/main-window.css";
	/** 通知画面のCSSファイル */
	public static final String CSS_NOTIFICATION = "/com/github/rebi0395/instantsnap/view/css/notification.css";

	/** アイコンのpngファイル */
	public static final String ICON_APP = "/icon/instantsnap.png";

	/** 一時保存先ディレクトリ */
	public static final String TMP_SAVE_DIR = "./tmp/";

}
