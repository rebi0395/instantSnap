package com.github.rebi0395.instantsnap.common;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 * 画像をクリップボードに転送するためのクラス
 */
public class ImageTransferable implements Transferable {

	/** 転送する画像 */
	private Image image;

	/** 画像を登録するコンストラクタ */
	public ImageTransferable(Image image) {
		this.image = image;
	}

	/**
	 * 転送可能な画像型データフレーバーを取得する
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.imageFlavor };
	}

	/**
	 * 指定されたデータフレーバーが使用できるかを確認する
	 */
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return DataFlavor.imageFlavor.equals(flavor);
	}

	/**
	 * 指定されたデータフレーバーに対応するデータを取得する
	 */
	@Override
	public Object getTransferData(DataFlavor flavor) {
		if (!DataFlavor.imageFlavor.equals(flavor)) {
			AppLogger.error("getTransferDataFlavors InValid DataFlavor");
			return null;
		}
		return image;
	}
}
