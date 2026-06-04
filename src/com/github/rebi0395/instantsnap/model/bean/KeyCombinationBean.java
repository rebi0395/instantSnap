package com.github.rebi0395.instantsnap.model.bean;

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
