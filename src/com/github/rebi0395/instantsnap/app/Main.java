package com.github.rebi0395.instantsnap.app;

import com.github.rebi0395.instantsnap.common.DpiUtil;

/**
 * 起動クラス
 */
public class Main {
	
	/**
	 * 起動メソッド
	 * @param args
	 */
	public static void main(String[] args) {
		DpiUtil.enablePerMonitorV2(); // DPIスケーリングの対応
		InstantSnapApp.launch(InstantSnapApp.class, args); // JavaFXの起動
	}
}
