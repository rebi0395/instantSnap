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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.KeyCombinationBean;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.view.swing.presenter.MainWindowPresenter;

/**
 * 常駐中に表示する小型メインウィンドウのパネルです。
 * <p>
 * このクラスは表示とユーザー操作の受付だけを担当し、撮影などの実処理は
 * MainWindowPresenterの対応するメソッドを呼び出します。
 * 生成と表示内容の変更はSwingイベントディスパッチスレッド（EDT）上で行います。
 * </p>
 */
public class MainWindowPanel extends JPanel implements ActionListener {

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

	/** パネル上の操作を受け取るプレゼンター */
	private final MainWindowPresenter presenter;

	/** プログラムから言語選択を更新中の場合true */
	private boolean updatingLanguageSelection;

	/**
	 * 現在の設定値を使ってパネルを生成します。
	 *
	 * @param config 現在の設定値
	 * @param presenter パネル上の操作を受け取るプレゼンター
	 */
	public MainWindowPanel(ConfigBean config, MainWindowPresenter presenter) {

		this.presenter = presenter;
		// 部品を先に揃え、設定値の反映は初期表示と再読込で同じ経路にする。
		this.settingsButton = createActionButton(ActionIconType.SETTINGS);
		this.captureButton = createActionButton(ActionIconType.CAPTURE);
		this.rangeSelectionButton = createActionButton(ActionIconType.RANGE_SELECTION);
		this.captureShortcutLabel = createShortcutLabel();
		this.rangeSelectionShortcutLabel = createShortcutLabel();
		this.languageComboBox = createLanguageComboBox();

		refresh(config);
		initializeLayout();
	}

	/**
	 * パネル内のコンポーネントを横一列に配置します。
	 */
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

	/**
	 * 言語選択またはショートカット表示と、ボタンを縦に並べます。
	 *
	 * @param upperComponent ボタンの上に置く部品
	 * @param button 下段に置く操作ボタン
	 * @return 上下2段に配置した透明背景のパネル
	 */
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

	/**
	 * 操作用のアイコンボタンを作成します。表示名はrefreshで設定します。
	 *
	 * @param iconType ボタンに描画するアイコンの種類
	 * @return このパネルへ操作を通知するボタン
	 */
	private JButton createActionButton(ActionIconType iconType) {
		JButton button = new JButton(new ActionIcon(iconType));
		button.setPreferredSize(ACTION_BUTTON_SIZE);
		button.setMinimumSize(ACTION_BUTTON_SIZE);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setIconTextGap(7);
		button.setFocusPainted(false);
		button.addActionListener(this);
		return button;
	}

	/**
	 * ショートカット表示用の空ラベルを作成します。文言はrefreshで設定します。
	 *
	 * @return 高さと幅を揃えた中央揃えのラベル
	 */
	private JLabel createShortcutLabel() {
		JLabel label = new JLabel("", SwingConstants.CENTER);
		label.setPreferredSize(SHORTCUT_LABEL_SIZE);
		label.setMinimumSize(SHORTCUT_LABEL_SIZE);

		Font baseFont = UIManager.getFont("Label.font");
		if (baseFont != null) {
			label.setFont(baseFont.deriveFont(Math.max(11.0f, baseFont.getSize2D() - 1.0f)));
		}
		return label;
	}

	/**
	 * 言語の表示名を使う選択欄を生成します。選択値はrefreshで設定します。
	 *
	 * @return 言語の変更をこのパネルへ通知する選択欄
	 */
	private JComboBox<LanguageType> createLanguageComboBox() {
		JComboBox<LanguageType> comboBox = new JComboBox<>(LanguageType.values());
		comboBox.setPreferredSize(SHORTCUT_LABEL_SIZE);
		comboBox.setMinimumSize(SHORTCUT_LABEL_SIZE);
		comboBox.setRenderer(new LanguageListCellRenderer());
		comboBox.setToolTipText("Language / 言語");
		comboBox.addActionListener(this);
		return comboBox;
	}

