package com.github.rebi0395.instantsnap.model.enums;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;
import static com.github.rebi0395.instantsnap.common.MessageKeyConstant.*;

import com.github.rebi0395.instantsnap.common.MessageKeyConverter;

import javafx.util.StringConverter;

/**
 * 撮影モードを指定する列挙型クラス
 */
public enum SnapAreaType implements MessageEnum {

	/** 使用しているウィンドウのみ撮影 */
	ACTIVE_WINDOW("1", "Active window", SNAP_AREA_ACTIVE_WINDOW),
	/** マウスポインターのあるモニターを撮影 */
	ACTIVE_SCREEN("2", "Screen with mouse pointer", SNAP_AREA_ACTIVE_SCREEN),
	/** メインモニターを固定して撮影 */
	MAIN_SCREEN("3", "Main screen", SNAP_AREA_MAIN_SCREEN),
	/** 全てのモニターを全て含んで撮影 */
	FULL_SCREEN("4", "Full screen", SNAP_AREA_FULL_SCREEN);

	/** プロパティファイルと紐つく値 */
	private final String value;
	/** 基準表示名（英語） */
	private final String baseDisplayName;
	/** メッセージキー */
	private final String messageKey;

	/** コンストラクタ */
	SnapAreaType(String value, String baseDisplayName, String messageKey) {
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
	 * プロパティファイルの値から対応する SnapAreaType を取得する。
	 * 不正な値が指定された場合は デフォルト値を返し、ログを出力する。
	 * @param raw プロパティファイルと紐つく値
	 * @return 対応する SnapAreaType、またはデフォルトの SnapAreaType
	 */
	public static SnapAreaType parseOrDefault(String raw) {
		return BaseEnum.parseOrDefault(SnapAreaType.class, raw, ACTIVE_WINDOW, PROPERTY_SNAP_AREA);
	}

	/**
	 * 列挙型と文字列を相互変換する	JavaFXコンバーターを取得する
	 * @return SnapAreaType と文字列を相互変換するJavaFXコンバーター
	 */
	public static StringConverter<SnapAreaType> toConverter(MessageKeyConverter converter) {
		return MessageEnum.toMessageConverter(SnapAreaType.class, ACTIVE_WINDOW, converter);
	}
}
