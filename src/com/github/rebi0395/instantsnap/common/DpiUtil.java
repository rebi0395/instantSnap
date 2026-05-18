package com.github.rebi0395.instantsnap.common;

import com.sun.jna.Function;
import com.sun.jna.Pointer;

/**
 * Windows環境における高DPI（拡大率）の設定を制御するクラス
 */
public class DpiUtil {

	// モニターごとに異なるDPI（拡大率）をプロセス全体に強制適用する設定値
	private static final Pointer DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2 = new Pointer(-4);
	// 初期化有無の変数
	private static boolean initialized = false;
	
	/**
	 * アプリ起動時に呼び出し、モニターごとに異なるDPI（拡大率）を
	 * プロセス全体に強制適用するように初期化（マルチモニターでの座標バグ防止）
	 */
	public static synchronized void enablePerMonitorV2() {
		// 二重Dpi宣言防止
		if (initialized) {
			return;
		}
		try {
			initialized = true; // 初期化済にする
			// User32.dll から直接関数を呼び出す
			Function.getFunction("user32", "SetProcessDpiAwarenessContext")
            	.invoke(new Object[]{ DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2 });
			System.out.println("DPI Awareness: Per-Monitor V2 enabled");
		} catch (Throwable t) {
			System.out.println("DPI Awareness setting failed: " + t.getMessage());
		}
	}
}
