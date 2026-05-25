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
    private ConfigBean configBean;
    /** キー押下をコントローラーに通知するインターフェース */
    private KeyEventListener listener;
    /** ロガー */
	private static final Logger log = LoggerFactory.getLogger(TriggerKeyManager.class);

    /**
     * コンストラクタ
     * @param configBean ユーザー設定値
     * @param listener イベントの通知先
     */
    public TriggerKeyManager(ConfigBean configBean, KeyEventListener listener) {
        this.configBean = configBean;
        this.listener = listener;
    }

    /**
     * キー監視を開始する
     * @throws ServiceException 
     */
    public void activate() throws ServiceException {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
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
        try {
            GlobalScreen.unregisterNativeHook();
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
        	if ((PRINTSCREEN.equals(configBean.getTrigger()) && !isShiftPressed) || 
        		(SHIFT_PRINTSCREEN.equals(configBean.getTrigger()) && isShiftPressed)) {
        		// 撮影トリガーが入力されたことを通知する
        		listener.requestSnap();
        	}
        }
        // pauseキー押下時、アプリを終了する
        if (e.getKeyCode() == NativeKeyEvent.VC_PAUSE) {
        	listener.requestExit();;
        }
    }
}