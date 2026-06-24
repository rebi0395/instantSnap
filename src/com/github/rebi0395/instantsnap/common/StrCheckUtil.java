package com.github.rebi0395.instantsnap.common;

import java.util.Locale;

/**
 * 文字列のnull対策、空白除去等、文字列を安全に扱うための共通クラス
 */
public class StrCheckUtil {

	// インスタンス化しない
	private StrCheckUtil() {
	}

	/**
	 * 空白除去を行います。null時は空文字列を返却します
	 */
	public static String stripSafe(String str) {
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
		return !isBlankSafe(str);
	}

	/**
	 * 文字列が"true", "1", "on" と記載がされているかをチェックします。
	 * @param str 文字列
	 * @return 判定結果 true: "true", "1", "on" と記載されている。false: されていない
	 */
	public static boolean isStrTrue(String str) {
		if (isBlankSafe(str)) {
			return false;
		}
		str = str.strip().toLowerCase();
		switch (str) {
		case "true", "1", "on":
			return true;
		default:
			return false;
		}
	}

	/**
	 * 文字列が"false", "0", "off"と記載がされているかをチェックします。
	 * @param str 文字列
	 * @return 判定結果 true: "false", "0", "off"と記載されている。false: されていない
	 */
	public static boolean isStrFalse(String str) {
		if (isBlankSafe(str)) {
			return false;
		}
		str = str.strip().toLowerCase(Locale.ROOT);
		switch (str) {
		case "false", "0", "off":
			return true;
		default:
			return false;
		}
	}

	/**
	 * プロパティファイルで安全に書き込める値に変換する
	 * @param value 書き込む値
	 * @return エスケープ後の値
	 */
	public static String escapePropertyValue(String value) {
		// 空、nullなら早期リターン
		if (isBlankSafe(value)) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i); // 一文字ずつ切り抜く

			switch (ch) {
			case '\n':
			case '\r':
				// 値での改行はレイアウトが崩れるため無効化
				break;

			case '\t':
				// タブは半角スペースに変換
				builder.append(" ");
				break;

			case '\\':
				// Windowsパス対策
				builder.append("\\\\");
				break;

			default:
				// 制御文字を除外
				if (Character.isISOControl(ch)) {
					break;
				}
				builder.append(ch);
			}
		}
		return builder.toString();
	}

	/**
	 * ファイル禁止文字を■に置換する
	 * @param fileName ユーザー入力のファイル名先頭部分
	 * @return 置換後のファイル名先頭部分
	 */
	public static String sanitizeFileNamePrefix(String fileName) {
		if (isBlankSafe(fileName)) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < fileName.length(); i++) {
			char ch = fileName.charAt(i); // 一文字ずつ切り抜く

			switch (ch) {
			case '\\':
			case '/':
			case ':':
			case '*':
			case '?':
			case '"':
			case '<':
			case '>':
			case '|':
				builder.append("■");
				break;

			default:
				if (Character.isISOControl(ch)) {
					builder.append("■");
					break;
				}

				builder.append(ch);
				break;
			}
		}
		return builder.toString();
	}

	/**
	 * booleanプロパティを安全に読み取る
	 * 
	 * @param properties プロパティ
	 * @param key プロパティのキー
	 * @param defaultValue デフォルト値
	 * @return 読み取り結果
	 */
	public static boolean readBooleanOrDefault(String value, boolean defaultValue) {

		// 空、nullは指定したデフォルト値返却
		if (isBlankSafe(value)) {
			return defaultValue;
		}
		//	"true", "1", "on" ならtrue
		if (isStrTrue(value)) {
			return true;
		}
		// "false", "0", "off" ならfalse
		if (isStrFalse(value)) {
			return false;
		}

		return defaultValue; // 指定したデフォルト値
	}

}
