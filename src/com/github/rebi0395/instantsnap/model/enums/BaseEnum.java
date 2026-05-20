package com.github.rebi0395.instantsnap.model.enums;

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
	 * プロパティファイルの記載を元に、列挙型から設定項目を取得する
	 * 入力値が不正の場合、nullを返却する
	 * @param value プロパティファイルの記載
	 * @return 設定項目
	 */
	static <E extends Enum<?> & BaseEnum> E fromValue(Class<E> enums, String value) {
    	if (value != null && !value.isBlank()) {
			value = value.strip(); // 左右空白除去
            for (E mode : enums.getEnumConstants()){
            	// アルファベットの大文字、小文字の差異は無視する
                if (mode.getValue().equalsIgnoreCase(value)) {
                	return mode;
                }
            }
		}
    	return null; // 不正値
    }
}
