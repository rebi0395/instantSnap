package com.github.rebi0395.instantsnap.common;

public class BooleanUtil {

	// インスタンス化しない
	private BooleanUtil() {}
	
	/**
	 * 文字列が"true"、もしくは "1" と記載がされているかをチェックします。
	 * @param str 文字列
	 * @return 判定結果 true: "true"、"1" と記載されている。false: されていない
	 */
	public static boolean isTrue (String str) {
		if (str == null) {
			return false;
		}
		str = str.strip().toLowerCase();
		return switch(str) {
			case "true", "1" -> true;
			default -> false;
		};
	}
	
}
