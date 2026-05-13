package instantsnap.core;

import static instantsnap.util.AppConstant.*;

/**
 * 設定値保管クラス
 * setterの代わりにnull値チェック等を実装した
 * applyメソッドを用いて、値を安全に設定する
 */
public class Option {
	
	private String savePath;
	private String format;
	private String trigger;
	private String openMode;

	/**
	 * コンストラクタ。デフォルト値を設定する
	 */
	public Option(){
		this.savePath = DEFAULT_SAVE_PATH;
		this.format = PNG;
		this.trigger = SHIFT_PRINTSCREEN;
		this.openMode = DISOPEN;
	}

	/**
	 * @return savePath
	 */
	public String getSavePath() {
		return savePath;
	}

	/**
	 * nullチェック、前後トリム処理を行い、savePathを適用する
	 * 想定外の値やnull時には、デフォルトパスをセットする。
	 * @param savePath スクショの保存先パス
	 */
	public void applySavePath(String savePath) {
		if (savePath == null) {
			this.savePath = DEFAULT_SAVE_PATH;
		} else {
			this.savePath = savePath.strip();
		}
	}

	/**
	 * @return format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * nullチェックを行い、formatを適用する
	 * 想定外の値やnull時には、拡張子.pngをセットする。
	 * @param savePath スクショの拡張子
	 */
	public void applyFormat(String format) {
		if (format == null) {
			this.format = PNG;
		} else if (JPG.equals(format)) {
			this.format = JPG;
		} else {
			this.format = PNG;
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
	public void applyTrigger(String trigger) {
		if (trigger == null) {
			this.trigger = SHIFT_PRINTSCREEN;
		} else if (PRINTSCREEN.equals(trigger)) {
			this.trigger = PRINTSCREEN;
		} else {
			this.trigger = SHIFT_PRINTSCREEN;
		}
	}

	/**
	 * @return openMode
	 */
	public String getOpenMode() {
		return openMode;
	}

	/**
	 * nullチェックを行い、撮影後の画像/フォルダ展開有無を適用する
	 * 想定外の値やnull時には、撮影後フォルダを開かない。
	 * @param openMode 撮影後の自動展開設定
	 */
	public void applyOpenMode(String openMode) {
		if (openMode == null) {
			this.openMode = DISOPEN;
		} else if (AUTO_OPEN_IMAGE.equals(openMode)) {
			this.openMode = AUTO_OPEN_IMAGE;
		} else if (AUTO_OPEN_FOLDER.equals(openMode)){
			this.openMode = AUTO_OPEN_FOLDER;
		} else {
			this.openMode = DISOPEN;
		}
	}
}
