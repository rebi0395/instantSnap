package com.github.rebi0395.instantsnap.model.bean;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.SnapAfter;
import com.github.rebi0395.instantsnap.model.enums.SnapArea;

/**
 * 設定値保管クラス。セット時に値の安全性も保証する
 */
public class Option {
	private String savePath;
	private SnapArea snapArea;
	private FormatType format;
	private String trigger;
	private SnapAfter snapAfter;

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(Option.class);

	/**
	 * コンストラクタ。デフォルト値を設定する
	 */
	public Option() {
		this.savePath = DEFAULT_SAVE_PATH;
		this.snapArea = SnapArea.ACTIVE_WINDOW;
		this.format = FormatType.PNG;
		this.trigger = SHIFT_PRINTSCREEN;
		this.snapAfter = SnapAfter.NOTHING;
	}

	/**
	 * 設定値を検証し、不正な値の場合はデフォルト値を適用して警告ログを出す
	 * 
	 * @param <T> プロパティの型（EnumやIntegerなど）
	 * @param value 検索対象
	 * @param defaultValue 想定外の値だった場合に返すデフォルト値
	 * @param configName 設定項目名（ログ出力用）
	 * @param raw プロパティファイルから読み込んだ生の文字列（ログ出力用）
	 * @return 解析に成功した値、またはデフォルト値
	 */
	@SuppressWarnings("unchecked")
	private <T> T validateAndConvert(T value, T defaultValue, String configName, String raw) {

		// String型の場合、空チェックを実施する
		if (value instanceof String str) {
			if (!str.strip().isBlank()){
				return (T) str.strip(); // 空白除去し、返却
			} else {
				value = null; // 不正値扱いにする
			}
		}

		// nullでなければ採用する
		if (value != null) {
			return value;
		} 

		log.warn("設定項目「{}」の値({})が不正のため、デフォルト値 {} を適用します", 
				configName, raw, defaultValue);
		return defaultValue;
	}

	/**
	 * スクショの保存先パスを取得する。
	 * @return savePath スクショの保存先パス
	 */
	public String getSavePath() {
		return savePath;
	}

	/**
	 * \の置換処理、前後トリム処理を行い、savePathを適用する
	 * 想定外の値やnull時には、デフォルトパスをセットする
	 * @param savePath スクショの保存先パス
	 */
	public void setSavePath(String savePath) {
		if (savePath != null && !savePath.isBlank()) {
			this.savePath = savePath.strip().replace("\\", "/");
		} else {
			this.savePath = DEFAULT_SAVE_PATH;
		}
	}

	/**
	 * @return snapMode
	 */
	public SnapArea getSnapMode() {
		return snapArea;
	}

	/**
	 * プロパティファイルの記載を元に、撮影モードをセットする
	 * 想定外の値のとき、デフォルト値を設定し、ログで通知する
	 * @param value プロパティファイルの記載
	 */
	public void setSnapMode(String value) {
		SnapArea mode = SnapArea.fromValue(value);
		this.snapArea = validateAndConvert(mode, SnapArea.ACTIVE_WINDOW, SNAP_AREA, value);
	}

	/**
	 * @return format
	 */
	public FormatType getFormat() {
		return format;
	}

	/**
	 * プロパティファイルの記載を元に、拡張子をセットする
	 * 想定外の値のとき、デフォルト値を設定し、ログで通知する
	 * @param value プロパティファイルの記載
	 */
	public void setFormat(String value) {
		FormatType type = FormatType.fromValue(value);
		this.format = validateAndConvert(type, FormatType.PNG, FORMAT, value);
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

	/**
	 * @return snapAfter
	 */
	public SnapAfter getSnapAfter() {
		return snapAfter;
	}

	/**
	 * プロパティファイルの記載を元に、撮影後設定をセットする
	 * 想定外の値のとき、デフォルト値を設定し、ログで通知する
	 * @param value プロパティファイルの記載
	 */
	public void setSnapAfter(String value) {
		SnapAfter after = SnapAfter.fromValue(value);
		this.snapAfter = validateAndConvert(after, SnapAfter.NOTHING, SNAP_AFTER, value);
	}
}
