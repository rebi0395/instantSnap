package com.github.rebi0395.instantsnap.model.enums;

/**
 * 撮影要求が発生した経路を表します。
 * 設定値ではなく、1回の撮影要求にだけ付与する実行時情報です。
 */
public enum SnapRouteType {

	/** グローバルショートカットからの撮影 */
	SHORTCUT_KEY,

	/** メインウィンドウの撮影ボタンからの撮影 */
	MAIN_BUTTON,

	/** 範囲確定後の自動撮影 */
	RANGE_MODE;

	/**
	 * メインウィンドウのボタンから要求された撮影かを判定します。
	 *
	 * @return メインウィンドウの撮影ボタンからの場合true
	 */
	public boolean isFromMainWindow() {
		return this == MAIN_BUTTON;
	}
}
