package com.github.rebi0395.instantsnap.common;

/**
 * 定数管理クラス
 */
public final class AppConstant {
	
	// インスタンス化しない
	private AppConstant(){}
	
	// プロパティファイルのパス
	public static final String PROPERTIES_PASS = "config.properties";
	// プロパティの項目名 
	public static final String SAVE_PATH = "savePath";
	public static final String SNAP_MODE = "snapMode";
	public static final String FORMAT = "format";
	public static final String TRIGGER = "trigger";
	public static final String OPENMODE = "openMode";
	// デフォルト値
	public static final String DEFAULT_SAVE_PATH = "./screenshots/";
	// 撮影トリガーキー
	public static final String SHIFT_PRINTSCREEN = "1";
	public static final String PRINTSCREEN = "2";
}
