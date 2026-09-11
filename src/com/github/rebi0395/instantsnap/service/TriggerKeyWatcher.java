package com.github.rebi0395.instantsnap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;

import static com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.rebi0395.instantsnap.common.AppLogger;
import com.github.rebi0395.instantsnap.common.NativeKeyCodeConverter;
import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.listener.ExitKeyEventListener;
import com.github.rebi0395.instantsnap.listener.SnapKeyEventListener;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.KeyCombinationBean;
import com.github.rebi0395.instantsnap.model.enums.SnapRouteType;

/**
 * スクリーンショット撮影のトリガーとなるキーを監視し、
 * 撮影処理を呼び出すクラス
 * JNativeHookライブラリを使用。
 */
public class TriggerKeyWatcher implements NativeKeyListener, AutoCloseable {

	/** ユーザー設定値 */
	private ConfigBean config;
	/** 撮影と範囲選択のキー押下を通知するインターフェース */
	private final SnapKeyEventListener snapKeyEventListener;
	/** 終了キーの押下を通知するインターフェース */
	private final ExitKeyEventListener exitKeyEventListener;
	/** キー監視を行ってるかを判定する変数 trueで監視中 */
	private boolean keyListenerRunning = false;
	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(TriggerKeyWatcher.class);

	/** 撮影トリガーキーの押下フラグ */
	private boolean isSnapTriggerPressed = false;
	/** 範囲選択トリガーキーの押下フラグ */
	private boolean isRangeSelectionTriggerPressed = false;
	/** ALTキーの押下フラグ */
	private boolean isAltPressed = false;
	/** Ctrlキーの押下フラグ */
	private boolean isCtrlPressed = false;
	/** Shiftキーの押下フラグ */
	private boolean isShiftPressed = false;

	/** 撮影後、連写防止用のフラグ trueで撮影可能 */
	private boolean canSnap = true;
	/** 範囲選択画面表示フラグ trueで範囲選択画面が表示可能 */
	private boolean canRangeSelection = true;

	/**
	 * コンストラクタ
	 * @param config ユーザーの設定値
	 * @param snapKeyEventListener 撮影と範囲選択の通知先
	 * @param exitKeyEventListener 終了要求の通知先
	 */
	public TriggerKeyWatcher(
			ConfigBean config,
			SnapKeyEventListener snapKeyEventListener,
			ExitKeyEventListener exitKeyEventListener) {
		this.config = config;
		this.snapKeyEventListener = snapKeyEventListener;
		this.exitKeyEventListener = exitKeyEventListener;
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

		// traceの場合、キーログをコンソール表示
		AppLogger.keyPressed(e);

		// 右側のAlt,Ctrl,Shiftの拡張キーを正規化する
		int nativeKeyCode = NativeKeyCodeConverter.normalizeNativeKeyCode(e.getKeyCode());

		// pauseキー押下時、アプリを終了する
		if (nativeKeyCode == VC_PAUSE) {
			exitKeyEventListener.requestExit();
			return;
		}

		// 撮影キーのショートカットキーを取得
		KeyCombinationBean snapKeys = config.getSnapKeyBean();
		// 範囲選択キーのショートカットキーを取得
		KeyCombinationBean selectionKeys = config.getSelectionKeyBean();

		// 押下状態を更新する
		isSnapTriggerPressed = updateKeyStates(nativeKeyCode, true,
				snapKeys.getTriggerKey(), isSnapTriggerPressed);
		// 条件キーが完全一致で押されているかを判定し、かつ撮影可能であれば、撮影を行う
		if (canSnap && isShortcutPressed(isSnapTriggerPressed, snapKeys)) {
			// 連写を防ぐため、撮影可能フラグをオフにする
			canSnap = false;
			clearKeyStates();
			snapKeyEventListener.requestSnap(SnapRouteType.SHORTCUT_KEY);
			return;
		}

		// 押下状態を更新する
		isRangeSelectionTriggerPressed = updateKeyStates(nativeKeyCode, true,
				selectionKeys.getTriggerKey(), isRangeSelectionTriggerPressed);
		// 範囲選択キーが完全一致で押されているかを判定し、かつ画面表示が可能であれば、範囲選択画面を表示する。
		if (canRangeSelection && isShortcutPressed(isRangeSelectionTriggerPressed, selectionKeys)) {
			canRangeSelection = false; // 二重起動防止のため、フラグをオフにする
			clearKeyStates();
			snapKeyEventListener.requestRangeSelection();
		}
	}

