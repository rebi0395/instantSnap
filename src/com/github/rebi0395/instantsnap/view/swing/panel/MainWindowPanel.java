package com.github.rebi0395.instantsnap.view.swing.panel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;

import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.KeyCombinationBean;
import com.github.rebi0395.instantsnap.model.bean.ThemePalette;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.model.enums.ThemeType;
import com.github.rebi0395.instantsnap.view.swing.icon.ActionIcon;
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

	/** 絵本テーマで設定ボタンに表示するアイコン */
	private static final String STORYBOOK_SETTINGS_ICON_PATH = "/images/theme/storybook/icon-bear-32.png";

	/** 絵本テーマで撮影ボタンに表示するアイコン */
	private static final String STORYBOOK_CAPTURE_ICON_PATH = "/images/theme/storybook/icon-camera-32.png";

	/** 絵本テーマで範囲選択ボタンに表示するアイコン */
	private static final String STORYBOOK_RANGE_SELECTION_ICON_PATH = "/images/theme/storybook/icon-paw-32.png";

	/** 設定画面を開くボタン */
	private final JButton settingsButton;

	/** 通常撮影を開始するボタン */
	private final JButton captureButton;

	/** 範囲選択を開始するボタン */
	private final JButton rangeSelectionButton;

	/** ミニウィンドウ上で言語を変更する選択欄 */
	private final JComboBox<LanguageType> languageComboBox;

	/** 言語選択欄へ現在のテーマ色を反映するレンダラー */
	private final LanguageListCellRenderer languageRenderer;

	/** 通常撮影のショートカット表示 */
	private final JLabel captureShortcutLabel;

	/** 範囲選択のショートカット表示 */
	private final JLabel rangeSelectionShortcutLabel;

	/** パネル上の操作を受け取るプレゼンター */
	private final MainWindowPresenter presenter;

	/** デフォルト・ダークテーマで設定ボタンに表示するアイコン */
	private final Icon standardSettingsIcon;

	/** デフォルト・ダークテーマで撮影ボタンに表示するアイコン */
	private final Icon standardCaptureIcon;

	/** デフォルト・ダークテーマで範囲選択ボタンに表示するアイコン */
	private final Icon standardRangeSelectionIcon;

	/** 絵本テーマで設定ボタンに表示するアイコン */
	private final Icon storybookSettingsIcon;

	/** 絵本テーマで撮影ボタンに表示するアイコン */
	private final Icon storybookCaptureIcon;

	/** 絵本テーマで範囲選択ボタンに表示するアイコン */
	private final Icon storybookRangeSelectionIcon;

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
		this.standardSettingsIcon = new ActionIcon(ActionIcon.Type.SETTINGS);
		this.standardCaptureIcon = new ActionIcon(ActionIcon.Type.CAPTURE);
		this.standardRangeSelectionIcon = new ActionIcon(ActionIcon.Type.RANGE_SELECTION);
		this.storybookSettingsIcon = loadThemeIcon(STORYBOOK_SETTINGS_ICON_PATH, standardSettingsIcon);
		this.storybookCaptureIcon = loadThemeIcon(STORYBOOK_CAPTURE_ICON_PATH, standardCaptureIcon);
		this.storybookRangeSelectionIcon = loadThemeIcon(
				STORYBOOK_RANGE_SELECTION_ICON_PATH,
				standardRangeSelectionIcon);
		this.settingsButton = createActionButton(standardSettingsIcon);
		this.captureButton = createActionButton(standardCaptureIcon);
		this.rangeSelectionButton = createActionButton(standardRangeSelectionIcon);
		this.captureShortcutLabel = createShortcutLabel();
		this.rangeSelectionShortcutLabel = createShortcutLabel();
		this.languageRenderer = new LanguageListCellRenderer();
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
	 * @param icon ボタンに表示するアイコン
	 * @return このパネルへ操作を通知するボタン
	 */
	private JButton createActionButton(Icon icon) {
		JButton button = new JButton(icon);
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
	 * クラスパス上のテーマ用画像を読み込みます。
	 * パッケージ漏れなどで画像が見つからない場合も操作できるよう、標準アイコンへ戻します。
	 *
	 * @param resourcePath クラスパスのルートから始まる画像パス
	 * @param fallbackIcon 読み込みに失敗した場合に使用するアイコン
	 * @return 読み込んだ画像アイコン、または標準アイコン
	 */
	private Icon loadThemeIcon(String resourcePath, Icon fallbackIcon) {
		URL imageUrl = MainWindowPanel.class.getResource(resourcePath);
		if (imageUrl == null) {
			return fallbackIcon;
		}
		return new ImageIcon(imageUrl);
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
		comboBox.setRenderer(languageRenderer);
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

		applyTheme(config.getThemeType());
	}

	/**
	 * 設定画面で選択されたテーマをミニウィンドウへ反映します。
	 * デフォルトではSwing標準UIを使い、ダークと絵本では共通Paletteの配色を使います。
	 *
	 * @param themeType 最新のテーマ設定
	 */
	private void applyTheme(ThemeType themeType) {
		languageRenderer.setTheme(themeType);
		applyActionButtonIcons(themeType);
		ThemePalette themePalette = themeType.getThemePalette();

		if (themeType == ThemeType.DEFAULT) {
			applyDefaultTheme(themePalette);
		} else {
			applyCustomTheme(themePalette);
		}

		languageComboBox.repaint();
		revalidate();
		repaint();
	}

	/**
	 * 絵本テーマでは絵柄付きの画像へ切り替え、それ以外ではJava2D製の標準アイコンへ戻します。
	 *
	 * @param themeType 最新のテーマ設定
	 */
	private void applyActionButtonIcons(ThemeType themeType) {
		if (themeType == ThemeType.STORYBOOK) {
			settingsButton.setIcon(storybookSettingsIcon);
			captureButton.setIcon(storybookCaptureIcon);
			rangeSelectionButton.setIcon(storybookRangeSelectionIcon);
			return;
		}
		settingsButton.setIcon(standardSettingsIcon);
		captureButton.setIcon(standardCaptureIcon);
		rangeSelectionButton.setIcon(standardRangeSelectionIcon);
	}

	/**
	 * ダークまたは絵本テーマの共通配色をミニウィンドウへ反映します。
	 *
	 * @param themePalette 選択されたテーマのSwing配色
	 */
	private void applyCustomTheme(ThemePalette themePalette) {
		setBackground(themePalette.getBackground());
		captureShortcutLabel.setForeground(themePalette.getMutedText());
		rangeSelectionShortcutLabel.setForeground(themePalette.getMutedText());

		languageComboBox.setBackground(themePalette.getControlBackground());
		languageComboBox.setForeground(themePalette.getText());
		languageComboBox.setBorder(BorderFactory.createLineBorder(themePalette.getBorder()));

		Border buttonBorder = BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(themePalette.getBorder()),
				BorderFactory.createEmptyBorder(4, 6, 4, 6));
		for (JButton button : getActionButtons()) {
			// OS標準スキンによる色の上書きを避け、テーマ配色を確実に描画する。
			button.setUI(new BasicButtonUI());
			button.setOpaque(true);
			button.setBackground(themePalette.getControlBackground());
			button.setForeground(themePalette.getText());
			button.setBorder(buttonBorder);
		}
	}

	/**
	 * カスタムテーマから切り替えたとき、Swing標準のUIと共通配色へ戻します。
	 *
	 * @param themePalette デフォルトテーマのSwing配色
	 */
	private void applyDefaultTheme(ThemePalette themePalette) {
		setBackground(themePalette.getBackground());
		captureShortcutLabel.setForeground(themePalette.getText());
		rangeSelectionShortcutLabel.setForeground(themePalette.getText());

		languageComboBox.setBackground(themePalette.getControlBackground());
		languageComboBox.setForeground(themePalette.getText());
		languageComboBox.setBorder(UIManager.getBorder("ComboBox.border"));

		Border buttonBorder = UIManager.getBorder("Button.border");
		for (JButton button : getActionButtons()) {
			// カスタムテーマ専用UIを外し、OSに対応したSwing標準UIへ戻す。
			button.updateUI();
			button.setBackground(themePalette.getControlBackground());
			button.setForeground(themePalette.getText());
			button.setBorder(buttonBorder);
		}
	}

	/**
	 * テーマを同じ手順で適用する操作ボタンを返します。
	 *
	 * @return 設定、撮影、範囲選択ボタン
	 */
	private JButton[] getActionButtons() {
		return new JButton[] { settingsButton, captureButton, rangeSelectionButton };
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

		/** 現在のミニウィンドウテーマ */
		private ThemeType themeType = ThemeType.DEFAULT;

		/** 現在のミニウィンドウ配色 */
		private ThemePalette themePalette = ThemeType.DEFAULT.getThemePalette();

		/**
		 * 言語選択欄の描画テーマを切り替えます。
		 *
		 * @param themeType 最新のテーマ設定
		 */
		private void setTheme(ThemeType themeType) {
			this.themeType = themeType;
			this.themePalette = themeType.getThemePalette();
		}

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
			if (themeType != ThemeType.DEFAULT) {
				setForeground(themePalette.getText());
				setBackground(themePalette.getControlBackground());
				if (isSelected) {
					setBackground(themePalette.getSelectionBackground());
					setForeground(themePalette.getAccentText());
				}
				if (!list.isEnabled()) {
					setForeground(themePalette.getMutedText());
				}
			}
			return component;
		}
	}
}