	/**
	 * パネル上の操作に対応するPresenterのメソッドをEDT上で呼び出します。
	 *
	 * @param event ボタン押下または言語選択の変更イベント
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object eventSource = event.getSource();
		if (eventSource == settingsButton) {
			presenter.openSettings();
			return;
		}
		if (eventSource == captureButton) {
			presenter.requestSnap();
			return;
		}
		if (eventSource == rangeSelectionButton) {
			presenter.requestRangeSelection();
			return;
		}
		if (eventSource == languageComboBox && !updatingLanguageSelection) {
			Object selectedItem = languageComboBox.getSelectedItem();
			if (selectedItem instanceof LanguageType languageType) {
				presenter.changeLanguage(languageType);
			}
		}
	}

	/**
	 * 現在の設定値を文言、ショートカット、言語選択へ反映します。
	 * EDT上で呼び出します。渡された設定値の保存・変更は行いません。
	 *
	 * @param config 表示へ反映する最新の設定値
	 */
	public void refresh(ConfigBean config) {
		UiTexts texts = resolveUiTexts(config.getLanguageType());
		settingsButton.setText(texts.settings());
		captureButton.setText(texts.capture());
		rangeSelectionButton.setText(texts.rangeSelection());
		captureShortcutLabel.setText(resolveShortcutText(config.getSnapKeyBean(), texts.notSet()));
		rangeSelectionShortcutLabel.setText(resolveShortcutText(config.getSelectionKeyBean(), texts.notSet()));

		// 再読込による選択変更をユーザー操作と扱い、再保存することを防ぐ。
		updatingLanguageSelection = true;
		try {
			languageComboBox.setSelectedItem(config.getLanguageType());
		} finally {
			updatingLanguageSelection = false;
		}
	}

	/**
	 * ショートカットの表示文字列を取得します。
	 *
	 * @param keyCombination キー設定。nullまたはトリガー未定義の場合は未設定扱い
	 * @param notSetText 未設定時に表示する文言
	 * @return 修飾キーを含むショートカット名、または未設定の文言
	 */
	private String resolveShortcutText(KeyCombinationBean keyCombination, String notSetText) {
		if (keyCombination == null || !keyCombination.isTriggerKeyDefined()) {
			return notSetText;
		}
		return keyCombination.resolveKeyCombinationName();
	}

	/**
	 * 言語設定に対応したメインウィンドウ用の短い文言を取得します。
	 *
	 * @param language 表示言語。日本語以外は英語を使用
	 * @return ボタン名とショートカット未設定時の文言
	 */
	private UiTexts resolveUiTexts(LanguageType language) {
		if (language == LanguageType.JAPANESE) {
			return new UiTexts("設定", "撮影", "範囲選択", "未設定");
		}
		return new UiTexts("Settings", "Capture", "Select area", "Not set");
	}

	/**
	 * 設定ボタンと言語選択をまとめて有効・無効にします。
	 * 設定画面との同時編集を避けるため、両方を同じ状態にします。
	 *
	 * @param enabled 設定操作を許可する場合true
	 */
	public void setSettingsEnabled(boolean enabled) {
		settingsButton.setEnabled(enabled);
		languageComboBox.setEnabled(enabled);
	}

	/**
	 * パネル上のすべての操作を有効・無効にします。
	 *
	 * @param enabled 操作を許可する場合true。終了処理の開始時はfalse
	 */
	public void setActionsEnabled(boolean enabled) {
		setSettingsEnabled(enabled);
		captureButton.setEnabled(enabled);
		rangeSelectionButton.setEnabled(enabled);
	}

	/** パネル内で使用するアイコンの種類です。 */
	private enum ActionIconType {
		/** 歯車で表す設定画面の起動 */
		SETTINGS,
		/** カメラで表す通常撮影 */
		CAPTURE,
		/** 切り取り線で表す範囲選択 */
		RANGE_SELECTION
	}

	/**
	 * メインウィンドウで使う短い文言です。
	 *
	 * @param settings 設定ボタン名
	 * @param capture 撮影ボタン名
	 * @param rangeSelection 範囲選択ボタン名
	 * @param notSet ショートカット未設定時の文言
	 */
	private record UiTexts(String settings, String capture, String rangeSelection, String notSet) {
	}

	/** LanguageTypeを利用者向けの表示名で描画するレンダラーです。 */
	private static final class LanguageListCellRenderer extends DefaultListCellRenderer {

		/** シリアルバージョンUID */
		private static final long serialVersionUID = 1L;

		/**
		 * 言語の列挙値を利用者向けの表示名で描画します。
		 *
		 * @param list 描画対象の一覧
		 * @param value 項目の値
		 * @param index 項目位置。選択欄本体の描画時は-1
		 * @param isSelected 選択中の場合true
		 * @param cellHasFocus フォーカスがある場合true
		 * @return Swing標準の配色に言語名を設定した描画部品
		 */
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

		/**
		 * 指定した種類のアイコンを生成します。
		 *
		 * @param type 描画するアイコンの種類
		 */
		private ActionIcon(ActionIconType type) {
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
		 * @param g2 アイコン左上を原点とする描画コンテキスト
		 */
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

		/**
		 * カメラ本体とレンズを組み合わせた撮影アイコンを描画します。
		 *
		 * @param g2 アイコン左上を原点とする描画コンテキスト
		 */
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

		/**
		 * 四隅の切り取り線で範囲選択アイコンを描画します。
		 *
		 * @param g2 アイコン左上を原点とする描画コンテキスト
		 */
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
