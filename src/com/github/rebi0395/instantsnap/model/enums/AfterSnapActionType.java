package com.github.rebi0395.instantsnap.model.enums;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;
import static com.github.rebi0395.instantsnap.common.MessageKeyConstant.*;

import com.github.rebi0395.instantsnap.common.MessageKeyConverter;

import javafx.util.StringConverter;

/**
 * 撮影後の挙動パターンを定義する列挙型クラス
 */
public enum AfterSnapActionType implements BaseEnum, MessageEnum {

	/** 何もしない(デフォルト値) */
	NONE("0", "None", AFTER_SNAP_ACTION_NONE),
	/** 画像を開く */
	OPEN_IMAGE("1", "Open Image", AFTER_SNAP_ACTION_OPEN_IMAGE),
	/** ペイントで開く */
	OPEN_PAINT("2", "Open in Paint", AFTER_SNAP_ACTION_OPEN_PAINT),
	/** 一時保存フォルダに保存 */
	SAVE_TO_TMP("3", "Save to temporary folder", AFTER_SNAP_ACTION_SAVE_TO_TMP);

	/** プロパティファイルと紐つく値 */
	private final String value;
	/** 基準表示名 */
	private final String baseDisplayName;
	/** メッセージキー */
	private final String messageKey;

	/** コンストラクタ */
	AfterSnapActionType(String value, String baseDisplayName, String messageKey) {
		this.value = value;
		this.baseDisplayName = baseDisplayName;
		this.messageKey = messageKey;
	}

	/**
	 * @return value プロパティファイルと紐つく値
	 */
	@Override
	public String getValue() {
		return value;
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
		// TODO 自動生成されたメソッド・スタブ
		return messageKey;
	}

	/**
	 * プロパティファイルの値から対応する AfterSnapActionType を取得する。
	 * 不正な値が指定された場合は デフォルト値を返し、ログを出力する。
	 * @param raw プロパティファイルと紐つく値
	 * @return 対応する AfterSnapActionType、またはデフォルトの AfterSnapActionType
	 */
	public static AfterSnapActionType parseOrDefault(String raw) {
		return BaseEnum.parseOrDefault(AfterSnapActionType.class, raw, NONE, PROPERTY_AFTER_SNAP_ACTION);
	}

	/**
	 * 列挙型と文字列を相互変換する	JavaFXコンバーターを取得する
	 * @return AfterSnapActionType と文字列を相互変換するJavaFXコンバーター
	 */
	public static StringConverter<AfterSnapActionType> toConverter(MessageKeyConverter converter) {
		return MessageEnum.toMessageConverter(AfterSnapActionType.class, NONE, converter);
	}
}