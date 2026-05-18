package com.github.rebi0395.instantsnap.model.enums;

/**
 * 撮影モードを指定するクラス
 */
public enum SnapMode {
	ACTIVE_WINDOW("1","activeWindow"),
	ACTIVE_SCREEN("2","activeScreen"),
	MAIN_SCREEN("3","mainScreen"),
	FULL_SCREEN("4","fullScreen");

	private String code;
	private String displayName;

	SnapMode(String code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	/**
	 * @return code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * プロパティファイルの記載を元に、撮影モードを取得する
	 * 入力値が不正の場合、nullを返却する
	 * @param code
	 * @return
	 */
    public static SnapMode fromCode(String code) {
    	if (code != null && !code.isBlank()) {
			code.strip(); // 左右空白除去
            for (SnapMode mode : values()) {
                if (mode.code.equals(code)) {
                	return mode;
                }
            }
		}
    	return null; // 不正値
    }
}
