package com.github.rebi0395.instantsnap.model.bean;


import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.SnapMode;

/**
 * 設定値保管クラス
 */
public class Option {
	private String savePath;
	private SnapMode snapMode;
	private FormatType format;
	private String trigger;
//	private String openMode;

	/**
	 * コンストラクタ。デフォルト値を設定する
	 */
	public Option(){
		this.savePath = DEFAULT_SAVE_PATH;
		this.snapMode = SnapMode.ACTIVE_WINDOW;
		this.format = FormatType.PNG;
		this.trigger = SHIFT_PRINTSCREEN;
	}

	/**
	 * スクショの保存先パスを取得する
	 * @return savePath スクショの保存先パス
	 */
	public String getSavePath() {
		if (this.savePath != null) {
			return this.savePath.strip().replace("\\", "/");
		}
		return savePath;
	}

	/**
	 * \の置換処理、前後トリム処理を行い、savePathを適用する
	 * 想定外の値やnull時には、デフォルトパスをセットする
	 * @param savePath スクショの保存先パス
	 */
	public void setSavePath(String savePath) {
		if (savePath != null) {
			this.savePath = savePath.strip().replace("\\", "/");
		} else {
			this.savePath = DEFAULT_SAVE_PATH;
		}
	}

	/**
	 * @return snapMode
	 */
	public SnapMode getSnapMode() {
		return snapMode;
	}

	/**
	 * プロパティファイルの記載を元に、撮影範囲を適応します
	 * @param code プロパティファイルの記載
	 * @return デフォルトを適応した場合true、適応していない場合false
	 */
	public boolean applySnapMode(String code) {
		SnapMode mode = SnapMode.fromCode(code);
		if (mode != null) {
			this.snapMode = mode;
			return false;
		} else {
			this.snapMode = SnapMode.ACTIVE_WINDOW; // デフォルト値
			return true;
		}
	}

	/**
	 * @return format
	 */
	public FormatType getFormat() {
		return format;
	}


	/**
	 * プロパティファイルの記載を元に、拡張子を適応します
	 * @param propertyValue
	 * @return デフォルトを適応した場合true、適応していない場合false
	 */
	public boolean applyFormat(String propertyValue) {
		FormatType type = FormatType.fromString(propertyValue);
		if (type != null) {
			this.format = type;
			return false;
		} else {
			this.format = FormatType.PNG; // デフォルト値
			return true;
		}
	}

	/**
	 * @return trigger
	 */
	public String getTrigger() {
		return trigger;
	}

	/**
	 * nullチェックを行い、起動キーを適用する
	 * 想定外の値やnull時には、shift + printscreenをセットする。
	 * @param trigger スクリーンショット起動キー
	 */
	public void setTrigger(String trigger) {
		if (trigger == null) {
			this.trigger = SHIFT_PRINTSCREEN;
		} else if (PRINTSCREEN.equals(trigger)) {
			this.trigger = PRINTSCREEN;
		} else {
			this.trigger = SHIFT_PRINTSCREEN;
		}
	}

//	/**
//	 * @return openMode
//	 */
//	public String getOpenMode() {
//		return openMode;
//	}

//	/**
//	 * nullチェックを行い、撮影後の画像/フォルダ展開有無を適用する
//	 * 想定外の値やnull時には、撮影後フォルダを開かない。
//	 * @param openMode 撮影後の自動展開設定
//	 */
//	public void setOpenMode(String openMode) {
//		if (openMode == null) {
//			this.openMode = DISOPEN;
//		} else if (AUTO_OPEN_IMAGE.equals(openMode)) {
//			this.openMode = AUTO_OPEN_IMAGE;
//		} else if (AUTO_OPEN_FOLDER.equals(openMode)){
//			this.openMode = AUTO_OPEN_FOLDER;
//		} else {
//			this.openMode = DISOPEN;
//		}
//	}
}
