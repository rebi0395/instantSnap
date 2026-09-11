package com.github.rebi0395.instantsnap.listener;

/**
 * グローバルショートカットによるアプリ終了要求を受け取るリスナーです。
 */
public interface ExitKeyEventListener {

	/**
	 * 終了キーが押されたことを通知します。
	 */
	void requestExit();
}
