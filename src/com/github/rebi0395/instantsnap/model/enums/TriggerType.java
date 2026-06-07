package com.github.rebi0395.instantsnap.model.enums;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import com.github.rebi0395.instantsnap.model.bean.KeyCombinationBean;

import javafx.util.StringConverter;
import static com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*;

/**
 * トリガーキーを定義する列挙型クラス
 */
public enum TriggerType implements BaseEnum {

	/** Shift + PrintScreen キー */
	SHIFT_PRINTSCREEN("1", "Shift + Print Screen"),
	/** Ctrl + Shift + S キー */
	CTRL_SHIFT_S("2", "Ctrl + Shift + S"),
	/** F9 キー */
	F9("3", "F9"),
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
	 * ユーザーのトリガーキー設定に応じて、トリガーキーの組み合わせを取得する
	 * カスタム設定の場合は、引数の組み合わせをそのまま返す
	 * 
	 * @param customKeyBean カスタム設定時に使用するキー組み合わせ
	 * @return トリガー設定に応じた KeyCombinationBean
	 */
	public KeyCombinationBean resolveKeyCombination(KeyCombinationBean customKeyBean) {
		switch (this) {
		case SHIFT_PRINTSCREEN:
			return defaultKeyCombination();
			
		case CTRL_SHIFT_S:
			return new KeyCombinationBean(VC_S, true, false, true);

		case F9:
			return new KeyCombinationBean(VC_F9, false, false, false);

		case CUSTOM:
			if (customKeyBean == null || customKeyBean.getTriggerKey() == VC_UNDEFINED) {
				return defaultKeyCombination();
			}
			return customKeyBean;

		default:
			return defaultKeyCombination();
		}
	}

	/**
	 * デフォルト設定のトリガーキーの組み合わせを取得する
	 * @return デフォルト設定である、Shift + Print Screenのキー組み合わせ
	 */
	private static KeyCombinationBean defaultKeyCombination() {
		return new KeyCombinationBean(VC_PRINTSCREEN, false, false, true);
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
		return BaseEnum.parseOrDefault(TriggerType.class, raw, SHIFT_PRINTSCREEN, PROPERTY_TRIGGER);
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
