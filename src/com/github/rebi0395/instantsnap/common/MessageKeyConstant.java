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
	/** クリップボード保存通知メッセージキー */
	public static final String NOTIFICATION_CLIPBOARD_SAVED = "notification.clipboard.saved";
	/** 保存先を開く通知アクション文言キー */
	public static final String NOTIFICATION_OPEN_SAVE_DIR = "notification.open.save.dir";

}
