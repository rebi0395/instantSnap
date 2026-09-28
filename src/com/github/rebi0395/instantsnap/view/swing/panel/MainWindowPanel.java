package com.github.rebi0395.instantsnap.view.swing.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;

import com.github.rebi0395.instantsnap.common.MessageKeyConstant;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.KeyCombinationBean;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.model.enums.SnapAreaType;
import com.github.rebi0395.instantsnap.model.enums.ThemeType;
import com.github.rebi0395.instantsnap.view.swing.icon.ActionIcon;
import com.github.rebi0395.instantsnap.view.swing.icon.QuickSettingIcon;
import com.github.rebi0395.instantsnap.view.swing.presenter.MainWindowPresenter;
import com.github.rebi0395.instantsnap.view.swing.util.ComboRenderer;
import com.github.rebi0395.instantsnap.view.swing.util.ThemePalette;

/**
 * 常駐中に表示する小型メインウィンドウのパネルです。
 * <p>
 * このクラスは表示とユーザー操作の受付だけを担当し、撮影などの実処理は
 * MainWindowPresenterの対応するメソッドを呼び出します。
 * 生成と表示内容の変更はSwingイベントディスパッチスレッド（EDT）上で行います。
 * </p>
 */
public class MainWindowPanel extends JPanel implements ActionListener, FocusListener {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 操作ボタンの表示サイズ */
	private static final Dimension ACTION_BUTTON_SIZE = new Dimension(112, 82);

	/** ショートカット表示領域のサイズ */
	private static final Dimension SHORTCUT_LABEL_SIZE = new Dimension(126, 22);

	/** ショートカット表示に使用するフォントサイズ */
	private static final float SHORTCUT_FONT_SIZE = 12.0f;

	/** 下段に表示する撮影範囲選択欄のサイズ */
	private static final Dimension SNAP_AREA_COMBO_SIZE = new Dimension(238, 30);

	/** 下段に表示する小型操作ボタンのサイズ */
	private static final Dimension QUICK_SETTING_BUTTON_SIZE = new Dimension(34, 34);

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

	/** ミニウィンドウ上で撮影範囲を変更する選択欄 */
	private final JComboBox<SnapAreaOption> snapAreaComboBox;

	/** 現在設定されている保存先フォルダを開く小型ボタン */
	private final JButton openSaveDirectoryButton;

	/** 撮影後の通知表示を有効・無効にする小型ボタン */
	private final JToggleButton notificationButton;

	/** コンボボックスへ現在のテーマ色を反映するレンダラー */
	private final ComboRenderer renderer;

	/** 通常撮影のショートカット表示 */
	private final JLabel captureShortcutLabel;

	/** 範囲選択のショートカット表示 */
	private final JLabel rangeSelectionShortcutLabel;

	/** パネル上の操作を受け取るプレゼンター */
	private final MainWindowPresenter presenter;

	/** 現在の言語に対応した文言を取得するコンバーター */
	private final MessageKeyConverter converter;

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

	/** プログラムから撮影範囲選択を更新中の場合true */
	private boolean updatingSnapAreaSelection;

	/** 保存先フォルダを開く操作が利用可能な場合true */
	private boolean saveDirectoryAvailable;

	/** 現在のテーマで使用する配色 */
	private ThemePalette currentThemePalette;

	/** 現在メインウィンドウへ適用しているテーマ */
	private ThemeType currentThemeType;

