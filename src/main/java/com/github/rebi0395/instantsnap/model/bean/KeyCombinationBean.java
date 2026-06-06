package com.github.rebi0395.instantsnap.model.bean;

import static com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*;

/**
 * 設定した起動キーを保持するクラスです。
 */
public class KeyCombinationBean {

	/** 組み合わせの中心となるキー Nativeキーコードを持つ */
	private int triggerKey;
	/** Altを使用するかの真偽値 trueで使用する */
	private boolean useAlt;
	/** Ctrlを使用するかの真偽値 trueで使用する */
	private boolean useCtrl;
	/** Shiftを使用するかの真偽値 trueで使用する */
	private boolean useShift;

	/** コンストラクタ */
	public KeyCombinationBean(int triggerKey, boolean useAlt, boolean useCtrl, boolean useShift) {
		this.triggerKey = triggerKey;
		this.useAlt = useAlt;
		this.useCtrl = useCtrl;
		this.useShift = useShift;
	}

	/**
	 * 現在の設定しているキー設定を文字列で取得する
	 * @return キー設定の文字列(例：Shift + F10)
	 */
	public String resolveKeyCombinationName() {
		if (triggerKey == VC_UNDEFINED) {
			return "";
		}
		StringBuilder blilder = new StringBuilder();

		// 修飾キーを用いるとき、文字列を結合する
		if (useAlt) blilder.append("Alt + ");
		if (useCtrl) blilder.append("Ctrl + ");
		if (useShift) blilder.append("Shift + ");
		blilder.append(getKeyText(triggerKey));

		return blilder.toString();
	}

	/**
	 * triggerKey を取得する
	 * @return triggerKey
	 */
	public int getTriggerKey() {
		return triggerKey;
	}

	/**
	 * triggerKey をセットする
	 * @param triggerKey
	 */
	public void setTriggerKey(int triggerKey) {
		this.triggerKey = triggerKey;
	}

	/**
	 * useAlt を取得する
	 * @return useAlt
	 */
	public boolean isUseAlt() {
		return useAlt;
	}

	/**
	 * useAlt をセットする
	 * @param useAlt
	 */
	public void setUseAlt(boolean useAlt) {
		this.useAlt = useAlt;
	}

	/**
	 * useCtrl を取得する
	 * @return useCtrl
	 */
	public boolean isUseCtrl() {
		return useCtrl;
	}

	/**
	 * useCtrl をセットする
	 * @param useCtrl
	 */
	public void setUseCtrl(boolean useCtrl) {
		this.useCtrl = useCtrl;
	}

	/**
	 * useShift を取得する
	 * @return useShift
	 */
	public boolean isUseShift() {
		return useShift;
	}

	/**
	 * useShift をセットする
	 * @param useShift
	 */
	public void setUseShift(boolean useShift) {
		this.useShift = useShift;
	}

}
