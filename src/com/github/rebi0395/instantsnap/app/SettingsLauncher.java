package com.github.rebi0395.instantsnap.app;

import javafx.application.Application;

/**
 * 子プロセス内でJavaFXランタイムを起動し、SettingWindowAppへ委譲する入口です。
 * 常駐側のMainとは分けて、設定画面を開くときだけJavaFXを初期化します。
 */
public final class SettingsLauncher {

	/**
	 * インスタンス化を禁止します。
	 */
	private SettingsLauncher() {
	}

	/**
	 * JavaFX設定画面を起動します。
	 *
	 * @param args JavaFXランタイムへ引き継ぐコマンドライン引数
	 */
	public static void main(String[] args) {
		Application.launch(SettingWindowApp.class, args);
	}
}
