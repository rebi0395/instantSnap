package com.github.rebi0395.instantsnap.model.bean;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.SnapMode;

/**
 * 設定値保管クラス。セット時に値の安全性も保証する
 */
public class Option {
	private String savePath;
	private SnapMode snapMode;
	private FormatType format;
	private String trigger;
	//	private String openMode;

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(Option.class);

	/**
	 * コンストラクタ。デフォルト値を設定する
	 */
	public Option() {
		this.savePath = DEFAULT_SAVE_PATH;
		this.snapMode = SnapMode.ACTIVE_WINDOW;
		this.format = FormatType.PNG;
		this.trigger = SHIFT_PRINTSCREEN;
	}

	/**
	 * 設定値を検証し、不正な値の場合はデフォルト値を適用して警告ログを出すセッターヘルパー
	 * 
	 * @param <T> プロパティの型（EnumやIntegerなど）
	 * @param value プロパティファイルから読み込んだ文字列
	 * @param parser 文字列を目的の型に変換するロジック
	 * @param configName ログに出力する設定項目名
	 * @param defaultValue 想定外の値だった場合に返すデフォルト値
	 * @return 解析に成功した値、またはデフォルト値
	 */
	private <T> T validateAndConvert(String value, Function<String, T> parser, String configName, T defaultValue) {
		try {
			// パース処理を試みる
			T parsedValue = parser.apply(value);

			// パース結果がnullでなければそれを採用
			if (parsedValue != null) {
				return parsedValue;
			}
		} catch (RuntimeException e) {
			log.error("パース処理に失敗しました");
		}
		log.warn("設定項目：「"+ configName + "」の値が不正のため、"
				+ "デフォルト値：" + defaultValue + "を適用します");
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
	public SnapMode getSnapMode() {
		return snapMode;
	}

	/**
	 * プロパティファイルの記載を元に、撮影モードをセットする
	 * 想定外の値のとき、デフォルト値を設定し、ログで通知する
	 * @param value プロパティファイルの記載
	 */
	public void setSnapMode(String value) {
		this.snapMode = validateAndConvert(
				value,
				SnapMode::fromCode,
				SNAP_MODE,
				SnapMode.ACTIVE_WINDOW);
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
		this.format = validateAndConvert(
				value,
				FormatType::fromString,
				FORMAT,
				FormatType.PNG);
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
