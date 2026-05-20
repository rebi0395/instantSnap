package com.github.rebi0395.instantsnap.model.enums;

/**
 * 撮影後の挙動パターンを定義するクラス
 */
public enum SnapAfter implements BaseEnum {

	/** 何もしない(デフォルト値) */
	NOTHING("1", "Nothing"),
	/** 保存先フォルダを開く */
	OPEN_DIR("2", "OpenDirectory"),
	/** 保存した画像ファイルを開く */
	OPEN_IMAGE("3", "OpenFile");

	/** プロパティファイルと紐つく値 */
	private final String value;
	/** 表示名 */
	private final String displayName;

	/** コンストラクタ */
	SnapAfter(String value, String displayName) {
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
	 * プロパティファイルの値から対応する SnapAfter を取得する。
	 * 不正な値が指定された場合は null を返す。
	 *
	 * @param value プロパティファイルと紐つく値
	 * @return 対応する SnapAfter、または不正値の場合は null
	 */
	public static SnapAfter fromValue(String value) {
		return BaseEnum.fromValue(SnapAfter.class, value);
	}
}
