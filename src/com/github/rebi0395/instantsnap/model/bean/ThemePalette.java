package com.github.rebi0395.instantsnap.model.bean;

import java.awt.Color;

import javax.swing.UIManager;

/**
 * Swing画面で共有するテーマ配色を保持する不変クラスです。
 * <p>
 * JavaFXの配色はCSSが担当し、このクラスはメインウィンドウや通知などの
 * Swing画面だけで使用します。
 * </p>
 */
public final class ThemePalette {

	/** 画面全体の背景色 */
	private final Color background;

	/** 入力欄やボタンなどの背景色 */
	private final Color controlBackground;

	/** 通常の文字色 */
	private final Color text;

	/** ショートカットや閉じるボタンなどの補助文字色 */
	private final Color mutedText;

	/** 入力欄やボタンの枠線色 */
	private final Color border;

	/** 一覧で選択している項目の背景色 */
	private final Color selectionBackground;

	/** タイトルや主要操作に使用するアクセント色 */
	private final Color accent;

	/** アクセント色の上へ表示する文字色 */
	private final Color accentText;

	/** 通知本体の背景色 */
	private final Color notificationBackground;

	/** 通知本体の枠線色 */
	private final Color notificationBorder;

	/** 通知本体の影色 */
	private final Color notificationShadow;

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
			Color accentText,
			Color notificationBackground,
			Color notificationBorder,
			Color notificationShadow) {
		this.background = background;
		this.controlBackground = controlBackground;
		this.text = text;
		this.mutedText = mutedText;
		this.border = border;
		this.selectionBackground = selectionBackground;
		this.accent = accent;
		this.accentText = accentText;
		this.notificationBackground = notificationBackground;
		this.notificationBorder = notificationBorder;
		this.notificationShadow = notificationShadow;
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
				new Color(27, 31, 38),
				new Color(44, 51, 62),
				new Color(237, 241, 245),
				new Color(184, 193, 204),
				new Color(70, 81, 95),
				new Color(49, 95, 136),
				new Color(100, 169, 232),
				new Color(21, 32, 43),
				new Color(36, 42, 51),
				new Color(70, 81, 95),
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
	 * @return background 画面全体の背景色
	 */
	public Color getBackground() {
		return background;
	}

	/**
	 * 入力欄やボタンなどの背景色を取得する
	 * @return controlBackground 入力欄やボタンなどの背景色
	 */
	public Color getControlBackground() {
		return controlBackground;
	}

	/**
	 * 通常の文字色を取得する
	 * @return text 通常の文字色
	 */
	public Color getText() {
		return text;
	}

	/**
	 * 補助文字色を取得する
	 * @return mutedText 補助文字色
	 */
	public Color getMutedText() {
		return mutedText;
	}

	/**
	 * 入力欄やボタンの枠線色を取得する
	 * @return border 入力欄やボタンの枠線色
	 */
	public Color getBorder() {
		return border;
	}

	/**
	 * 一覧で選択している項目の背景色を取得する
	 * @return selectionBackground 一覧で選択している項目の背景色
	 */
	public Color getSelectionBackground() {
		return selectionBackground;
	}

	/**
	 * タイトルや主要操作に使用するアクセント色を取得する
	 * @return accent タイトルや主要操作に使用するアクセント色
	 */
	public Color getAccent() {
		return accent;
	}

	/**
	 * アクセント色の上へ表示する文字色を取得する
	 * @return accentText アクセント色の上へ表示する文字色
	 */
	public Color getAccentText() {
		return accentText;
	}

	/**
	 * アクセント操作へマウスを乗せたときの色を取得する
	 * @return アクセント色を一段階暗くしたホバー色
	 */
	public Color getAccentHover() {
		return accent.darker();
	}

	/**
	 * アクセント操作を押しているときの色を取得する
	 * @return アクセント色を二段階暗くした押下色
	 */
	public Color getAccentPressed() {
		return accent.darker().darker();
	}

	/**
	 * 通知本体の背景色を取得する
	 * @return notificationBackground 通知本体の背景色
	 */
	public Color getNotificationBackground() {
		return notificationBackground;
	}

	/**
	 * 通知本体の枠線色を取得する
	 * @return notificationBorder 通知本体の枠線色
	 */
	public Color getNotificationBorder() {
		return notificationBorder;
	}

	/**
	 * 通知本体の影色を取得する
	 * @return notificationShadow 通知本体の影色
	 */
	public Color getNotificationShadow() {
		return notificationShadow;
	}
}
