package com.github.rebi0395.instantsnap.view.swing.icon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * メインウィンドウの小型操作ボタンに表示するJava2D製アイコンです。
 * 通知ボタンが未選択の場合は斜線を重ね、通知が無効であることを表します。
 */
public final class QuickSettingIcon implements Icon {

	/** アイコンの一辺 */
	private static final int SIZE = 18;

	/** 描画するアイコンの種類 */
	private final Type type;

	/**
	 * 指定した種類のアイコンを生成します。
	 *
	 * @param type 描画するアイコンの種類
	 */
	public QuickSettingIcon(Type type) {
		this.type = type;
	}

	@Override
	public int getIconWidth() {
		return SIZE;
	}

	@Override
	public int getIconHeight() {
		return SIZE;
	}

	@Override
	public void paintIcon(Component component, Graphics graphics, int x, int y) {
		Graphics2D graphics2d = (Graphics2D) graphics.create();
		try {
			graphics2d.translate(x, y);
			graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics2d.setColor(resolveIconColor(component));
			graphics2d.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

			if (type == Type.DIRECTORY) {
				paintDirectoryIcon(graphics2d);
			} else {
				paintNotificationIcon(graphics2d);
			}

			if (type == Type.NOTIFICATION
					&& component instanceof AbstractButton button
					&& !button.isSelected()) {
				graphics2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				graphics2d.drawLine(2, 2, SIZE - 2, SIZE - 2);
			}
		} finally {
			graphics2d.dispose();
		}
	}

	/**
	 * 部品の有効状態に応じた描画色を取得します。
	 *
	 * @param component アイコンを表示する部品
	 * @return 通常の文字色、または無効時の文字色
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

	/** 保存先を表すフォルダを描画します。 */
	private void paintDirectoryIcon(Graphics2D graphics2d) {
		Path2D directory = new Path2D.Double();
		directory.moveTo(2, 5);
		directory.lineTo(7, 5);
		directory.lineTo(9, 7);
		directory.lineTo(16, 7);
		directory.lineTo(16, 15);
		directory.lineTo(2, 15);
		directory.closePath();
		graphics2d.draw(directory);
		graphics2d.drawLine(2, 8, 16, 8);
	}

	/** 通知ベルを描画します。 */
	private void paintNotificationIcon(Graphics2D graphics2d) {
		Path2D bell = new Path2D.Double();
		bell.moveTo(4, 12.5);
		bell.curveTo(5, 11, 5.5, 10, 5.5, 7);
		bell.curveTo(5.5, 3, 12.5, 3, 12.5, 7);
		bell.curveTo(12.5, 10, 13, 11, 14, 12.5);
		bell.closePath();
		graphics2d.draw(bell);
		graphics2d.drawLine(4, 13, 14, 13);
		graphics2d.drawArc(7, 12, 4, 4, 180, 180);
	}

	/** 小型設定ボタンで使用するアイコンの種類です。 */
	public enum Type {
		/** 現在設定されている保存先フォルダを開く操作 */
		DIRECTORY,
		/** 撮影後に表示する通知の有効・無効 */
		NOTIFICATION
	}
}
