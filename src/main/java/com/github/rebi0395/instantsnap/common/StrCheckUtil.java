package com.github.rebi0395.instantsnap.common;

/**
 * 文字列のnull対策、空白除去等、安全に扱うための共通クラスです
 */
public class StrCheckUtil {

	// インスタンス化しない
	private StrCheckUtil() {}
	
	/**
	 * 空白除去を行います。null時は空文字列を返却します
	 */
	public static String stripSafe (String str) {
		if (str == null) {
			return "";
		}
		return str.strip();
	}

	/**
	 * 空白であるか、安全にチェックします
	 * @param str 文字列
	 * @return 空白、nullならtrue / 文字があればfalse
	 */
	public static boolean isBlankSafe(String str) {
		if (str == null) {
			return true;
		} 
		return str.isBlank();
	}
	
	/**
	 * 空白でないことを、安全にチェックします
	 * @param str 文字列
	 * @return 文字があれば true / 空白やnullでfalse
	 */
	public static boolean isNotBlankSafe(String str) {
		if (str == null) {
			return false;
		} 
		return !isBlankSafe(str);
	}
	
	/**
	 * 文字列が"true"、もしくは "1" と記載がされているかをチェックします。
	 * @param str 文字列
	 * @return 判定結果 true: "true"、"1" と記載されている。false: されていない
	 */
	public static boolean isStrTrue (String str) {
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
