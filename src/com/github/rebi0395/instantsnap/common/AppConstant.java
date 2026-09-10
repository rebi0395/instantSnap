package com.github.rebi0395.instantsnap.common;

import java.nio.file.Path;

/**
 * 定数管理クラス
 */
public final class AppConstant {

	// インスタンス化しない
	private AppConstant() {
	}

	// -- プロパティファイル --
	public static final String OPTION_FILE_NAME = "config.properties";
	/** プロパティのファイルパス */
	public static final Path PROPERTIES_PATH = Path.of(OPTION_FILE_NAME);
	/** プロパティファイルの一時保存パス */
	public static final Path TEMP_PATH = PROPERTIES_PATH.resolveSibling(PROPERTIES_PATH.getFileName() + ".tmp");

	// -- プロパティの項目名 --
	/** プロパティ 言語設定 */
	public static final String PROPERTY_LANGUAGE = "language";
	/** プロパティ 撮影キー */
	public static final String PROPERTY_SNAP_KEY = "snapKey";
	/** プロパティ 撮影キーでAltキーを使用するか */
	public static final String PROPERTY_SNAP_USE_ALT = "snapIsUseAlt";
	/** プロパティ 撮影キーでCtrlキーを使用するか */
	public static final String PROPERTY_SNAP_USE_CTRL = "snapIsUseCtrl";
	/** プロパティ 撮影キーでShiftキーを使用するか */
	public static final String PROPERTY_SNAP_USE_SHIFT = "snapIsUseShift";
	/** プロパティ 範囲選択キー */
	public static final String PROPERTY_SELECTION_KEY = "selectionKey";
	/** プロパティ 範囲選択キーでAltキーを使用するか */
	public static final String PROPERTY_SELECTION_USE_ALT = "selectionIsUseAlt";
	/** プロパティ 範囲選択キーでCtrlキーを使用するか */
	public static final String PROPERTY_SELECTION_USE_CTRL = "selectionIsUseCtrl";
	/** プロパティ 範囲選択キーでShiftキーを使用するか */
	public static final String PROPERTY_SELECTION_USE_SHIFT = "selectionIsUseShift";
	/** プロパティ 範囲確定後に自動撮影するか */
	public static final String PROPERTY_AUTO_SNAP_AFTER_RANGE_SELECTION_ENABLED =
			"autoSnapAfterRangeSelectionEnabled";
	/** プロパティ 撮影後に範囲撮影モードを自動で終了するか */
	public static final String PROPERTY_CLOSE_RANGE_SELECTION_AFTER_SNAP_ENABLED =
			"closeRangeSelectionAfterSnapEnabled";
	/** プロパティ 撮影範囲 */
	public static final String PROPERTY_SNAP_AREA = "snapArea";
	/** プロパティ マウスカーソルを写すか */
	public static final String PROPERTY_INCLUDE_CURSOR_ENABLED = "includeCursorEnabled";
	/** プロパティ InstantSnapのウィンドウを写すか */
	public static final String PROPERTY_INCLUDE_SETTING_WINDOW_ENABLED = "includeSettingWindowEnabled";
	/** プロパティ 画像保存をするか */
	public static final String PROPERTY_SAVE_IMAGE_ENABLED = "saveImageEnabled";
	/** プロパティ クリップボードコピーをするか */
	public static final String PROPERTY_CLIPBOARD_ENABLED = "clipboardEnabled";
	/** プロパティ 保存パス */
	public static final String PROPERTY_SAVE_PATH = "savePath";
	/** プロパティ ファイル名の先頭文字 */
	public static final String PROPERTY_FILE_NAME_PREFIX = "fileNamePrefix";
	/** プロパティ ファイル名の日時指定 */
	public static final String PROPERTY_FILE_NAME_DATE_FORMAT = "fileNameDateFormat";
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

	/** プロパティ テーマ設定 */
	public static final String PROPERTY_THEME = "theme";

