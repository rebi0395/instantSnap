package com.github.rebi0395.instantsnap.model.bean;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 撮影結果と、撮影結果のメッセージキーを保持するクラス
 */
public class SnapResultBean {

	/** 画像の保存先パス */
	private Path targetPath;

	/** 画像保存に成功したかの結果を保持 trueで成功 / 一時保存時はfalse */
	private boolean isImageSaved = false;

	/** 一時保存ファイルに画像保存に成功したかの結果を保持 trueで成功 */
	private boolean isTmpSaved = false;

	/** クリップボード保存に成功したかの結果を保持 trueで成功 */
	private boolean isClipboardCopied = false;

	/** メイン画面に表示するメッセージキーを確認するリスト */
	List<String> messageKeyList = new ArrayList<>();

	/** 引数なしコンストラクタ */
	public SnapResultBean() {
	}

	/**
	 * コピーコンストラクタ
	 *
	 * @param source コピー元の撮影結果
	 */
	public SnapResultBean(SnapResultBean source) {
		this.targetPath = source.targetPath;
		this.isImageSaved = source.isImageSaved;
		this.isTmpSaved = source.isTmpSaved;
		this.isClipboardCopied = source.isClipboardCopied;
		this.messageKeyList = new ArrayList<>(source.messageKeyList);
	}

	/**
	 * 通知できる結果があるかを返す
	 * @return 画像保存、一時保存、クリップボード保存のいずれかが成功している場合 true
	 */
	public boolean hasResult() {
		return isImageSaved || isTmpSaved || isClipboardCopied;
	}

	/**
	 * 通知にボタンを表示するかを返す
	 * @return 画像保存、一時保存に成功している場合 true
	 */
	public boolean isOpenActionAvailable() {
		return targetPath != null && (isImageSaved || isTmpSaved);
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

	/**
	 * isTmpSaved を取得する
	 * @return isTmpSaved
	 */
	public boolean getIsTmpSaved() {
		return isTmpSaved;
	}

	/**
	 * isTmpSaved をセットする
	 * @param isTmpSaved
	 */
	public void setIsTmpSaved(boolean isTmpSaved) {
		this.isTmpSaved = isTmpSaved;
	}

	/**
	 * messageKeyList を取得する
	 * @return messageKeyList
	 */
	public List<String> getMessageKeyList() {
		return messageKeyList;
	}

	/**
	 * messageKeyList をセットする
	 * @param messageKeyList
	 */
	public void setMessageKeyList(List<String> messageKeyList) {
		this.messageKeyList = messageKeyList;
	}

}
