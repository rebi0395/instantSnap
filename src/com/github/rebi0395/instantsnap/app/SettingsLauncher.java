package com.github.rebi0395.instantsnap.app;

import javafx.application.Application;

/**
 * JavaFX設定画面を子プロセスとして起動するクラス
 */
public final class SettingsLauncher {

	/** インスタンス化を禁止します。 */
	private SettingsLauncher() {
	}

	/**
	 * JavaFX設定画面を起動します。
	 *
	 * @param args コマンドライン引数
	 */
	public static void main(String[] args) {
		Application.launch(SettingWindowApp.class, args);
	}
}
