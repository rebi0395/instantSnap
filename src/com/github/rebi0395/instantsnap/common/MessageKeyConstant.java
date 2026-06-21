package com.github.rebi0395.instantsnap.common;

public final class MessageKeyConstant {

	// インスタンス化しない
	private MessageKeyConstant() {
	}

	/** 言語別プロパティファイルの格納先 */
	public static final String BASE_NAME = "i18n.messages";

	/** ラベルの有効を表示するメッセージキー */
	public static final String LABEL_ENABLED = "label.enabled";
	/** ラベルで無効を表示するメッセージキー */
	public static final String LABEL_DISABLED = "label.disabled";

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

}
