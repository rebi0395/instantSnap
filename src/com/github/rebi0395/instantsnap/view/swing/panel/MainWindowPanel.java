package com.github.rebi0395.instantsnap.view.swing.panel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.github.rebi0395.instantsnap.model.enums.LanguageType;

/**
 * 常駐中に表示する小型メインウィンドウのパネルです。
 * <p>
 * このクラスは表示とユーザー操作の通知だけを担当し、撮影などの実処理は
 * コンストラクタで受け取ったアクションへ委譲します。
 * </p>
 */
public class MainWindowPanel extends JPanel {

	/** 言語選択が変更されたことを通知するインターフェースです。 */
	@FunctionalInterface
	public interface LanguageChangeListener {

		/**
		 * 選択言語の変更を通知します。
		 *
		 * @param languageType 選択された言語
		 */
		void onLanguageChanged(LanguageType languageType);
	}

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 操作ボタンの表示サイズ */
	private static final Dimension ACTION_BUTTON_SIZE = new Dimension(112, 82);

	/** ショートカット表示領域のサイズ */
	private static final Dimension SHORTCUT_LABEL_SIZE = new Dimension(126, 22);

	/** 設定画面を開くボタン */
	private final JButton settingsButton;

	/** 通常撮影を開始するボタン */
	private final JButton captureButton;

	/** 範囲選択を開始するボタン */
	private final JButton rangeSelectionButton;

	/** ミニウィンドウ上で言語を変更する選択欄 */
	private final JComboBox<LanguageType> languageComboBox;

	/** 通常撮影のショートカット表示 */
	private final JLabel captureShortcutLabel;

	/** 範囲選択のショートカット表示 */
	private final JLabel rangeSelectionShortcutLabel;

	/** プログラムから言語選択を更新中の場合true */
	private boolean updatingLanguageSelection;

	/**
	 * パネルを生成します。
	 *
	 * @param settingsText 設定ボタンの表示名
	 * @param captureText 撮影ボタンの表示名
	 * @param rangeSelectionText 範囲選択ボタンの表示名
	 * @param captureShortcut 撮影ショートカットの表示文字列
	 * @param rangeSelectionShortcut 範囲選択ショートカットの表示文字列
	 * @param selectedLanguage 現在の言語設定
	 * @param languageChangeListener 言語選択変更時の通知先
	 * @param settingsAction 設定ボタン押下時の処理
	 * @param captureAction 撮影ボタン押下時の処理
	 * @param rangeSelectionAction 範囲選択ボタン押下時の処理
	 */
	public MainWindowPanel(
			String settingsText,
			String captureText,
			String rangeSelectionText,
			String captureShortcut,
			String rangeSelectionShortcut,
			LanguageType selectedLanguage,
			LanguageChangeListener languageChangeListener,
			Runnable settingsAction,
			Runnable captureAction,
			Runnable rangeSelectionAction) {

		this.settingsButton = createActionButton(settingsText, ActionIconType.SETTINGS, settingsAction);
		this.captureButton = createActionButton(captureText, ActionIconType.CAPTURE, captureAction);
		this.rangeSelectionButton = createActionButton(
				rangeSelectionText,
				ActionIconType.RANGE_SELECTION,
				rangeSelectionAction);

		this.captureShortcutLabel = createShortcutLabel(captureShortcut);
		this.rangeSelectionShortcutLabel = createShortcutLabel(rangeSelectionShortcut);
		this.languageComboBox = createLanguageComboBox(selectedLanguage, languageChangeListener);

		initializeLayout();
	}

	/** パネル内のコンポーネントを横一列に配置します。 */
	private void initializeLayout() {
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridy = 0;
		constraints.insets = new Insets(0, 5, 0, 5);
		constraints.anchor = GridBagConstraints.CENTER;

		// 設定ボタンの上へ言語選択を置き、常駐画面だけで言語を変更できるようにする。
		constraints.gridx = 0;
		add(createActionColumn(languageComboBox, settingsButton), constraints);

		constraints.gridx = 1;
		add(createActionColumn(captureShortcutLabel, captureButton), constraints);

		constraints.gridx = 2;
		add(createActionColumn(rangeSelectionShortcutLabel, rangeSelectionButton), constraints);
	}

