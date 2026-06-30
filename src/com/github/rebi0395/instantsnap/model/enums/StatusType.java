package com.github.rebi0395.instantsnap.model.enums;

import static com.github.rebi0395.instantsnap.common.MessageKeyConstant.*;

/**
 * メイン画面のステータス表示を表す列挙型クラス
 */
public enum StatusType implements MessageEnum {

	/** 起動中 */
	STARTING("Starting InstantSnap...", STATUS_STARTING, 0),
	/** 撮影可能 */
	READY("Press the configured shortcut key to take a screenshot.", STATUS_CAN_SNAP, 0),
	/** 設定保存中 */
	SAVING("Saving settings...", STATUS_SAVING, 0),
	/** 設定保存完了 */
	SAVED("Settings saved.", STATUS_SAVED, 5),
	/** 入力値初期化 */
	RESET_INPUT("Input values have been reset. Click Apply Settings to save the changes.", STATUS_RESET_INPUT, 5);

	/** 基準表示名（英語） */
	private final String baseDisplayName;
	/** メッセージキー */
	private final String messageKey;
	/** 表示自動切り替え秒数 (0の場合は自動切り替えしない) */
	private final int changeSeconds;

	/** コンストラクタ */
	StatusType(String baseDisplayName, String messageKey, int changeSeconds) {
		this.baseDisplayName = baseDisplayName;
		this.messageKey = messageKey;
		this.changeSeconds = changeSeconds;
	}

	/**
	 * @return baseDisplayName 基準表示名（英語）
	 */
	@Override
	public String getBaseDisplayName() {
		return baseDisplayName;
	}

	/**
	 * @return messageKey メッセージキー
	 */
	@Override
	public String getMessageKey() {
		return messageKey;
	}

	/**
	 * @return changeSeconds 表示自動切り替え秒数 (0の場合は自動切り替えしない) 
	 */
	public int getChangeSeconds() {
		return changeSeconds;
	}
}
