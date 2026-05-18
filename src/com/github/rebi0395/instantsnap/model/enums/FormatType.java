package com.github.rebi0395.instantsnap.model.enums;

/**
 * 画像の拡張子を列挙するクラス
 */
public enum FormatType {
	PNG("png"),
	JPG("jpg", "jpeg"),
	BMP("bmp"),
	GIF("gif");

	private final String[] formats; 
	
	FormatType(String... format) {
		this.formats = format;
	}

	/**
	 * 拡張子の正式名称を文字列型で取得する
	 */
	 @Override
	public String toString() {
		return formats[0];
	}

	/**
	 * プロパティファイルの記載を元に、拡張子を取得する
	 * 入力値が不正の場合、nullを返却する
	 * @param value プロパティファイルの記載
	 * @return type
	 */
	public static FormatType fromString(String value) {
		if (value != null && !value.isBlank()) {
			String trimValue = value.strip(); // 左右空白除去
		    for (FormatType type : values()) {
		    	for (String formats : type.formats) {
			    	// 大文字、小文字は区別しないで判定
			        if (formats.equalsIgnoreCase(trimValue)) {
			            return type;
			        }
		    	}
		    }
		}
	    return null; // 不正値
	}
}
