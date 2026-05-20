package com.github.rebi0395.instantsnap.model.enums;

/**
 * 画像の拡張子を列挙するクラス
 */
public enum FormatType implements BaseEnum {

	PNG("png", "png"),
	JPG("jpg", "jpg"),
	BMP("bmp", "bmp"),
	GIF("gif", "gif");

	/** プロパティファイルと紐つく値 */
	private final String value;
	/** 表示名 */
	private final String displayName;

	/** コンストラクタ */
	FormatType(String value, String displayName) {
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
	 * プロパティファイルの値から対応する FormatType を取得する。
	 * 不正な値が指定された場合は null を返す。
	 *
	 * @param value プロパティファイルと紐つく値
	 * @return 対応する FormatType、または不正値の場合は null
	 */
	public static FormatType fromValue(String value) {
		return BaseEnum.fromValue(FormatType.class, value);
	}
}
