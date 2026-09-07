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
	 * @param args 起動引数。設定画面用の引数の解釈もAppへ委譲する
	 */
	public static void main(String[] args) {
		InstantSnapApp.launch(args);
	}
}
