package com.github.rebi0395.instantsnap.model;

public enum SnapMode {
	ACTIVE_WINDOW("1","activeWindow"),
	ACTIVE_SCREEN("2","activeScreen"),
	FULL_SCREEN("3","fullScreen");

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
