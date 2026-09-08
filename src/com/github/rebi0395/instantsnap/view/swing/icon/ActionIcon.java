package com.github.rebi0395.instantsnap.view.swing.icon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * メインウィンドウの操作を表すJava2D製アイコンです。
 * 画像倍率に依存せず、表示するボタンの文字色を使って滑らかに描画します。
 */
public final class ActionIcon implements Icon {

	/** アイコンの一辺 */
	private static final int SIZE = 34;

	/** 描画するアイコンの種類 */
	private final Type type;

	/**
	 * 指定した種類のアイコンを生成します。
	 *
	 * @param type 描画するアイコンの種類
	 */
	public ActionIcon(Type type) {
		this.type = type;
	}

	/**
	 * アイコンの横幅を取得します。
	 *
	 * @return Swing論理座標での横幅
	 */
	@Override
	public int getIconWidth() {
		return SIZE;
	}

	/**
	 * アイコンの高さを取得します。
	 *
	 * @return Swing論理座標での高さ
	 */
	@Override
	public int getIconHeight() {
		return SIZE;
	}

	/**
	 * 部品の有効状態に応じた色でアイコンを描画します。
	 *
	 * @param component アイコンを表示する部品
	 * @param graphics Swingから渡された描画コンテキスト
	 * @param x アイコン左端の論理座標
	 * @param y アイコン上端の論理座標
	 */
	@Override
	public void paintIcon(Component component, Graphics graphics, int x, int y) {
		// 親部品の描画設定に影響させないようコピーを使い、最後に解放する。
		Graphics2D graphics2d = (Graphics2D) graphics.create();
		try {
			graphics2d.translate(x, y);
			graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			graphics2d.setColor(resolveIconColor(component));
			graphics2d.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

			switch (type) {
			case SETTINGS -> paintSettingsIcon(graphics2d);
			case CAPTURE -> paintCaptureIcon(graphics2d);
			case RANGE_SELECTION -> paintRangeSelectionIcon(graphics2d);
			}
		} finally {
			graphics2d.dispose();
		}
	}

	/**
	 * 部品の有効状態に応じた描画色を取得します。
	 *
	 * @param component アイコンを表示する部品
	 * @return 通常の文字色、無効色、または無効色が未定義の場合は灰色
	 */
	private Color resolveIconColor(Component component) {
		if (component.isEnabled()) {
			return component.getForeground();
		}
		Color disabledColor = UIManager.getColor("Button.disabledText");
		if (disabledColor != null) {
			return disabledColor;
		}
		return Color.GRAY;
	}

	/**
	 * 歯車を簡略化した設定アイコンを描画します。
	 *
	 * @param graphics2d アイコン左上を原点とする描画コンテキスト
	 */
	private void paintSettingsIcon(Graphics2D graphics2d) {
		double center = SIZE / 2.0;
		for (int index = 0; index < 8; index++) {
			double angle = Math.PI * index / 4.0;
			double innerX = center + Math.cos(angle) * 10.5;
			double innerY = center + Math.sin(angle) * 10.5;
			double outerX = center + Math.cos(angle) * 14.5;
			double outerY = center + Math.sin(angle) * 14.5;
			graphics2d.drawLine((int) innerX, (int) innerY, (int) outerX, (int) outerY);
		}
		graphics2d.draw(new Ellipse2D.Double(6.5, 6.5, 21, 21));
		graphics2d.draw(new Ellipse2D.Double(12.5, 12.5, 9, 9));
	}

	/**
	 * カメラ本体とレンズを組み合わせた撮影アイコンを描画します。
	 *
	 * @param graphics2d アイコン左上を原点とする描画コンテキスト
	 */
	private void paintCaptureIcon(Graphics2D graphics2d) {
		graphics2d.draw(new RoundRectangle2D.Double(3.5, 9.5, 27, 20, 4, 4));
		Path2D top = new Path2D.Double();
		top.moveTo(9, 9.5);
		top.lineTo(12, 5.5);
		top.lineTo(21.5, 5.5);
		top.lineTo(25, 9.5);
		graphics2d.draw(top);
		graphics2d.draw(new Ellipse2D.Double(11, 13, 12, 12));
		graphics2d.draw(new Ellipse2D.Double(14.5, 16.5, 5, 5));
	}

	/**
	 * 四隅の切り取り線で範囲選択アイコンを描画します。
	 *
	 * @param graphics2d アイコン左上を原点とする描画コンテキスト
	 */
	private void paintRangeSelectionIcon(Graphics2D graphics2d) {
		Path2D corners = new Path2D.Double();
		corners.moveTo(13, 5);
		corners.lineTo(5, 5);
		corners.lineTo(5, 13);
		corners.moveTo(21, 5);
		corners.lineTo(29, 5);
		corners.lineTo(29, 13);
		corners.moveTo(29, 21);
		corners.lineTo(29, 29);
		corners.lineTo(21, 29);
		corners.moveTo(13, 29);
		corners.lineTo(5, 29);
		corners.lineTo(5, 21);
		graphics2d.draw(corners);

		// 選択範囲を示す中央の矩形だけ破線にする。
		graphics2d.setStroke(new BasicStroke(
				1.5f,
				BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER,
				10.0f,
				new float[] { 3.0f, 3.0f },
				0.0f));
		graphics2d.drawRect(10, 10, 14, 14);
	}

	/** メインウィンドウで使用する操作アイコンの種類です。 */
	public enum Type {
		/** 歯車で表す設定画面の起動 */
		SETTINGS,
		/** カメラで表す通常撮影 */
		CAPTURE,
		/** 切り取り線で表す範囲選択 */
		RANGE_SELECTION
	}
}
