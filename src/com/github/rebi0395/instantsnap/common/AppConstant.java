package com.github.rebi0395.instantsnap.common;

/**
 * 定数管理クラス
 */
public final class AppConstant {
	
	// インスタンス化しない
	private AppConstant(){}
	
	// プロパティの項目名 
	public static final String SAVE_PATH = "savePath";
	public static final String SNAP_AREA = "snapArea";
	public static final String FORMAT = "format";
	public static final String TRIGGER = "trigger";
	public static final String SNAP_AFTER = "snapAfter";
	// デフォルト値
	public static final String DEFAULT_SAVE_PATH = "./screenshots/";
	// 撮影トリガーキー
	public static final String SHIFT_PRINTSCREEN = "1";
	public static final String PRINTSCREEN = "2";
}
