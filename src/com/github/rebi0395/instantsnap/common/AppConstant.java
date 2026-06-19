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
	public static final String PROPERTY_LANGUAGE = "language";

	public static final String PROPERTY_TRIGGER = "trigger";
	public static final String PROPERTY_CUSTOM_KEY = "customKey";
	public static final String PROPERTY_USE_ALT = "useAlt";
	public static final String PROPERTY_USE_CTRL = "useCtrl";
	public static final String PROPERTY_USE_SHIFT = "useShift";
	public static final String PROPERTY_SNAP_AREA = "snapArea";

	public static final String PROPERTY_SAVE_IMAGE_ENABLED = "saveImageEnabled";
	public static final String PROPERTY_CLIPBOARD_ENABLED = "clipboardEnabled";
	public static final String PROPERTY_SAVE_PATH = "savePath";
	public static final String PROPERTY_FORMAT = "format";

	public static final String PROPERTY_AFTER_SNAP_ACTION = "afterSnapAction";
	public static final String PROPERTY_SOUND_ENABLED = "soundEnabled";
	public static final String PROPERTY_NOTIFICATION_ENABLED = "notificationEnabled";

	/** 保存先ディレクトリのデフォルト値 */
	public static final String DEFAULT_SAVE_DIR = "./screenshots/";
	
	/** 一時保存先ディレクトリ */
	public static final String TMP_SAVE_DIR = "./tmp/";

	/** アイコンのpngファイル */
	public static final String ICON_APP = "/icon/instantsnap.png";

	/** メインウィンドウのFXMLファイル */
	public static final String FXML_MAIN_VIEW = "/com/github/rebi0395/instantsnap/view/fxml/MainView.fxml";
	/** 終了ダイアログのFXMLファイル */
	public static final String FXML_EXIT_DIALOG = "/com/github/rebi0395/instantsnap/view/fxml/ExitDialog.fxml";
	/** 通知画面のFXMLファイル */
	public static final String FXML_NOTIFICATION = "/com/github/rebi0395/instantsnap/view/fxml/SnapNotification.fxml";
	
	
	/** メインウィンドウのCSSファイル */
	public static final String CSS_MAIN = "/com/github/rebi0395/instantsnap/view/css/main.css";

}
