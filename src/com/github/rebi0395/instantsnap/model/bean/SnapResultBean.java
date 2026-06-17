package com.github.rebi0395.instantsnap.model.bean;

import java.nio.file.Path;

public class SnapResultBean {

	/** 画像の保存先パス */
	private Path targetPath;

	/** 画像保存に成功したかの結果を保持 trueで成功 */
	private boolean isImageSaved = false;

	/** クリップボード保存に成功したかの結果を保持 trueで成功 */
	private boolean isClipboardCopied = false;

	/**
	 * 通知できる結果があるかを返却します
	 * @return 画像保存またｈクリップボード保存に成功している場合 true
	 */
	public boolean hasResult() {
		return isImageSaved || isClipboardCopied;
	}

	/**
	 * targetPath を取得する
	 * @return targetPath
	 */
	public Path getTargetPath() {
		return targetPath;
	}

	/**
	 * targetPath をセットする
	 * @param targetPath
	 */
	public void setTargetPath(Path targetPath) {
		this.targetPath = targetPath;
	}

	/**
	 * isImageSaved を取得する
	 * @return isImageSaved
	 */
	public boolean getIsImageSaved() {
		return isImageSaved;
	}

	/**
	 * isImageSaved をセットする
	 * @param isImageSaved
	 */
	public void setIsImageSaved(boolean isImageSaved) {
		this.isImageSaved = isImageSaved;
	}

	/**
	 * isClipboardCopied を取得する
	 * @return isClipboardCopied
	 */
	public boolean getIsClipboardCopied() {
		return isClipboardCopied;
	}

	/**
	 * isClipboardCopied をセットする
	 * @param isClipboardCopied
	 */
	public void setIsClipboardCopied(boolean isClipboardCopied) {
		this.isClipboardCopied = isClipboardCopied;
	}

}
