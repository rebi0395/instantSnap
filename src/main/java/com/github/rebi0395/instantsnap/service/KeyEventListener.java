package com.github.rebi0395.instantsnap.service;

/**
 * キー押下を通知するインタフェースです
 */
public interface KeyEventListener {

	/** 撮影キーが押された事を通知する */
	void requestSnap();
	/** アプリの終了を通知する */
	void requestExit();
}
