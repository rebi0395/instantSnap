package com.github.rebi0395.instantsnap.common;

/**
 * 定数管理クラス
 */
public final class AppConstant {

	// インスタンス化しない
	private AppConstant() {
	}

	// -- プロパティファイル --
	public static final String CONFIG_FILE_NAME = "config.properties";

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

	public static final String PROPERTY_SNAP_AFTER = "snapAfter";
	public static final String PROPERTY_SOUND_ENABLED = "soundEnabled";
	public static final String PROPERTY_NOTIFICATION_ENABLED = "notificationEnabled";

	// -- 保存先ファイルのデフォルト値 --
	public static final String DEFAULT_SAVE_PATH = "./screenshots/";

	// -- アイコンのpngファイル --
	public static final String ICON_APP = "/icon/instantsnap.png";

	// -- FXMLファイル --
	public static final String FXML_MAIN_VIEW = "/com/github/rebi0395/instantsnap/view/fxml/MainView.fxml";
	public static final String FXML_EXIT_DIALOG = "/com/github/rebi0395/instantsnap/view/fxml/ExitDialog.fxml";

	// -- CSSファイル --
	public static final String CSS_MAIN = "/com/github/rebi0395/instantsnap/view/css/main.css";

}
