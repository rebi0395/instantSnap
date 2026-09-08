package com.github.rebi0395.instantsnap.view.swing.util;

import java.awt.Component;

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

	@Override
	public Component getListCellRendererComponent(
			JList<?> list,
			Object value,
			int index,
			boolean isSelected,
			boolean cellHasFocus) {

		Component component = super.getListCellRendererComponent(
				list, value, index, isSelected, cellHasFocus);

		// JLabel として扱う（DefaultListCellRenderer は JLabel を返す）
		JLabel label = (JLabel) component;

		// 表示名を設定
		switch (value) {
		case LanguageType languageType -> label.setText(languageType.getBaseDisplayName());
		default -> label.setText(value == null ? "" : value.toString());
		}
		
		// Windows の閉じた ComboBox は白背景で描画されることがあるため
		if (themeType == ThemeType.DARK) {
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
			// STORYBOOK 用の色設定
			label.setForeground(themePalette.getTextColor());
			label.setBackground(themePalette.getControlBackgroundColor());
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
	 * 言語選択欄の描画テーマを切り替えます。
	 *
	 * @param themeType 最新のテーマ設定
	 */
	public void setTheme(ThemeType themeType) {
		this.themeType = themeType;
		this.themePalette = themeType.getThemePalette();
	}
}
