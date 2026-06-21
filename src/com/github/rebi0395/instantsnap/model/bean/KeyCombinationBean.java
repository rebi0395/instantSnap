package com.github.rebi0395.instantsnap.model.bean;

import static com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*;

import com.github.rebi0395.instantsnap.common.NativeKeyCodeConverter;

/**
 * 設定した起動キーを保持するクラスです。
 */
public class KeyCombinationBean {

	/** 組み合わせの中心となるキー Nativeキーコードを持つ */
	private int triggerKey;
	/** Ctrlを使用するかの真偽値 trueで使用する */
	private boolean useCtrl;
	/** Altを使用するかの真偽値 trueで使用する */
	private boolean useAlt;
	/** Shiftを使用するかの真偽値 trueで使用する */
	private boolean useShift;

	/** コンストラクタ */
	public KeyCombinationBean(int triggerKey, boolean useCtrl, boolean useAlt, boolean useShift) {
		this.triggerKey = triggerKey;
		this.useCtrl = useCtrl;
		this.useAlt = useAlt;
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
		if (useCtrl) {
			blilder.append("Ctrl + ");
		}
		if (useAlt) {
			blilder.append("Alt + ");
		}
		if (useShift) {
			blilder.append("Shift + ");
		}
		// 英語のキー名を取るために、JavaFXのキーコードからトリガーキー名を取得する
		blilder.append(NativeKeyCodeConverter.toFXKeyCode(triggerKey).getName());
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