	/** ショートカット表示とボタンを縦に並べた操作列を作成します。 */
	private JPanel createActionColumn(Component upperComponent, JButton button) {
		JPanel column = new JPanel(new GridBagLayout());
		column.setOpaque(false);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(0, 0, 5, 0);
		column.add(upperComponent, constraints);

		constraints.gridy = 1;
		constraints.insets = new Insets(0, 0, 0, 0);
		column.add(button, constraints);
		return column;
	}

	/** 操作用の大きなアイコンボタンを作成します。 */
	private JButton createActionButton(String text, ActionIconType iconType, Runnable action) {
		JButton button = new JButton(text, new ActionIcon(iconType));
		button.setPreferredSize(ACTION_BUTTON_SIZE);
		button.setMinimumSize(ACTION_BUTTON_SIZE);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setIconTextGap(7);
		button.setFocusPainted(false);
		button.addActionListener(event -> action.run());
		return button;
	}

	/** ショートカットキーを中央揃えで表示するラベルを作成します。 */
	private JLabel createShortcutLabel(String shortcutText) {
		JLabel label = new JLabel(shortcutText, SwingConstants.CENTER);
		label.setPreferredSize(SHORTCUT_LABEL_SIZE);
		label.setMinimumSize(SHORTCUT_LABEL_SIZE);

		Font baseFont = UIManager.getFont("Label.font");
		if (baseFont != null) {
			label.setFont(baseFont.deriveFont(Math.max(11.0f, baseFont.getSize2D() - 1.0f)));
		}
		return label;
	}

	/** 言語の表示名を使う選択欄を生成します。 */
	private JComboBox<LanguageType> createLanguageComboBox(
			LanguageType selectedLanguage,
			LanguageChangeListener languageChangeListener) {
		JComboBox<LanguageType> comboBox = new JComboBox<>(LanguageType.values());
		comboBox.setPreferredSize(SHORTCUT_LABEL_SIZE);
		comboBox.setMinimumSize(SHORTCUT_LABEL_SIZE);
		comboBox.setRenderer(new LanguageListCellRenderer());
		comboBox.setToolTipText("Language / 言語");
		comboBox.setSelectedItem(selectedLanguage);
		comboBox.addActionListener(event -> {
			if (updatingLanguageSelection) {
				return;
			}
			Object selectedItem = comboBox.getSelectedItem();
			if (selectedItem instanceof LanguageType languageType) {
				languageChangeListener.onLanguageChanged(languageType);
			}
		});
		return comboBox;
	}

	/** 設定変更後のショートカット表示を更新します。 */
	public void updateShortcutTexts(String captureShortcut, String rangeSelectionShortcut) {
		captureShortcutLabel.setText(captureShortcut);
		rangeSelectionShortcutLabel.setText(rangeSelectionShortcut);
	}

	/** 言語設定変更後に各ボタンの表示名を更新します。 */
	public void updateButtonTexts(String settingsText, String captureText, String rangeSelectionText) {
		settingsButton.setText(settingsText);
		captureButton.setText(captureText);
		rangeSelectionButton.setText(rangeSelectionText);
	}

	/** JavaFX設定画面などで変更された言語を選択欄へ反映します。 */
	public void updateLanguageSelection(LanguageType languageType) {
		updatingLanguageSelection = true;
		try {
			languageComboBox.setSelectedItem(languageType);
		} finally {
			updatingLanguageSelection = false;
		}
	}

	/** 言語選択欄の有効状態を変更します。 */
	public void setLanguageSelectionEnabled(boolean enabled) {
		languageComboBox.setEnabled(enabled);
	}

