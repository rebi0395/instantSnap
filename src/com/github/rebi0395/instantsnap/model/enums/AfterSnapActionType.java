package com.github.rebi0395.instantsnap.model.enums;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import javafx.util.StringConverter;

/**
 * 撮影後の挙動パターンを定義するクラス
 */
public enum AfterSnapActionType implements BaseEnum {

	/** 何もしない(デフォルト値) */
	NONE("0", "何もしない"),
	/** 画像を開く */
	OPEN_IMAGE("1", "画像を開く"),
	/** ペイントで開く */
	OPEN_PAINT("2", "ペイントで開く"),
	/** ペイントで開く */
	SAVE_TO_TMP("3", "一時保存フォルダに保存");

	/** プロパティファイルと紐つく値 */
	private final String value;
	/** 表示名 */
	private final String displayName;

	/** コンストラクタ */
	AfterSnapActionType(String value, String displayName) {
		this.value = value;
		this.displayName = displayName;
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
	 * @return AfterSnapActionType と文字列を交互変換するJavaFXコンバーター
	 */
	public static StringConverter<AfterSnapActionType> toConverter() {
		return BaseEnum.toConverter(AfterSnapActionType.class, NONE);
	}
}