	/**
	 * メインウィンドウの部品を生成します。
	 *
	 * @param presenter パネル上の操作を受け取るプレゼンター
	 * @param converter 現在の言語に対応した文言を取得するコンバーター
	 */
	public MainWindowPanel(MainWindowPresenter presenter, MessageKeyConverter converter) {

		this.presenter = presenter;
		this.converter = converter;
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
		this.renderer = new ComboRenderer(ThemeType.DEFAULT, ThemeType.DEFAULT.getThemePalette());
		this.languageComboBox = createLanguageComboBox();
		this.snapAreaComboBox = createSnapAreaComboBox();
		this.openSaveDirectoryButton = createQuickActionButton(
				new QuickSettingIcon(QuickSettingIcon.Type.DIRECTORY));
		this.notificationButton = createNotificationButton(
				new QuickSettingIcon(QuickSettingIcon.Type.NOTIFICATION));

		applyMultilingualFont();
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

		// 左2列を使い、撮影範囲の長い表示名が見切れにくい配置にする。
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(8, 5, 0, 5);
		add(snapAreaComboBox, constraints);

		// 右下には、保存先を開く操作と通知表示の切替だけを小さく配置する。
		constraints.gridx = 2;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.NONE;
		add(createQuickSettingPanel(), constraints);
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
		button.setFocusable(true);
		button.setFocusPainted(true);
		button.addFocusListener(this);
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
			label.setFont(baseFont.deriveFont(SHORTCUT_FONT_SIZE));
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
		comboBox.setRenderer(renderer);
		comboBox.setFocusable(true);
		comboBox.setToolTipText("Language / 言語");
		comboBox.addFocusListener(this);
		comboBox.addActionListener(this);
		enableEnterPopupToggle(comboBox);
		return comboBox;
	}

	/**
	 * 撮影範囲を選択するコンボボックスを生成します。
	 * 表示項目は言語変更時にrefreshで作り直します。
	 *
	 * @return 撮影範囲の変更をこのパネルへ通知する選択欄
	 */
	private JComboBox<SnapAreaOption> createSnapAreaComboBox() {
		JComboBox<SnapAreaOption> comboBox = new JComboBox<>();
		comboBox.setPreferredSize(SNAP_AREA_COMBO_SIZE);
		comboBox.setMinimumSize(SNAP_AREA_COMBO_SIZE);
		comboBox.setRenderer(renderer);
		comboBox.setFocusable(true);
		comboBox.addFocusListener(this);
		comboBox.addActionListener(this);
		enableEnterPopupToggle(comboBox);
		return comboBox;
	}

