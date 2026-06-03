package com.github.rebi0395.instantsnap.model.bean;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;
import static com.github.rebi0395.instantsnap.common.StrCheckUtil.*;

import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.SnapAfter;
import com.github.rebi0395.instantsnap.model.enums.SnapArea;
import com.github.rebi0395.instantsnap.model.enums.TriggerType;

/**
 * 設定値保管クラス。セット時に値の安全性も保証する
 */
public class ConfigBean {

	/** 画像の保存パス */
	private String savePath;
	/** 撮影キー */
	private TriggerType triggerType;
	/** 撮影範囲 */
	private SnapArea snapArea;
	/** 拡張子 */
	private FormatType formatType;
	/** 撮影後設定 */
	private SnapAfter snapAfter;
	/** クリップボードに画像に保存する (trueで保存する) */
	private boolean isCopyToClipboard;
	/** カスタムキーの保存配列 */
	private int[] customKeyCodes;
	
	/**
	 * コンストラクタ。デフォルト値を設定する
	 */
	public ConfigBean() {
		this.savePath = DEFAULT_SAVE_PATH;
		this.triggerType = TriggerType.SHIFT_PRINTSCREEN;
		this.snapArea = SnapArea.ACTIVE_WINDOW;
		this.formatType = FormatType.PNG;
		this.snapAfter = SnapAfter.NOTHING;
		this.isCopyToClipboard = true;
	}

	/**
	 * スクショの保存先パスを取得する。
	 * @return savePath スクショの保存先パス
	 */
	public String getSavePath() {
		return savePath;
	}

	/**
	 * \の置換処理、前後トリム処理を行い、savePathをセットする
	 * 想定外の値やnull時には、デフォルトパスをセットする
	 * @param savePath スクショの保存先パス
	 */
	public void setSavePath(String savePath) {
		if (isBlankSafe(savePath)) {
			this.savePath = DEFAULT_SAVE_PATH;
		} else {
			this.savePath = savePath.strip().replace("\\", "/");
		}
	}

	/**
	 * 撮影ショートカットキーを取得する
	 * @return triggerType
	 */
	public TriggerType getTriggerType() {
		return triggerType;
	}

	/**
	 * 撮影ショートカットキーをセットする
	 * @param triggerType セットする
	 */
	public void setTriggerType(TriggerType triggerType) {
		this.triggerType = triggerType;
	}

	/**
	 * 撮影範囲を取得する
	 * @return snapMode
	 */
	public SnapArea getSnapArea() {
		return snapArea;
	}

	/**
	 * 撮影範囲をセットする
	 * @param area 撮影範囲
	 */
	public void setSnapArea(SnapArea area) {
		this.snapArea = area;
	}

	/**
	 * 拡張子を取得する
	 * @return format 拡張子
	 */
	public FormatType getFormatType() {
		return formatType;
	}

	/**
	 * 拡張子をセットする
	 * @param format 拡張子
	 */
	public void setFormatType(FormatType format) {
		this.formatType = format;
	}

	/**
	 * 撮影後設定を取得する
	 * @return snapAfter 撮影後設定
	 */
	public SnapAfter getSnapAfter() {
		return snapAfter;
	}

	/**
	 * 撮影後設定をセットする
	 * @param snapAfter 撮影後設定
	 */
	public void setSnapAfter(SnapAfter snapAfter) {
		this.snapAfter = snapAfter;
	}

	/**
	 * 撮影後、クリップボードに保存するかの設定を返します
	 * @return isCopyToClipboard
	 */
	public boolean getIsCopyToClipboard() {
		return isCopyToClipboard;
	}

	/**
	 * 撮影後、クリップボードに保存するかの設定をセットします
	 * @param isCopyToClipboard セットする isClipboard
	 */
	public void setIsCopyToClipboard(boolean isCopyToClipboard) {
		this.isCopyToClipboard = isCopyToClipboard;
	}

	/**
	 * カスタムキーのキーコードを取得する
	 * @return customKeyCodes
	 */
	public int[] getCustomKeyCodes() {
		return customKeyCodes;
	}

	/**
	 * カスタムキーのキーコードをセットする
	 * @param customKeyCodes セットする
	 */
	public void setCustomKeyCodes(int[] customKeyCodes) {
		this.customKeyCodes = customKeyCodes;
	}
}