	/** 設定画面の起動中かどうかに応じて設定ボタンの有効状態を変更します。 */
	public void setSettingsEnabled(boolean enabled) {
		settingsButton.setEnabled(enabled);
	}

	/** 終了処理中にすべての操作ボタンを無効化します。 */
	public void setActionsEnabled(boolean enabled) {
		settingsButton.setEnabled(enabled);
		captureButton.setEnabled(enabled);
		rangeSelectionButton.setEnabled(enabled);
		languageComboBox.setEnabled(enabled);
	}

	/** パネル内で使用するアイコンの種類です。 */
	private enum ActionIconType {
		SETTINGS,
		CAPTURE,
		RANGE_SELECTION
	}

	/** LanguageTypeを利用者向けの表示名で描画するレンダラーです。 */
	private static final class LanguageListCellRenderer extends DefaultListCellRenderer {

		/** シリアルバージョンUID */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(
				JList<?> list,
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus) {
			Component component = super.getListCellRendererComponent(
					list,
					value,
					index,
					isSelected,
					cellHasFocus);
			if (value instanceof LanguageType languageType) {
				setText(languageType.getBaseDisplayName());
			}
			return component;
		}
	}

	/** 画面倍率に依存せず滑らかに表示できるJava2D製アイコンです。 */
	private static final class ActionIcon implements Icon {

		/** アイコンの一辺 */
		private static final int SIZE = 34;

		/** 描画するアイコンの種類 */
		private final ActionIconType type;

		private ActionIcon(ActionIconType type) {
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
			Graphics2D g2 = (Graphics2D) graphics.create();
			try {
				g2.translate(x, y);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
				g2.setColor(resolveIconColor(component));
				g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

				switch (type) {
				case SETTINGS -> paintSettingsIcon(g2);
				case CAPTURE -> paintCaptureIcon(g2);
				case RANGE_SELECTION -> paintRangeSelectionIcon(g2);
				}
			} finally {
				g2.dispose();
			}
		}

		/** 無効時はLook & Feelの無効色を使い、ボタンの状態と見た目を合わせます。 */
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

		/** 歯車を簡略化した設定アイコンを描画します。 */
		private void paintSettingsIcon(Graphics2D g2) {
			double center = SIZE / 2.0;
			for (int index = 0; index < 8; index++) {
				double angle = Math.PI * index / 4.0;
				double innerX = center + Math.cos(angle) * 10.5;
				double innerY = center + Math.sin(angle) * 10.5;
				double outerX = center + Math.cos(angle) * 14.5;
				double outerY = center + Math.sin(angle) * 14.5;
				g2.drawLine((int) innerX, (int) innerY, (int) outerX, (int) outerY);
			}
			g2.draw(new Ellipse2D.Double(6.5, 6.5, 21, 21));
			g2.draw(new Ellipse2D.Double(12.5, 12.5, 9, 9));
		}

		/** カメラ本体とレンズを組み合わせた撮影アイコンを描画します。 */
		private void paintCaptureIcon(Graphics2D g2) {
			g2.draw(new RoundRectangle2D.Double(3.5, 9.5, 27, 20, 4, 4));
			Path2D top = new Path2D.Double();
			top.moveTo(9, 9.5);
			top.lineTo(12, 5.5);
			top.lineTo(21.5, 5.5);
			top.lineTo(25, 9.5);
			g2.draw(top);
			g2.draw(new Ellipse2D.Double(11, 13, 12, 12));
			g2.draw(new Ellipse2D.Double(14.5, 16.5, 5, 5));
		}

		/** 四隅の切り取り線で範囲選択アイコンを描画します。 */
		private void paintRangeSelectionIcon(Graphics2D g2) {
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
			g2.draw(corners);

			// 選択範囲を示す中央の矩形だけ破線にする。
			g2.setStroke(new BasicStroke(
					1.5f,
					BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER,
					10.0f,
					new float[] { 3.0f, 3.0f },
					0.0f));
			g2.drawRect(10, 10, 14, 14);
		}
	}
}
