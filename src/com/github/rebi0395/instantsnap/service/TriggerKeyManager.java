package com.github.rebi0395.instantsnap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.NativeInputEvent;

import static com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.KeyCombinationBean;

/**
 * スクリーンショット撮影のトリガーとなるキーを監視し、
 * 撮影処理を呼び出すクラス
 * JNativeHookライブラリを使用。
 */
public class TriggerKeyManager implements NativeKeyListener {

	/** ユーザー設定値 */
	private ConfigBean config;
	/** キー押下をコントローラーに通知するインターフェース */
	private KeyEventListener listener;
	/** キー監視を行ってるかを判定する変数 trueで監視中 */
	private boolean keyListenerRunning = false;
	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(TriggerKeyManager.class);

	/** ALTキーの押下フラグ */
	boolean isAltPressed = false;
	/** ALTキーの押下フラグ */
	boolean isCtrlPressed = false;
	/** ALTキーの押下フラグ */
	boolean isShiftPressed = false;
	/** 撮影後、連写防止用のフラグ trueで撮影を禁止する */
	private boolean hasTriggeredSnap = false;

	/**
	 * コンストラクタ
	 * @param listener イベントの通知先
	 */
	public TriggerKeyManager(ConfigBean config, KeyEventListener listener) {
		this.config = config;
		this.listener = listener;
	}

	/**
	 * キー監視を開始する
	 * @param config ユーザーの設定値
	 * @throws ServiceException 
	 */
	public void activate() throws ServiceException {
		if (keyListenerRunning) {
			log.warn("キー監視はすでに開始されています");
			return;
		}
		try {
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(this);
			keyListenerRunning = true;
			log.info("キー監視開始");
		} catch (NativeHookException ne) {
			throw new ServiceException("キー監視の開始に失敗しました", ne);
		}
	}

	/**
	 * キー監視を停止する
	 * @throws ServiceException 
	 */
	public void deactivate() throws ServiceException {
		if (!keyListenerRunning) {
			log.warn("キー監視は現在稼働していません");
			return;
		}

		try {
			GlobalScreen.unregisterNativeHook();
			keyListenerRunning = false;
			log.info("キー監視停止");
		} catch (NativeHookException nhe) {
			throw new ServiceException("キー監視の停止に失敗しました", nhe);
		}
	}

	/**
	 * 設定したトリガーキーの押下をイベントリスナーに通知する
	 */
	@Override
	public void nativeKeyPressed(NativeKeyEvent e) {

		// pauseキー押下時、アプリを終了する
		if (e.getKeyCode() == VC_PAUSE) {
			listener.requestExit();
		}

		// ALT押下フラグ
		if (e.getKeyCode() == VC_ALT) {
			isAltPressed = true;
		}
		// Ctrl押下フラグ
		if (e.getKeyCode() == VC_CONTROL) {
			isCtrlPressed = true;
		}
		// Shift押下フラグ
		if ((e.getModifiers() & NativeInputEvent.SHIFT_MASK) != 0) {
			isShiftPressed = true;
		}
		
		// 撮影キーのショートカットキーを取得
		KeyCombinationBean keys = config.getTriggerType().getKeyCombination(config.getCustomKeyBean());
		
		// 押下状態を完全判定で判定する。一致でtrue、不要な修飾キーが押下されていればfalseとなる
		boolean isModifierKeyPressed = (keys.isUseAlt() == isAltPressed)
									&& (keys.isUseCtrl() == isCtrlPressed)
									&& (keys.isUseShift() == isShiftPressed);
		
		// トリガーキーと修飾キーで判定を行う
		if (e.getKeyCode() == keys.getTriggerKey() && isModifierKeyPressed) {
			if (!hasTriggeredSnap) {
				// 連写を防ぐためのフラグをオンにする
				hasTriggeredSnap = true;
				listener.requestSnap();
			}
		}
	}

	/**
	 * 特定のキーが離されたとき、押下フラグをオフに更新するメソッド
	 */
	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
	    if (e.getKeyCode() == VC_ALT) isAltPressed = false;
	    if (e.getKeyCode() == VC_CONTROL) isCtrlPressed = false;
	    if (e.getKeyCode() == VC_SHIFT) isShiftPressed = false;
	    
	    int key = config.getTriggerType().getKeyCombination(config.getCustomKeyBean()).getTriggerKey();
	    if (e.getKeyCode() == key) {
	    	// フラグをオフにし、撮影を可能にする
	    	hasTriggeredSnap = false;
	    }
	}

	/**
	 * キー監視が実行されているか、状態を取得します
	 * @return keyListenerRunning / true 監視中
	 */
	public boolean isKeyListenerRunning() {
		return keyListenerRunning;
	}
}