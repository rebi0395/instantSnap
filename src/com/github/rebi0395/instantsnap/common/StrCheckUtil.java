package com.github.rebi0395.instantsnap.common;

import java.util.List;
import java.util.Locale;

/**
 * 文字列のnull対策、空白除去等、文字列を安全に扱うための共通クラス
 */
public class StrCheckUtil {

	/** Windowsで使用できない予約名 */
	public static final List<String> WINDOWS_RESERVED_NAMES = List.of(
			"CON", "PRN", "AUX", "NUL",
			"COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
			"LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9");

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
	 * 文字列がWindowsの予約語であるかを判定します
	 * @param str 文字列
	 * @return 予約語
	 */
	public static boolean isWindowsReservedNames(String str) {
		if (isBlankSafe(str)) {
			return false;
		}
		
		// 大文字小文字を区別しなくする
		String upperStr = str.strip().toUpperCase(Locale.ROOT);
		
		if (WINDOWS_RESERVED_NAMES.contains(upperStr)) {
			// Windowsの予約語が含まれていれば true
			return true;
		}
		return false;
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
	 * 文字列の真ん中部分を省略表記する
	 * @param str 文字列
	 * @param maxlength 表示する最大文字数
	 * @return 真ん中省略がされた文字数
	 */
	public static String compressMiddle(String str, int maxlength) {
		String ellipsis = "...";
		// null 空文字は早期リターン
		if (isBlankSafe(str)) {
			return "";
		}
		// 文字列が指定文字数以下の場合、文字列をそのまま返却
		if (str.length() <= maxlength) {
			return str;
		}
		// 三点リーダーよりも最大文字数の指定が短い場合
		if (maxlength <= ellipsis.length()) {
			return ellipsis.substring(0, maxlength);
		}

		int length = str.length(); // 文字列の文字数
		int keeplength = maxlength - ellipsis.length(); // 三点リーダーを含めた残せる文字数
		int leftlength = keeplength / 2; // 左側の長さ
		int rightlength = keeplength - leftlength; // 右側の長さ

		String leftStr = str.substring(0, leftlength);
		String rightStr = str.substring(length - rightlength);

		StringBuilder builder = new StringBuilder();
		builder.append(leftStr);
		builder.append(ellipsis);
		builder.append(rightStr);

		return builder.toString();
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
		String sanitizedFileName = builder.toString();
		if (isWindowsReservedNames(sanitizedFileName)) {
			return sanitizedFileName + "■"; // 予約語に文字を加え返却
		}
		return sanitizedFileName;
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
