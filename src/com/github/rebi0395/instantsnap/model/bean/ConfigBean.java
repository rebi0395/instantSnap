package com.github.rebi0395.instantsnap.model.bean;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;
import static com.github.rebi0395.instantsnap.common.StrCheckUtil.*;
import static com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*;

import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.model.enums.AfterSnapActionType;
import com.github.rebi0395.instantsnap.model.enums.FileNameDateFormatType;
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
	/** ファイル名の先頭文字 */
	private String fileNamePrefix;
	/** ファイル名の日時フォーマット */
	private FileNameDateFormatType fileNameDateFormatType;
	/** 拡張子 */
	private FormatType formatType;

	/** 撮影後設定 */
	private AfterSnapActionType afterSnapActionType;
	/** 撮影後、通知を表示する設定（trueで有効） */
	private boolean isShowNotification;
	/** 撮影音を鳴らす設定（trueで有効） */
	private boolean isSnapSoundEnabled;
	/** 通知音を鳴らす設定（trueで有効） */
	private boolean isNotificationSoundEnabled;

	/**
	 * コンストラクタ。デフォルト値を設定する
	 */
	public ConfigBean() {
		// -- 言語設定
		this.languageType = LanguageType.ENGLISH;

		// -- 撮影設定
		this.triggerType = TriggerType.SHIFT_PRINTSCREEN;
		this.customKeyBean = new KeyCombinationBean(
				VC_UNDEFINED,
				DEFAULT_USE_CTRL,
				DEFAULT_USE_ALT,
				DEFAULT_USE_SHIFT);
		this.snapAreaType = SnapAreaType.ACTIVE_WINDOW;

		// -- 出力設定
		this.isSaveImage = DEFAULT_SAVE_IMAGE_ENABLED;
		this.isCopyToClipboard = DEFAULT_CLIPBOARD_ENABLED;
		this.savePath = DEFAULT_SAVE_DIR;
		this.fileNamePrefix = DEFAULT_FILE_NAME_PREFIX;
		this.fileNameDateFormatType = FileNameDateFormatType.MILLISECOND_DOT;
		this.formatType = FormatType.PNG;

		// -- 撮影後動作
		this.afterSnapActionType = AfterSnapActionType.NONE;
		this.isShowNotification = DEFAULT_NOTIFICATION_ENABLED; // 通知
		this.isSnapSoundEnabled = DEFAULT_SOUND_SNAP_ENABLED; // 撮影音
		this.isNotificationSoundEnabled = DEFAULT_SOUND_NOTIFICATION_ENABLED; // 通知音
	}

	/**
	 * 画像ファイルが必要かを返す
	 *
	 * @return 画像保存ON、または撮影後アクションありの場合 true
	 */
	public boolean isFileRequired() {
		return this.getIsSaveImage()
				|| this.getAfterSnapActionType() != AfterSnapActionType.NONE;
	}

	/**
	 * 一時保存かを返す
	 *
	 * @return 撮影後アクションが一時保存。または
	 * 画像保存OFF、かつ撮影後アクションありの場合 true
	 */
	public boolean isTemporarySave() {
		return this.getAfterSnapActionType() == AfterSnapActionType.SAVE_TO_TMP ||
				(!this.getIsSaveImage() && this.getAfterSnapActionType() != AfterSnapActionType.NONE);
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
	 * ファイル名の先頭文字を取得する。
	 * @return fileNamePrefix ファイル名の先頭文字
	 */
	public String getFileNamePrefix() {
		return fileNamePrefix;
	}

	/**
	 * ファイル名の先頭文字をセットする
	 * ファイル名で使用できない文字は■で置換される。
	 * @param fileNamePrefix ファイル名の先頭文字
	 */
	public void setFileNamePrefix(String fileNamePrefix) {
		if (isBlankSafe(fileNamePrefix)) {
			this.fileNamePrefix = DEFAULT_FILE_NAME_PREFIX;
		} else {
			this.fileNamePrefix = sanitizeFileNamePrefix(fileNamePrefix);
		}
	}

	/**
	 * ファイル名の日時フォーマット設定を取得する
	 * @return fileNameDateFormatType ファイル名の日時フォーマット設定
	 */
	public FileNameDateFormatType getFileNameDateFormatType() {
		return fileNameDateFormatType;
	}

	/**
	 * ファイル名の日時フォーマット設定をセットする
	 * @param fileNameDateFormatType セットする
	 */
	public void setFileNameDateFormatType(FileNameDateFormatType fileNameDateFormatType) {
		this.fileNameDateFormatType = fileNameDateFormatType;
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
	 * 撮影音を有効にするかの設定を取得する
	 * @return isSnapSoundEnabled
	 */
	public boolean getIsSnapSoundEnabled() {
		return isSnapSoundEnabled;
	}

	/**
	 * 撮影音を表示するかの設定をセットする
	 * @param isSnapSoundEnabled
	 */
	public void setIsSnapSoundEnabled(boolean isSoundEnabled) {
		this.isSnapSoundEnabled = isSoundEnabled;
	}

	/**
	 * 通知音を有効にするかの設定を取得する
	 * @return isNotificationSoundEnabled
	 */
	public boolean getIsNotificationSoundEnabled() {
		return isNotificationSoundEnabled;
	}

	/**
	 * 通知音を表示するかの設定をセットする
	 * @param isNotificationSoundEnabled
	 */
	public void setIsNotificationSoundEnabled(boolean isNotificationSoundEnabled) {
		this.isNotificationSoundEnabled = isNotificationSoundEnabled;
	}

}