	/**
	 * フォーカス中のコンボボックスをEnterキーで開閉できるようにします。
	 *
	 * @param comboBox Enterキー操作を追加するコンボボックス
	 */
	private void enableEnterPopupToggle(JComboBox<?> comboBox) {
		String actionKey = "toggleComboPopup";
		KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		comboBox.getInputMap(JComponent.WHEN_FOCUSED).put(enterKey, actionKey);
		comboBox.getActionMap().put(actionKey, new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				if (comboBox.isPopupVisible()) {
					comboBox.hidePopup();
					return;
				}
				comboBox.showPopup();
			}
		});
	}

	/**
	 * 保存先フォルダを開く正方形の小型ボタンを生成します。
	 *
	 * @param icon ボタンへ表示するフォルダアイコン
	 * @return このパネルへ操作を通知する小型ボタン
	 */
	private JButton createQuickActionButton(Icon icon) {
		JButton button = new JButton(icon);
		applyQuickButtonBaseSetting(button);
		return button;
	}

	/**
	 * 通知表示を切り替える正方形の小型ボタンを生成します。
	 *
	 * @param icon ボタンへ表示する通知アイコン
	 * @return 選択状態で通知の有効・無効を表す小型ボタン
	 */
	private JToggleButton createNotificationButton(Icon icon) {
		JToggleButton button = new JToggleButton(icon);
		applyQuickButtonBaseSetting(button);
		return button;
	}

	/**
	 * 小型ボタンで共通するサイズと操作設定を反映します。
	 *
	 * @param button 設定対象の小型ボタン
	 */
	private void applyQuickButtonBaseSetting(AbstractButton button) {
		button.setPreferredSize(QUICK_SETTING_BUTTON_SIZE);
		button.setMinimumSize(QUICK_SETTING_BUTTON_SIZE);
		button.setFocusable(true);
		button.setFocusPainted(true);
		button.setMargin(new Insets(2, 2, 2, 2));
		button.addFocusListener(this);
		button.addActionListener(this);
	}

	/**
	 * 右下の小型操作ボタンを横並びにします。
	 *
	 * @return 保存先フォルダと通知表示のボタンを持つ透明背景のパネル
	 */
	private JPanel createQuickSettingPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(0, 0, 0, 4);
		panel.add(openSaveDirectoryButton, constraints);

		constraints.gridx = 1;
		constraints.insets = new Insets(0, 0, 0, 0);
		panel.add(notificationButton, constraints);
		return panel;
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
			return;
		}
		if (eventSource == snapAreaComboBox && !updatingSnapAreaSelection) {
			Object selectedItem = snapAreaComboBox.getSelectedItem();
			if (selectedItem instanceof SnapAreaOption option) {
				presenter.changeSnapArea(option.snapAreaType());
			}
			return;
		}
		if (eventSource == openSaveDirectoryButton) {
			presenter.openSaveDirectory();
			return;
		}
		if (eventSource == notificationButton) {
			presenter.changeNotificationEnabled(notificationButton.isSelected());
		}
	}

	/**
	 * キーボードフォーカスを受け取った部品の枠線を更新します。
	 *
	 * @param event フォーカス取得イベント
	 */
	@Override
	public void focusGained(FocusEvent event) {
		updateFocusBorder(event.getComponent());
	}

	/**
	 * キーボードフォーカスが外れた部品の枠線を元に戻します。
	 *
	 * @param event フォーカス解除イベント
	 */
	@Override
	public void focusLost(FocusEvent event) {
		updateFocusBorder(event.getComponent());
	}

	/**
	 * 全テーマの操作部品へ、フォーカス状態に応じた枠線を設定します。
	 * 標準テーマでは外周に枠を重ね、カスタムテーマでは濃淡の異なる2色で表示します。
	 *
	 * @param component フォーカス状態が変わった部品
	 */
	private void updateFocusBorder(Component component) {
		if (currentThemePalette == null) {
			return;
		}

		if (currentThemeType == ThemeType.DEFAULT) {
			// OS標準の部品はフォーカスを受けても枠が見えないことがあるため、
			// 標準の枠と余白を保ったまま、フォーカス位置を外周に重ねて描く。
			Border baseBorder;
			if (component instanceof JComboBox<?>) {
				baseBorder = UIManager.getBorder("ComboBox.border");
			} else if (component == openSaveDirectoryButton || component == notificationButton) {
				baseBorder = BorderFactory.createLineBorder(currentThemePalette.getBorderColor());
			} else {
				baseBorder = UIManager.getBorder("Button.border");
			}
			if (component instanceof JComponent control) {
				control.setBorder(new FocusIndicatorBorder(baseBorder, currentThemePalette.getFocusColor()));
			}
			return;
		}

		Border focusBorder = BorderFactory.createLineBorder(currentThemePalette.getBorderColor());
		if (component.hasFocus()) {
			focusBorder = BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(currentThemePalette.getFocusGlowColor()),
					BorderFactory.createLineBorder(currentThemePalette.getFocusColor(), 2));
		}

		if (component instanceof JComboBox<?> comboBox) {
			comboBox.setBorder(focusBorder);
			return;
		}

		if (component instanceof AbstractButton button) {
			if (button == settingsButton || button == captureButton || button == rangeSelectionButton) {
				int verticalPadding = 4;
				int horizontalPadding = 6;
				if (component.hasFocus()) {
					verticalPadding = 2;
					horizontalPadding = 4;
				}
				button.setBorder(BorderFactory.createCompoundBorder(
						focusBorder,
						BorderFactory.createEmptyBorder(
								verticalPadding,
								horizontalPadding,
								verticalPadding,
								horizontalPadding)));
				return;
			}
			button.setBorder(focusBorder);
		}
	}

	/**
	 * 現在の設定値を文言、ショートカット、言語選択へ反映します。
	 * EDT上で呼び出します。渡された設定値の保存・変更は行いません。
	 *
	 * @param config 表示へ反映する最新の設定値
	 */
	public void refresh(ConfigBean config) {
		UiTexts texts = resolveUiTexts();
		settingsButton.setText(texts.settings());
		captureButton.setText(texts.capture());
		rangeSelectionButton.setText(texts.rangeSelection());
		updateShortcutLabel(captureShortcutLabel,
				resolveShortcutText(config.getSnapKeyBean(), texts.notSet()));
		updateShortcutLabel(rangeSelectionShortcutLabel,
				resolveShortcutText(config.getSelectionKeyBean(), texts.notSet()));
		refreshSnapAreaOptions(config.getSnapAreaType());
		notificationButton.setSelected(config.getIsShowNotification());

		String savePath = config.getSavePath();
		saveDirectoryAvailable = savePath != null && !savePath.isBlank();
		openSaveDirectoryButton.setEnabled(saveDirectoryAvailable);
		updateQuickSettingToolTips(texts, savePath);

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
	 * ミニウィンドウ内の文字へ、多言語の代替表示に対応するJava論理フォントを設定します。
	 * 言語変更のたびに切り替える必要がないため、パネル生成時に一度だけ呼び出します。
	 */
	private void applyMultilingualFont() {
		Font buttonFont = UIManager.getFont("Button.font");
		Font comboBoxFont = UIManager.getFont("ComboBox.font");
		Font labelFont = UIManager.getFont("Label.font");

		if (buttonFont != null) {
			buttonFont = new Font(Font.DIALOG, buttonFont.getStyle(), buttonFont.getSize());
			settingsButton.setFont(buttonFont);
			captureButton.setFont(buttonFont);
			rangeSelectionButton.setFont(buttonFont);
		}
		if (comboBoxFont != null) {
			comboBoxFont = new Font(Font.DIALOG, comboBoxFont.getStyle(), comboBoxFont.getSize());
			languageComboBox.setFont(comboBoxFont);
			snapAreaComboBox.setFont(comboBoxFont);
		}
		if (labelFont != null) {
			labelFont = new Font(Font.DIALOG, labelFont.getStyle(), labelFont.getSize());
			captureShortcutLabel.setFont(labelFont.deriveFont(SHORTCUT_FONT_SIZE));
			rangeSelectionShortcutLabel.setFont(labelFont.deriveFont(SHORTCUT_FONT_SIZE));
		}
	}

	/**
	 * 設定画面で選択されたテーマをミニウィンドウへ反映します。
	 * デフォルトではSwing標準UIを使い、ダークと絵本では共通Paletteの配色を使います。
	 *
	 * @param themeType 最新のテーマ設定
	 */
	private void applyTheme(ThemeType themeType) {
		renderer.setTheme(themeType);
		applyActionButtonIcons(themeType);
		ThemePalette themePalette = themeType.getThemePalette();
		this.currentThemePalette = themePalette;
		this.currentThemeType = themeType;

		if (themeType == ThemeType.DEFAULT) {
			applyDefaultTheme(themePalette);
		} else {
			applyCustomTheme(themePalette);
		}

		languageComboBox.repaint();
		snapAreaComboBox.repaint();
		updateQuickSettingButtonColors();
		updateFocusBorder(languageComboBox);
		updateFocusBorder(snapAreaComboBox);
		updateFocusBorder(settingsButton);
		updateFocusBorder(captureButton);
		updateFocusBorder(rangeSelectionButton);
		updateFocusBorder(openSaveDirectoryButton);
		updateFocusBorder(notificationButton);
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
		setBackground(themePalette.getBackgroundColor());
		captureShortcutLabel.setForeground(themePalette.getMutedTextColor());
		rangeSelectionShortcutLabel.setForeground(themePalette.getMutedTextColor());

		languageComboBox.setRenderer(renderer);
		languageComboBox.setBorder(BorderFactory.createLineBorder(themePalette.getBorderColor()));
		snapAreaComboBox.setRenderer(renderer);
		snapAreaComboBox.setBorder(BorderFactory.createLineBorder(themePalette.getBorderColor()));

		Border buttonBorder = BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(themePalette.getBorderColor()),
				BorderFactory.createEmptyBorder(4, 6, 4, 6));
		for (JButton button : getActionButtons()) {
			// OS標準スキンによる色の上書きを避け、テーマ配色を確実に描画する。
			button.setUI(new BasicButtonUI());
			button.setOpaque(true);
			button.setBackground(themePalette.getControlBackgroundColor());
			button.setForeground(themePalette.getTextColor());
			button.setBorder(buttonBorder);
		}
	}

	/**
	 * カスタムテーマから切り替えたとき、Swing標準のUIと共通配色へ戻します。
	 *
	 * @param themePalette デフォルトテーマのSwing配色
	 */
	private void applyDefaultTheme(ThemePalette themePalette) {
		setBackground(themePalette.getBackgroundColor());
		captureShortcutLabel.setForeground(themePalette.getTextColor());
		rangeSelectionShortcutLabel.setForeground(themePalette.getTextColor());

		languageComboBox.setBackground(themePalette.getControlBackgroundColor());
		languageComboBox.setForeground(themePalette.getTextColor());
		languageComboBox.setBorder(UIManager.getBorder("ComboBox.border"));
		snapAreaComboBox.setBackground(themePalette.getControlBackgroundColor());
		snapAreaComboBox.setForeground(themePalette.getTextColor());
		snapAreaComboBox.setBorder(UIManager.getBorder("ComboBox.border"));

		Border buttonBorder = UIManager.getBorder("Button.border");
		for (JButton button : getActionButtons()) {
			// カスタムテーマ専用UIを外し、OSに対応したSwing標準UIへ戻す。
			button.updateUI();
			button.setBackground(themePalette.getControlBackgroundColor());
			button.setForeground(themePalette.getTextColor());
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
	 * ショートカットの表示文言と、全文を確認するためのツールチップを更新します。
	 *
	 * @param label 更新対象のショートカット表示ラベル
	 * @param shortcutText 表示するショートカット名
	 */
	private void updateShortcutLabel(JLabel label, String shortcutText) {
		label.setText(shortcutText);
		label.setToolTipText(shortcutText);
	}

	/**
	 * 選択言語に対応した撮影範囲の一覧を作り直し、現在値を選択します。
	 * 一覧の再作成中に発生するActionEventは設定変更として扱いません。
	 *
	 * @param selectedType 現在設定されている撮影範囲
	 */
	private void refreshSnapAreaOptions(SnapAreaType selectedType) {
		updatingSnapAreaSelection = true;
		try {
			snapAreaComboBox.removeAllItems();
			SnapAreaOption selectedOption = null;
			for (SnapAreaType snapAreaType : SnapAreaType.values()) {
				SnapAreaOption option = new SnapAreaOption(
						snapAreaType,
						presenter.resolveSnapAreaDisplayName(snapAreaType));
				snapAreaComboBox.addItem(option);
				if (snapAreaType == selectedType) {
					selectedOption = option;
				}
			}
			if (selectedOption != null) {
				snapAreaComboBox.setSelectedItem(selectedOption);
				snapAreaComboBox.setToolTipText(selectedOption.displayName());
			}
		} finally {
			updatingSnapAreaSelection = false;
		}
	}

	/**
	 * 小型操作ボタンへ現在の保存先と通知状態を含むツールチップを設定します。
	 *
	 * @param texts 現在の表示言語に対応する文言
	 * @param savePath 現在設定されている保存先パス
	 */
	private void updateQuickSettingToolTips(UiTexts texts, String savePath) {
		String directoryToolTip = texts.saveDirectoryNotSet();
		if (saveDirectoryAvailable) {
			directoryToolTip = texts.openSaveDirectory() + ": " + savePath;
		}
		openSaveDirectoryButton.setToolTipText(directoryToolTip);

		String notificationState = texts.disabled();
		if (notificationButton.isSelected()) {
			notificationState = texts.enabled();
		}
		notificationButton.setToolTipText(texts.notification() + ": " + notificationState);
	}

	/** 現在のテーマと選択状態を小型操作ボタンへ反映します。 */
	private void updateQuickSettingButtonColors() {
		if (currentThemePalette == null) {
			return;
		}
		for (AbstractButton button : getQuickSettingButtons()) {
			button.setUI(new BasicButtonUI());
			button.setOpaque(true);
			button.setForeground(currentThemePalette.getTextColor());
			button.setBorder(BorderFactory.createLineBorder(currentThemePalette.getBorderColor()));
			if (button == notificationButton && notificationButton.isSelected()) {
				button.setBackground(currentThemePalette.getSelectionBackgroundColor());
			} else {
				button.setBackground(currentThemePalette.getControlBackgroundColor());
			}
			button.repaint();
		}
	}

	/**
	 * 同じテーマを適用する小型操作ボタンを返します。
	 *
	 * @return 保存先フォルダと通知表示の小型ボタン
	 */
	private AbstractButton[] getQuickSettingButtons() {
		return new AbstractButton[] { openSaveDirectoryButton, notificationButton };
	}

	/**
	 * 現在の言語に対応したメインウィンドウ用の短い文言を取得します。
	 *
	 * @return ボタン名とショートカット未設定時の文言
	 */
	private UiTexts resolveUiTexts() {
		return new UiTexts(
				converter.getMessage(MessageKeyConstant.MAIN_WINDOW_SETTINGS),
				converter.getMessage(MessageKeyConstant.MAIN_WINDOW_CAPTURE),
				converter.getMessage(MessageKeyConstant.MAIN_WINDOW_SELECT_AREA),
				converter.getMessage(MessageKeyConstant.MAIN_WINDOW_SHORTCUT_NOT_SET),
				converter.getMessage(MessageKeyConstant.MAIN_WINDOW_OPEN_SAVE_DIRECTORY),
				converter.getMessage(MessageKeyConstant.MAIN_WINDOW_SAVE_DIRECTORY_NOT_SET),
				converter.getMessage(MessageKeyConstant.MAIN_WINDOW_NOTIFICATION),
				converter.getEnabledMessage(true),
				converter.getEnabledMessage(false));
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
		snapAreaComboBox.setEnabled(enabled);
		notificationButton.setEnabled(enabled);
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
		openSaveDirectoryButton.setEnabled(enabled && saveDirectoryAvailable);
	}

	/**
	 * 標準テーマの枠にフォーカス表示を重ねる。
	 * 枠の余白は元のままにし、フォーカス移動で文字やアイコンの位置を変えない。
	 */
	private static final class FocusIndicatorBorder extends AbstractBorder {

		private static final long serialVersionUID = 1L;

		/** OS標準または小型ボタンの通常の枠 */
		private final Border baseBorder;

		/** フォーカス中の外周に使う色 */
		private final Color focusColor;

		private FocusIndicatorBorder(Border baseBorder, Color focusColor) {
			this.baseBorder = baseBorder;
			this.focusColor = focusColor;
		}

		@Override
		public Insets getBorderInsets(Component component) {
			return getBorderInsets(component, new Insets(0, 0, 0, 0));
		}

		@Override
		public Insets getBorderInsets(Component component, Insets insets) {
			Insets baseInsets = baseBorder == null ? new Insets(0, 0, 0, 0) : baseBorder.getBorderInsets(component);
			insets.set(baseInsets.top, baseInsets.left, baseInsets.bottom, baseInsets.right);
			return insets;
		}

		@Override
		public void paintBorder(Component component, Graphics graphics, int x, int y, int width, int height) {
			if (baseBorder != null) {
				baseBorder.paintBorder(component, graphics, x, y, width, height);
			}
			if (!component.hasFocus() || !component.isEnabled()) {
				return;
			}

			Graphics focusGraphics = graphics.create();
			try {
				focusGraphics.setColor(focusColor);
				focusGraphics.drawRect(x, y, width - 1, height - 1);
				focusGraphics.drawRect(x + 1, y + 1, width - 3, height - 3);
			} finally {
				focusGraphics.dispose();
			}
		}
	}

	/**
	 * メインウィンドウで使う短い文言です。
	 *
	 * @param settings 設定ボタン名
	 * @param capture 撮影ボタン名
	 * @param rangeSelection 範囲選択ボタン名
	 * @param notSet ショートカット未設定時の文言
	 */
	private record UiTexts(
			String settings,
			String capture,
			String rangeSelection,
			String notSet,
			String openSaveDirectory,
			String saveDirectoryNotSet,
			String notification,
			String enabled,
			String disabled) {
	}

	/**
	 * 撮影範囲コンボボックスへ表示する値です。
	 *
	 * @param snapAreaType 設定へ保存する撮影範囲
	 * @param displayName 現在の言語に対応した表示名
	 */
	private record SnapAreaOption(SnapAreaType snapAreaType, String displayName) {
		@Override
		public String toString() {
			return displayName;
		}
	}
}
