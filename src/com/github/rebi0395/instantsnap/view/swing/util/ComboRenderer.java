package com.github.rebi0395.instantsnap.view.swing.util;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.model.enums.ThemeType;

/**
 * コンボボックスを制御するレンダラー
 */
public class ComboRenderer extends DefaultListCellRenderer {
	private ThemeType themeType;
	private ThemePalette themePalette;

	/**
	 * コンストラクタ
	 * @param themeType テーマタイプ
	 * @param themePalette テーマタイプの色
	 */
	public ComboRenderer(ThemeType themeType, ThemePalette themePalette) {
		this.themeType = themeType;
		this.themePalette = themePalette;
		setOpaque(true);
	}

	/**
	 * コンボボックスの各項目をテーマに応じた色、表示名で描画するレンダラー
	 */
	@Override
	public Component getListCellRendererComponent(
			JList<?> list,
			Object value,
			int index,
			boolean isSelected,
			boolean cellHasFocus) {

		// Swing 標準の描画処理（選択状態・フォーカスなどの基本設定）
		Component component = super.getListCellRendererComponent(
				list, value, index, isSelected, cellHasFocus);

		// JLabelとして扱う（DefaultListCellRenderer は JLabel を返す）
		JLabel label = (JLabel) component;

		// 表示名を設定
		switch (value) {
		case LanguageType languageType -> {
			String displayName = languageType.getBaseDisplayName();
			label.setText(displayName);

			Font currentFont = label.getFont();
			if (currentFont != null && currentFont.canDisplayUpTo(displayName) >= 0) {
				// Segoe UIにない日中韓の文字は、複数言語を扱える論理フォントで描画する。
				Font fallbackFont = new Font(Font.DIALOG, currentFont.getStyle(), currentFont.getSize());
				label.setFont(fallbackFont);
			}
		}
		default -> {

		}
		}

		if (themeType == ThemeType.DARK) {
			// ダークモード用の色設定
			if (isSelected) {
				label.setBackground(themePalette.getSelectionBackgroundColor());
				label.setForeground(themePalette.getTextColor());
			}
			if (!list.isEnabled()) {
				label.setForeground(themePalette.getMutedTextColor());
			}
			return label;
		}

		if (themeType == ThemeType.STORYBOOK) {
			// 絵本テーマ用の色設定
			if (isSelected) {
				label.setBackground(themePalette.getSelectionBackgroundColor());
				label.setForeground(themePalette.getTextColor());
			}
			if (!list.isEnabled()) {
				label.setForeground(themePalette.getMutedTextColor());
			}
			return label;
		}
		return label;
	}
	
	/**
	 * 描画テーマを切り替える
	 *
	 * @param themeType 最新のテーマ設定
	 */
	public void setTheme(ThemeType themeType) {
		this.themeType = themeType;
		this.themePalette = themeType.getThemePalette();
	}
}