	// -- デフォルト値 --  
	/** 保存先ディレクトリのデフォルト値 */
	public static final String DEFAULT_SAVE_DIR = "./screenshots/";
	/** 保存先ディレクトリのデフォルト値 */
	public static final String DEFAULT_FILE_NAME_PREFIX = "";
	/** 撮影キーでCtrlキーを使用するかどうかのデフォルト値 */
	public static final boolean DEFAULT_SNAP_USE_CTRL = false;
	/** 撮影キーでAltキーを使用するかどうかのデフォルト値 */
	public static final boolean DEFAULT_SNAP_USE_ALT = false;
	/** 撮影キーでShiftキーを使用するかどうかのデフォルト値 */
	public static final boolean DEFAULT_SNAP_USE_SHIFT = true;
	/** 範囲選択キーでCtrlキーを使用するかどうかのデフォルト値 */
	public static final boolean DEFAULT_SELECTION_USE_CTRL = false;
	/** 範囲選択キーでAltキーを使用するかどうかのデフォルト値 */
	public static final boolean DEFAULT_SELECTION_USE_ALT = false;
	/** 範囲選択キーでShiftキーを使用するかどうかのデフォルト値 */
	public static final boolean DEFAULT_SELECTION_USE_SHIFT = false;
	/** 範囲確定後に自動撮影するかどうかのデフォルト値 */
	public static final boolean DEFAULT_AUTO_SNAP_AFTER_RANGE_SELECTION_ENABLED = true;
	/** 撮影後に範囲撮影モードを自動で終了するかどうかのデフォルト値 */
	public static final boolean DEFAULT_CLOSE_RANGE_SELECTION_AFTER_SNAP_ENABLED = true;
	/** 画像にマウスカーソルを含めるかどうかのデフォルト値 */
	public static final boolean DEFAULT_INCLUDE_CURSOR_ENABLED = false;
	/** 画像にInstantSnapのウィンドウを含めるかどうかのデフォルト値 */
	public static final boolean DEFAULT_INCLUDE_SETTING_WINDOW_ENABLED = false;
	/** 画像保存を有効にするかどうかのデフォルト値 */
	public static final boolean DEFAULT_SAVE_IMAGE_ENABLED = true;
	/** クリップボード保存を有効にするかどうかのデフォルト値 */
	public static final boolean DEFAULT_CLIPBOARD_ENABLED = true;
	/** 撮影時通知を表示するかどうかのデフォルト値 */
	public static final boolean DEFAULT_NOTIFICATION_ENABLED = true;
	/** 撮影音を鳴らすかどうかのデフォルト値 */
	public static final boolean DEFAULT_SOUND_SNAP_ENABLED = true;
	/** 通知音を鳴らすかどうかのデフォルト値 */
	public static final boolean DEFAULT_SOUND_NOTIFICATION_ENABLED = true;

	// -- 外部ファイル --
	/** JavaFX設定ウィンドウのFXMLファイル */
	public static final String FXML_SETTING_WINDOW = "/com/github/rebi0395/instantsnap/view/javafx/fxml/SettingWindow.fxml";

	/** JavaFX設定ウィンドウのCSSファイル */
	public static final String CSS_SETTING_WINDOW = "/com/github/rebi0395/instantsnap/view/javafx/css/setting-window.css";
	/** JavaFX設定ウィンドウのテーマCSS：絵本 */
	public static final String CSS_SETTING_THEME_STORYBOOK = "/com/github/rebi0395/instantsnap/view/javafx/css/theme-storybook.css";
	/** JavaFX設定ウィンドウのテーマCSS：ダーク */
	public static final String CSS_SETTING_THEME_DARK = "/com/github/rebi0395/instantsnap/view/javafx/css/theme-dark.css";
	/** テーマCSSファイル名とテーマstyleClassの接頭辞 */
	public static final String THEME_CSS_PREFIX = "theme-";
	/** アイコンのpngファイル */
	public static final String ICON_APP = "/images/icon/InstantSnap.png";
	/** JavaFX設定ウィンドウの専用アイコン */
	public static final String ICON_SETTING_WINDOW = "/images/icon/InstantSnap_setting.png";

	/** 一時保存先ディレクトリ */
	public static final String TMP_SAVE_DIR = "./tmp/";

	// -- OS名
	/** OS名：Windows */
	public static final String OS_NAME_WINDOWS = "Windows";
	/** OS名：macOS */
	public static final String OS_NAME_MAC = "macOS";
	/** OS名：Linux */
	public static final String OS_NAME_LINUX = "Linux";
	/** OS名：その他 */
	public static final String OS_NAME_OTHER = "Other";

}
