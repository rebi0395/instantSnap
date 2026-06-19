package com.github.rebi0395.instantsnap.model.bean;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;
import static com.github.rebi0395.instantsnap.common.StrCheckUtil.*;
import static com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*;

import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.model.enums.AfterSnapActionType;
import com.github.rebi0395.instantsnap.model.enums.SnapAreaType;
import com.github.rebi0395.instantsnap.model.enums.TriggerType;

/**
 * 設定値保管クラス。セット時に値の安全性も保証する
 */
public class ConfigBean {

	/** 言語設定 */
	private LanguageType languageType;
	/** 撮影キー */
	private TriggerType triggerType;
	/** 撮影カスタムキーの保存クラス */
	private KeyCombinationBean customKeyBean;
	/** 撮影範囲 */
	private SnapAreaType snapAreaType;

	/** 画像を保存する (trueで保存する) */
	private boolean isSaveImage;
	/** クリップボードに画像に保存する (trueで保存する) */
	private boolean isCopyToClipboard;
	/** 画像の保存パス */
	private String savePath;
	/** 拡張子 */
	private FormatType formatType;

	/** 撮影後設定 */
	private AfterSnapActionType afterSnapActionType;
	/** 撮影後、通知を表示する設定（trueで有効） */
	private boolean isShowNotification;
	/** 撮影音を鳴らす設定（trueで有効） */
	private boolean isSoundEnabled;

	/**
	 * コンストラクタ。デフォルト値を設定する
	 */
	public ConfigBean() {
		// -- 言語設定
		this.languageType = LanguageType.JAPANESE;

		// -- 撮影設定
		this.triggerType = TriggerType.SHIFT_PRINTSCREEN;
		this.customKeyBean = new KeyCombinationBean(VC_UNDEFINED, false, false, false);
		this.snapAreaType = SnapAreaType.ACTIVE_WINDOW;

		// -- 出力設定
		this.isSaveImage = true;
		this.isCopyToClipboard = true;
		this.savePath = DEFAULT_SAVE_DIR;
		this.formatType = FormatType.PNG;

		// -- 撮影後、通知設定
		this.afterSnapActionType = AfterSnapActionType.NONE;
		this.isShowNotification = false;
		this.isSoundEnabled = true;
	}

	/**
	 * 言語設定を取得する
	 * @return languageType 言語設定
	 */
	public LanguageType getLanguageType() {
		return languageType;
	}

	/**
	 * 言語設定をセットする
	 * @param languageType セットする
	 */
	public void setLanguageType(LanguageType languageType) {
		this.languageType = languageType;
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
	 * カスタム設定のキーの組み合わせを取得する
	 * @return customKeyBean
	 */
	public KeyCombinationBean getCustomKeyBean() {
		return customKeyBean;
	}

	/**
	 * カスタム設定のキーの組み合わせをセットする
	 * @param customKeyBean
	 */
	public void setCustomKeyBean(KeyCombinationBean customKeyBean) {
		this.customKeyBean = customKeyBean;
	}

	/**
	 * 撮影範囲を取得する
	 * @return snapMode
	 */
	public SnapAreaType getSnapAreaType() {
		return snapAreaType;
	}

	/**
	 * 撮影範囲をセットする
	 * @param area 撮影範囲
	 */
	public void setSnapAreaType(SnapAreaType area) {
		this.snapAreaType = area;
	}

	/**
	 * 撮影後、画像を保存するかの設定を返します
	 * @return isSaveImage
	 */
	public boolean getIsSaveImage() {
		return isSaveImage;
	}

	/**
	 * 撮影後、画像を保存するかの設定をセットします
	 * @param isSaveImage セットする
	 */
	public void setIsSaveImage(boolean isSaveImage) {
		this.isSaveImage = isSaveImage;
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
	 * @param isCopyToClipboard セットする
	 */
	public void setIsCopyToClipboard(boolean isCopyToClipboard) {
		this.isCopyToClipboard = isCopyToClipboard;
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
			this.savePath = DEFAULT_SAVE_DIR;
		} else {
			this.savePath = savePath.strip().replace("\\", "/");
		}
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
	 * @return afterSnapActionType 撮影後設定
	 */
	public AfterSnapActionType getAfterSnapActionType() {
		return afterSnapActionType;
	}

	/**
	 * 撮影後設定をセットする
	 * @param afterSnapActionType 撮影後設定
	 */
	public void setAfterSnapActionType(AfterSnapActionType afterSnapActionType) {
		this.afterSnapActionType = afterSnapActionType;
	}

	/**
	 * 撮影後、通知を表示するかの設定を取得する
	 * @return isShowNotification
	 */
	public boolean getIsShowNotification() {
		return isShowNotification;
	}

	/**
	 * 撮影後、通知を表示するかの設定をセットする
	 * @param isShowNotification セットする
	 */
	public void setIsShowNotification(boolean isShowNotification) {
		this.isShowNotification = isShowNotification;
	}

	/**
	 * 撮影後、通知音を有効にするかの設定を取得する
	 * @return isSoundEnabled
	 */
	public boolean getIsSoundEnabled() {
		return isSoundEnabled;
	}

	/**
	 * 撮影後、通知音を表示するかの設定をセットする
	 * @param isSoundEnabled
	 */
	public void setIsSoundEnabled(boolean isSoundEnabled) {
		this.isSoundEnabled = isSoundEnabled;
	}
}