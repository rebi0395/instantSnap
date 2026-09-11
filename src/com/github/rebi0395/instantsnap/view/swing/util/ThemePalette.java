package com.github.rebi0395.instantsnap.view.swing.util;

import java.awt.Color;

import javax.swing.UIManager;

/**
 * Swing画面で共有するテーマ配色を保持する不変クラスです。
 */
public final class ThemePalette {

	/** 画面全体の背景色 */
	private final Color backgroundColor;

	/** 入力欄やボタンなどの背景色 */
	private final Color controlBackgroundColor;

	/** 通常の文字色 */
	private final Color textColor;

	/** ショートカットや閉じるボタンなどの補助文字色 */
	private final Color mutedTextColor;

	/** 入力欄やボタンの枠線色 */
	private final Color borderColor;

	/** 一覧で選択している項目の背景色 */
	private final Color selectionBackgroundColor;

	/** タイトルや主要操作に使用するアクセント色 */
	private final Color accentColor;

	/** キーボードフォーカスを表す枠線色 */
	private final Color focusColor;

	/** キーボードフォーカスの外側へ表示する発光色 */
	private final Color focusGlowColor;

	/** アクセント色の上へ表示する文字色 */
	private final Color accentTextColor;

	/** 通知本体の背景色 */
	private final Color notificationBackgroundColor;

	/** 通知本体の枠線色 */
	private final Color notificationBorderColor;

	/** 通知本体の影色 */
	private final Color notificationShadowColor;

	/**
	 * 各Swing画面で使用する配色を生成します。
	 * テーマごとの具体的な値は名前付きのファクトリーメソッドへ集約します。
	 */
	private ThemePalette(
			Color background,
			Color controlBackground,
			Color text,
			Color mutedText,
			Color border,
			Color selectionBackground,
			Color accent,
			Color focus,
			Color focusGlow,
			Color accentText,
			Color notificationBackground,
			Color notificationBorder,
			Color notificationShadow) {
		this.backgroundColor = background;
		this.controlBackgroundColor = controlBackground;
		this.textColor = text;
		this.mutedTextColor = mutedText;
		this.borderColor = border;
		this.selectionBackgroundColor = selectionBackground;
		this.accentColor = accent;
		this.focusColor = focus;
		this.focusGlowColor = focusGlow;
		this.accentTextColor = accentText;
		this.notificationBackgroundColor = notificationBackground;
		this.notificationBorderColor = notificationBorder;
		this.notificationShadowColor = notificationShadow;
	}

	/**
	 * 現在のSwing Look and Feelを基準にデフォルト配色を生成します。
	 *
	 * @return デフォルトテーマの配色
	 */
	public static ThemePalette createDefault() {
		Color background = getUiColor("Panel.background", new Color(238, 238, 238));
		Color controlBackground = getUiColor("Button.background", background);
		Color text = getUiColor("Label.foreground", new Color(51, 51, 51));
		Color mutedText = getUiColor("Label.disabledForeground", new Color(102, 102, 102));
		Color border = getUiColor("Component.borderColor", new Color(184, 184, 184));
		Color selectionBackground = getUiColor("ComboBox.selectionBackground", new Color(0, 120, 215));
		return new ThemePalette(
				background,
				controlBackground,
				text,
				mutedText,
				border,
				selectionBackground,
				new Color(0x09, 0x69, 0xda),
				getUiColor("Component.focusColor", new Color(25, 118, 210)),
				new Color(144, 202, 249),
				Color.WHITE,
				Color.WHITE,
				new Color(0xd0, 0xd7, 0xde),
				new Color(0, 0, 0, 7));
	}

	/**
	 * ダークテーマの配色を生成します。
	 *
	 * @return ダークテーマの配色
	 */
	public static ThemePalette createDark() {
		return new ThemePalette(
				new Color(15, 17, 21),
				new Color(28, 33, 40),
				new Color(242, 245, 248),
				new Color(196, 205, 215),
				new Color(116, 131, 151),
				new Color(67, 113, 153),
				new Color(112, 184, 242),
				new Color(66, 165, 255),
				new Color(150, 215, 255),
				new Color(21, 32, 43),
				new Color(24, 28, 34),
				new Color(116, 131, 151),
				new Color(0, 0, 0, 40));
	}

