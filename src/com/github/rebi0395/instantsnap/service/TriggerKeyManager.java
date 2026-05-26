package com.github.rebi0395.instantsnap.service;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;

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
    private boolean isKeyListenerRunning = false;
    /** ロガー */
	private static final Logger log = LoggerFactory.getLogger(TriggerKeyManager.class);

    /**
     * コンストラクタ
     * @param listener イベントの通知先
     */
    public TriggerKeyManager(KeyEventListener listener) {
        this.listener = listener;
    }

    /**
     * キー監視を開始する
     * @param config ユーザーの設定値
     * @throws ServiceException 
     */
    public void activate(ConfigBean config) throws ServiceException {
        if (isKeyListenerRunning) {
            log.warn("キー監視はすでに開始されています");
            return;
        }
    	this.config = config; // ユーザーの設定値を更新
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            isKeyListenerRunning = true;
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
        if (!isKeyListenerRunning) {
            log.warn("キー監視は現在されていません");
            return;
        }
        
        try {
            GlobalScreen.unregisterNativeHook();
            isKeyListenerRunning = false;
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
        // PrintScreenキーが押下時
        if (e.getKeyCode() == NativeKeyEvent.VC_PRINTSCREEN) {
        	// ビット演算でshiftキーの状態ビットだけを抜き出し判定する
        	boolean isShiftPressed = (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0;
        	// shiftキーが押されているかで撮影可否を判定する
        	if ((PRINTSCREEN.equals(config.getTrigger()) && !isShiftPressed) || 
        		(SHIFT_PRINTSCREEN.equals(config.getTrigger()) && isShiftPressed)) {
        		// 撮影トリガーが入力されたことを通知する
        		listener.requestSnap();
        	}
        }
        // pauseキー押下時、アプリを終了する
        if (e.getKeyCode() == NativeKeyEvent.VC_PAUSE) {
        	listener.requestExit();
        }
    }

	/**
	 * キー監視が実行されているか、状態を取得します
	 * @return isKeyListenerRunning / true 監視中
	 */
	public boolean isKeyListenerRunning() {
		return isKeyListenerRunning;
	}
}