package com.github.rebi0395.instantsnap.common;

/**
 * メッセージキーの定数クラス
 */
public final class MessageKeyConstant {

	// インスタンス化しない
	private MessageKeyConstant() {
	}

	/** 言語別プロパティファイルの格納先 */
	public static final String BASE_NAME = "i18n.messages";

	// -- メインウィンドウ固定文言 -- 
	/** タブ：基本設定 */
	public static final String TAB_BASIC_SETTINGS = "tab.basicSettings";
	/** タブ：その他設定 */
	public static final String TAB_OTHER_SETTINGS = "tab.otherSettings";

	/** キャプション：出力 */
	public static final String CAPTION_OUTPUT = "caption.output";

	/** セクション：ショートカットキー選択 */
	public static final String SECTION_SHORTCUT_KEY = "section.shortcutKey";
	/** セクション：カスタムキー設定 */
	public static final String SECTION_CUSTOM_KEY = "section.customKey";
	/** セクション：撮影範囲 */
	public static final String SECTION_SNAP_AREA = "section.snapArea";
	/** セクション：保存方法 */
	public static final String SECTION_SAVE_METHOD = "section.saveMethod";
	/** セクション：保存先フォルダ選択 */
	public static final String SECTION_SELECT_SAVE_DIRECTORY = "section.selectSaveDirectory";
	/** セクション：ファイル名の設定 */
	public static final String SECTION_FILE_NAME = "section.fileName";
	/** セクション：撮影後の動作 */
	public static final String SECTION_AFTER_SNAP_ACTION = "section.afterSnapAction";
	/** セクション：通知表示 */
	public static final String SECTION_NOTIFICATION = "section.notification";
	/** セクション：サウンド設定 */
	public static final String SECTION_SOUND = "section.sound";

	/** チェック項目：画像を保存する */
	public static final String CHECK_SAVE_IMAGE = "check.saveImage";
	/** チェック項目：クリップボードにコピーする */
	public static final String CHECK_COPY_TO_CLIPBOARD = "check.copyToClipboard";
	/** チェック項目：通知を表示する */
	public static final String CHECK_SHOW_NOTIFICATION = "check.showNotification";
	/** チェック項目：撮影音を有効にする */
	public static final String CHECK_ENABLE_SNAP_SOUND = "check.enableSnapSound";
	/** チェック項目：通知音を有効にする */
	public static final String CHECK_ENABLE_NOTIFICATION_SOUND = "check.enableNotificationSound";

	/** 折りたたみ項目：画像保存先の設定 */
	public static final String ACCORDION_SAVE_DESTINATION = "accordion.saveDestination";

	/** ボタン：入力値を初期化 */
	public static final String BUTTON_RESET_TO_DEFAULTS = "button.resetInput";
	/** ボタン：設定を反映 */
	public static final String BUTTON_APPLY_CONFIG = "button.applyConfig";
	/** ボタン：ファイル選択ボタン */
	public static final String BUTTON_SELECT_FILE = "button.selectFile";

	/** 入力欄：カスタムキー */
	public static final String PROMPT_CUSTOM_KEY = "prompt.customKey";

	/** ラベルの有効を表示するメッセージキー */
	public static final String LABEL_ENABLED = "label.enabled";
	/** ラベルで無効を表示するメッセージキー */
	public static final String LABEL_DISABLED = "label.disabled";

	/** ステータス：起動中 */
	public static final String STATUS_STARTING = "status.starting";	
	/** ステータス：撮影可能 */
	public static final String STATUS_CAN_SNAP = "status.canSnap";
	/** ステータス：設定反映中 */
	public static final String STATUS_SAVING = "status.saving";
	/** ステータス：設定反映完了 */
	public static final String STATUS_SAVED = "status.saved";
	/** ステータス：入力値初期化完了 */
	public static final String STATUS_RESET_INPUT = "status.resetInput";

	// -- SnapAreaType --
	/** 撮影範囲：使用しているウィンドウのみ撮影 */
	public static final String SNAP_AREA_ACTIVE_WINDOW = "snapArea.activeWindow";
	/** 撮影範囲：マウスポインターのあるモニターを撮影 */
	public static final String SNAP_AREA_ACTIVE_SCREEN = "snapArea.activeScreen";
	/** 撮影範囲：メインモニターを固定して撮影 */
	public static final String SNAP_AREA_MAIN_SCREEN = "snapArea.mainScreen";
	/** 撮影範囲：全てのモニターを含んで撮影 */
	public static final String SNAP_AREA_FULL_SCREEN = "snapArea.fullScreen";

	//-- AfterSnapActionType --
	/** 撮影後動作：何もしない */
	public static final String AFTER_SNAP_ACTION_NONE = "afterSnapAction.none";
	/** 撮影後動作：画像を開く */
	public static final String AFTER_SNAP_ACTION_OPEN_IMAGE = "afterSnapAction.openImage";
	/** 撮影後動作：ペイントで開く */
	public static final String AFTER_SNAP_ACTION_OPEN_PAINT = "afterSnapAction.openPaint";
	/** 撮影後動作：一時保存フォルダに保存 */
	public static final String AFTER_SNAP_ACTION_SAVE_TO_TMP = "afterSnapAction.saveToTmp";

	// -- 撮影後通知 -- 
	/** 画像保存通知メッセージキー */
	public static final String NOTIFICATION_IMAGE_SAVED = "notification.image.saved";
	/** 一時保存通知メッセージキー */
	public static final String NOTIFICATION_TMP_IMAGE_SAVED = "notification.tmp.image.saved";
	/** 一時保存フォルダがアプリ終了時にファイルを消すことを警告するメッセージキー */
	public static final String NOTIFICATION_TMP_WARN = "notification.tmp.warn";
	/** クリップボード保存通知メッセージキー */
	public static final String NOTIFICATION_CLIPBOARD_SAVED = "notification.clipboard.saved";
	/** 保存先を開く通知アクション文言キー */
	public static final String NOTIFICATION_OPEN_SAVE_DIR = "notification.open.save.dir";

	// -- 終了ダイアログ --
	/** 終了ダイアログ：タイトル */
	public static final String DIALOG_EXIT_TITLE = "dialog.exit.title";
	/** 終了ダイアログ：アプリ終了確認文言 */
	public static final String DIALOG_EXIT_CONFIRM_MESSAGE = "dialog.exit.confirm.message";
	/** 終了ダイアログ：一時保存フォルダ消去確認文言 */
	public static final String DIALOG_EXIT_DELETE_TMP = "dialog.exit.delete.tmp";
	/** 終了ダイアログ：終了するボタン */
	public static final String DIALOG_EXIT_BUTTON_APPLY = "dialog.exit.button.apply";
	/** 終了ダイアログ：キャンセルボタン */
	public static final String DIALOG_EXIT_BUTTON_CANCEL = "dialog.exit.button.cancel";
		
}
