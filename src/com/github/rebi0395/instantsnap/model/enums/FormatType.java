package com.github.rebi0395.instantsnap.model.enums;
import static com.github.rebi0395.instantsnap.common.AppConstant.*;

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
	 * @param raw プロパティファイルと紐つく値
	 * @return 対応する FormatType、または不正値の場合は null
	 */
	public static FormatType parseValue(String raw) {
		return BaseEnum.parseValue(FormatType.class, raw);
	}
	
	/**
	 * プロパティファイルの値から対応する FormatType を取得する。
	 * 不正な値が指定された場合は デフォルト値を返し、ログを出力する。
	 * @param raw プロパティファイルと紐つく値
	 * @return 対応する FormatType、またはデフォルトの FormatType
	 */
	public static FormatType parseOrDefault(String raw) {
		return BaseEnum.parseOrDefault(FormatType.class, raw, PNG, FORMAT);
	}
}