package com.github.rebi0395.instantsnap.view;

import javafx.geometry.Rectangle2D;

/**
 * JavaFXとAWTのスクリーン座標を相互変換するクラス
 */
public final class ScreenRectangleConverter {

	/** インスタンス生成を禁止する */
	private ScreenRectangleConverter() {
	}

	/**
	 * JavaFXのスクリーン範囲をAWTのスクリーン範囲へ変換する
	 *
	 * @param source JavaFXのスクリーン範囲
	 * @return AWTのスクリーン範囲
	 */
	public static java.awt.Rectangle toAwt(Rectangle2D source) {
		int minX = (int) Math.floor(source.getMinX());
		int minY = (int) Math.floor(source.getMinY());
		int maxX = (int) Math.ceil(source.getMaxX());
		int maxY = (int) Math.ceil(source.getMaxY());

		return new java.awt.Rectangle(minX, minY, maxX - minX, maxY - minY);
	}
}