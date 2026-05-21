package com.github.rebi0395.instantsnap.model.enums;

import com.github.rebi0395.instantsnap.common.AppLogger;

/**
 * Enum型の土台となるインターフェース
 */
public interface BaseEnum {

	/**
	 * 設定ファイルと紐付く値を取得する
	 * @return プロパティファイルと紐付く値
	 */
	String getValue();

	/**
	 * 表示名を取得する
	 * @return 表示名
	 */
	String getDisplayName();

	/**
	 * プロパティファイルの値から最適なenumの型を返却する
	 * 不正値ならデフォルト適応と、ログ出力を行う
	 * @param <E> Enum型
	 * @param type Enumの列挙値
	 * @param raw プロパティファイルの値
	 * @param defaultValue Enumのデフォルト値
	 * @param log ロガー
	 * @param configName プロパティファイルの項目名
	 * @return プロパティファイルの値を元に適応した設定、不正値ならデフォルト設定
	 */
	static <E extends Enum<?> & BaseEnum> E parseOrDefault(
	        Class<E> type,
	        String raw,
	        E defaultValue,
	        String configName
	) {
	    E value = parseValue(type, raw);
	    if (value != null) return value;

	    AppLogger.warn("設定項目「{}」の値({})が不正のため、デフォルト値 {} を適用します",
	            configName, raw, defaultValue.getDisplayName());
	    return defaultValue;
	}
	
	/**
	 * プロパティファイルの記載を元に、列挙型から設定項目を取得する
	 * 入力値が不正の場合、nullを返却する
	 * @param value プロパティファイルの記載
	 * @return 設定項目
	 */
	static <E extends Enum<?> & BaseEnum> E parseValue(Class<E> enums, String value) {
		if (value != null && !value.isBlank()) {
			value = value.strip(); // 左右空白除去
			for (E mode : enums.getEnumConstants()) {
				// アルファベットの大文字、小文字の差異は無視する
				if (mode.getValue().equalsIgnoreCase(value)) {
					return mode;
				}
			}
		}
		return null; // 不正値
	}
}