	/**
	 * 絵本テーマの配色を生成します。
	 *
	 * @return 絵本テーマの配色
	 */
	public static ThemePalette createStorybook() {
		return new ThemePalette(
				new Color(255, 242, 220),
				new Color(255, 253, 248),
				new Color(70, 51, 33),
				new Color(138, 112, 90),
				new Color(239, 189, 120),
				new Color(255, 154, 69),
				new Color(232, 125, 40),
				new Color(255, 173, 98),
				new Color(255, 228, 196),
				Color.WHITE,
				new Color(255, 253, 248),
				new Color(239, 189, 120),
				new Color(104, 72, 42, 20));
	}

	/**
	 * UIManagerから色を取得し、未定義の場合は代替色を返します。
	 *
	 * @param key UIManagerへ問い合わせるキー
	 * @param fallback 未定義時に使用する色
	 * @return Look and Feelの色、または代替色
	 */
	private static Color getUiColor(String key, Color fallback) {
		Color color = UIManager.getColor(key);
		if (color != null) {
			return color;
		}
		return fallback;
	}

	/**
	 * 画面全体の背景色を取得する
	 * @return backgroundColor 画面全体の背景色
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * 入力欄やボタンなどの背景色を取得する
	 * @return controlBackgroundColor 入力欄やボタンなどの背景色
	 */
	public Color getControlBackgroundColor() {
		return controlBackgroundColor;
	}

	/**
	 * 通常の文字色を取得する
	 * @return textColor 通常の文字色
	 */
	public Color getTextColor() {
		return textColor;
	}

	/**
	 * 補助文字色を取得する
	 * @return mutedTextColor 補助文字色
	 */
	public Color getMutedTextColor() {
		return mutedTextColor;
	}

	/**
	 * 入力欄やボタンの枠線色を取得する
	 * @return borderColor 入力欄やボタンの枠線色
	 */
	public Color getBorderColor() {
		return borderColor;
	}

	/**
	 * 一覧で選択している項目の背景色を取得する
	 * @return selectionBackgroundColor 一覧で選択している項目の背景色
	 */
	public Color getSelectionBackgroundColor() {
		return selectionBackgroundColor;
	}

	/**
	 * タイトルや主要操作に使用するアクセント色を取得する
	 * @return accentColor タイトルや主要操作に使用するアクセント色
	 */
	public Color getAccentColor() {
		return accentColor;
	}

	/**
	 * キーボードフォーカスを表す枠線色を取得する
	 * @return focusColor キーボードフォーカスの枠線色
	 */
	public Color getFocusColor() {
		return focusColor;
	}

	/**
	 * キーボードフォーカスの外側へ表示する発光色を取得する
	 * @return focusGlowColor キーボードフォーカスの発光色
	 */
	public Color getFocusGlowColor() {
		return focusGlowColor;
	}

	/**
	 * アクセント色の上へ表示する文字色を取得する
	 * @return accentTextColor アクセント色の上へ表示する文字色
	 */
	public Color getAccentTextColor() {
		return accentTextColor;
	}

	/**
	 * アクセント操作へマウスを乗せたときの色を取得する
	 * @return アクセント色を一段階暗くしたホバー色
	 */
	public Color getAccentHoverColor() {
		return accentColor.darker();
	}

	/**
	 * アクセント操作を押しているときの色を取得する
	 * @return アクセント色を二段階暗くした押下色
	 */
	public Color getAccentPressedColor() {
		return accentColor.darker().darker();
	}

	/**
	 * 通知本体の背景色を取得する
	 * @return notificationBackgroundColor 通知本体の背景色
	 */
	public Color getNotificationBackgroundColor() {
		return notificationBackgroundColor;
	}

	/**
	 * 通知本体の枠線色を取得する
	 * @return notificationBorderColor 通知本体の枠線色
	 */
	public Color getNotificationBorderColor() {
		return notificationBorderColor;
	}

	/**
	 * 通知本体の影色を取得する
	 * @return notificationShadowColor 通知本体の影色
	 */
	public Color getNotificationShadowColor() {
		return notificationShadowColor;
	}
}
