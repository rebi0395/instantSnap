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
	
	/** 未定義のキー設定を作成するコンストラクタ */
	public KeyCombinationBean() {
		this(VC_UNDEFINED, false, false, false);
	}

	/**
	 * 現在の設定しているキー設定を文字列で取得する
	 * @return キー設定の文字列(例：Shift + F10)
	 */
	public String resolveKeyCombinationName() {
		if (triggerKey == VC_UNDEFINED) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		// 修飾キーを用いるとき、文字列を結合する
		if (useCtrl) {
			builder.append("Ctrl + ");
		}
		if (useAlt) {
			builder.append("Alt + ");
		}
		if (useShift) {
			builder.append("Shift + ");
		}
		// nativeキーコードからJavaFXのキー名を取得する
		builder.append(NativeKeyCodeConverter.toFXKeyName(triggerKey));
		return builder.toString();
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
