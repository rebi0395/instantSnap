package com.github.rebi0395.instantsnap;

import com.github.rebi0395.instantsnap.common.DpiUtil;
import com.github.rebi0395.instantsnap.controller.MainController;

/**
 * 起動クラス
 */
public class Main {

	/**
	 * 起動メソッド
	 * @param args
	 */
	public static void main(String[] args) {
		DpiUtil.enablePerMonitorV2();
		MainController controller = new MainController();
		controller.init();
	}
}