	/**
	 * 特定のキーが離されたとき、押下フラグをオフに更新するメソッド
	 */
	@Override
	public void nativeKeyReleased(NativeKeyEvent e) {
		// 右側のAlt,Ctrl,Shiftの拡張キーを正規化する
		int nativeKeyCode = NativeKeyCodeConverter.normalizeNativeKeyCode(e.getKeyCode());

		// 撮影キーのショートカットキーを取得
		KeyCombinationBean keys = config.getSnapKeyBean();
		// 範囲選択キーのショートカットキーを取得
		KeyCombinationBean selectionKeys = config.getSelectionKeyBean();

		// 押下状態を更新し、撮影条件キーが完全一致で押されているかを判定する
		isSnapTriggerPressed = updateKeyStates(nativeKeyCode, false,
				keys.getTriggerKey(), isSnapTriggerPressed);
		if (!isShortcutPressed(isSnapTriggerPressed, keys)) {
			// キーの押下状況が一致していないとき、フラグをオンにし、撮影を可能にする
			canSnap = true;
		}

		// 押下状態を更新し、範囲選択条件キーが完全一致で押されているかを判定する
		isRangeSelectionTriggerPressed = updateKeyStates(nativeKeyCode, false,
				selectionKeys.getTriggerKey(), isRangeSelectionTriggerPressed);
		if (!isShortcutPressed(isRangeSelectionTriggerPressed, selectionKeys)) {
			// キーの押下状況が一致していないとき、フラグをオンにし、範囲選択を可能にする
			canRangeSelection = true;
		}
	}

	/**
	 * キーの押下状態を更新する
	 *
	 * @param nativeKeyCode キーコード
	 * @param setPressed 押下イベント呼び出しならtrue、離した場合はfalseを指定
	 * @param triggerKeys 設定されているショートカットキー
	 * @param triggerPressed トリガーキーの押下状態
	 * @return トリガーキーの押下状態
	 */
	private boolean updateKeyStates(int nativeKeyCode, boolean setPressed, int triggerKeys, boolean triggerPressed) {
		boolean isTriggerKeyPressed = triggerPressed;

		// トリガーキー押下フラグ
		// 未設定を表す0は、半角/全角キーなどの未定義イベントと一致させない。
		if (triggerKeys != VC_UNDEFINED && nativeKeyCode == triggerKeys) {
			isTriggerKeyPressed = setPressed;
		}
		// ALT押下フラグ
		if (nativeKeyCode == VC_ALT) {
			isAltPressed = setPressed;
		}
		// Ctrl押下フラグ
		if (nativeKeyCode == VC_CONTROL) {
			isCtrlPressed = setPressed;
		}
		// Shift押下フラグ
		if (nativeKeyCode == VC_SHIFT) {
			isShiftPressed = setPressed;
		}
		return isTriggerKeyPressed;
	}

	/**
	 * ショートカットキーが完全一致で押されているか判定する
	 *
	 * @param isTriggerKeyPressed トリガーキーの押下状態フィールド
	 * @param keys 設定されているショートカットキー
	 * @return 完全一致で押されている場合true
	 * 押されていないキーがある場合、余計な修飾キーが押されている場合はfalseを返す
	 */
	private boolean isShortcutPressed(boolean isTriggerKeyPressed, KeyCombinationBean keys) {
		// 未設定のショートカットは、キーコード0のイベントを含め常に不一致とする。
		if (!keys.isTriggerKeyDefined()) {
			return false;
		}
		return (keys.isUseAlt() == isAltPressed)
				&& (keys.isUseCtrl() == isCtrlPressed)
				&& (keys.isUseShift() == isShiftPressed)
				&& isTriggerKeyPressed;
	}

	/**
	 * キーの押下状態をリセットする
	 */
	private void clearKeyStates() {
		isSnapTriggerPressed = false;
		isRangeSelectionTriggerPressed = false;
		isAltPressed = false;
		isCtrlPressed = false;
		isShiftPressed = false;
	}

	/**
	 * キー監視が実行されているか、状態を取得します
	 * @return keyListenerRunning / true 監視中
	 */
	public boolean isKeyListenerRunning() {
		return keyListenerRunning;
	}

	/**
	 * キー監視を停止します。
	 * 終了処理全体を継続できるよう、停止失敗はログへ記録して呼び出し元へは送出しません。
	 */
	@Override
	public void close() {
		try {
			if (isKeyListenerRunning()) {
				deactivate();
			}
		} catch (ServiceException e) {
			log.error("トリガーキー監視の終了に失敗しました", e);
		}
	}
}
