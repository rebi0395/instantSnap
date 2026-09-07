package com.github.rebi0395.instantsnap.app;

/**
 * InstantSnapの起動クラスです。
 * <p>
 * 起動後の初期化と画面制御はInstantSnapAppへ委譲します。
 * </p>
 */
public final class Main {

	/**
	 * インスタンス化を禁止します。
	 */
	private Main() {
	}

	/**
	 * アプリケーションを起動します。
	 *
	 * @param args コマンドライン引数（現在は使用しない）
	 */
	public static void main(String[] args) {
		InstantSnapApp.launch();
	}
}
