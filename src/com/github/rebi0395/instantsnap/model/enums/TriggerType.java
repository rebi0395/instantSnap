package com.github.rebi0395.instantsnap.model.enums;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import com.github.rebi0395.instantsnap.common.AppLogger;

import javafx.util.StringConverter;
import static com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*;

/**
 * トリガーキーを定義する列挙型クラス
 */
public enum TriggerType implements BaseEnum {

	/** Shift + PrintScreenキー */
	SHIFT_PRINTSCREEN("1", "Shift + PrintScreen"),
	/** PrintScreenキー */
	PRINTSCREEN("2", "PrintScreen"),
	/** F10キー */
	F10("3", "F10"),
	/** カスタム設定 */
	CUSTOM("4", "Custom");

	/** プロパティファイルと紐つく値 */
	private final String value;
	/** 表示名 */
	private final String displayName;

	/** コンストラクタ */
	TriggerType(String value, String displayName) {
		this.value = value;
		this.displayName = displayName;
	}

	/**
	 * 列挙型に対応するキーコードの配列を返す
	 * カスタム設定の場合は、そのまま値を返却する
	 * @param customKeyCode
	 * @return 列挙型に対応するキーコードの配列
	 */
	public int[] getKeyCode(int... customKeyCode) {
		switch (this) {
		case SHIFT_PRINTSCREEN:
			return new int[] { VC_SHIFT, VC_PRINTSCREEN };

		case PRINTSCREEN:
			return new int[] { VC_PRINTSCREEN };

		case F10:
			return new int[] { VC_F10 };

		case CUSTOM:
			if (customKeyCode == null || customKeyCode.length == 0) {
				// カスタム設定だが、キーコード未設定の場合
				AppLogger.warn("キーが設定されていません。デフォルト値を適応します");
				return new int[] { VC_SHIFT, VC_PRINTSCREEN };
			}

			return customKeyCode;

		default:
			return new int[] { VC_SHIFT, VC_PRINTSCREEN };
		}
	}

	/**
	 * @return value プロパティファイルと紐つく値
	 */
	@Override
	public String getValue() {
		return value;
	}

	/**
	 * @return displayName 表示名
	 */
	@Override
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * プロパティファイルの値から対応する TriggerType を取得する。
	 * 不正な値が指定された場合は デフォルト値を返し、ログを出力する。
	 * 
	 * @param raw プロパティファイルと紐つく値
	 * @return 対応する TriggerType、またはデフォルトの TriggerType
	 */
	public static TriggerType parseOrDefault(String raw) {
		return BaseEnum.parseOrDefault(TriggerType.class, raw, SHIFT_PRINTSCREEN, TRIGGER);
	}

	/**
	 * 列挙型と文字列を相互変換する	JavaFXコンバーターを取得する
	 * 
	 * @return TriggerType と文字列を交互変換するJavaFXコンバーター
	 */
	public static StringConverter<TriggerType> toConverter() {
		return BaseEnum.toConverter(TriggerType.class, SHIFT_PRINTSCREEN);
	}
}
