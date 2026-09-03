package com.github.rebi0395.instantsnap.view.swing.panel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * 撮影結果通知画面のSwingパネルクラスです。
 * <p>
 * JavaFX版でFXMLと通知コントローラが担当していた、通知部品の生成、
 * 文言とアクションの設定、背景描画をこのクラスにまとめています。
 * </p>
 */
public final class SnapNotificationPanel extends JPanel {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 通知全体の横幅 */
	private static final int PANEL_WIDTH = 376;

	/** 影描画用の外側余白 */
	private static final int SHADOW_SIZE = 8;

	/** 通知のアクセント色 */
	private static final Color ACCENT_COLOR = new Color(0x09, 0x69, 0xda);

	/** 本文色 */
	private static final Color TEXT_COLOR = new Color(0x24, 0x29, 0x2f);

	/** 閉じるボタンの文字色 */
	private static final Color CLOSE_COLOR = new Color(0x57, 0x60, 0x6a);

	/** 通知タイトル */
	private final JLabel labelTitle;

	/** 通知メッセージ */
	private final JTextArea textAreaMessage;

	/** 保存先を開くボタンを配置する行 */
	private final JPanel panelOpenFolder;

	/** 保存先を開くボタン */
	private final JButton buttonOpenFolder;

	/** 通知を閉じるボタン */
	private final JButton buttonClose;

	/**
	 * 通知に必要なSwing部品を生成して配置します。
	 */
	public SnapNotificationPanel() {
		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new EmptyBorder(18, 30, 18, 24));

		labelTitle = createTitleLabel();
		buttonClose = createCloseButton();
		textAreaMessage = createMessageArea();
		buttonOpenFolder = createOpenFolderButton();
		panelOpenFolder = createOpenFolderPanel();

		add(createHeaderPanel());
		add(Box.createVerticalStrut(7));
		add(textAreaMessage);
		add(panelOpenFolder);

		getAccessibleContext().setAccessibleName("InstantSnap notification");
	}

	/**
	 * 通知の文言やアクションを設定します。
	 *
	 * @param messageList 表示するメッセージリスト
	 * @param openMessage クリック時にフォルダを開けることを通知するメッセージ
	 * @param openAction クリック時にフォルダを開く動作。画像保存でない場合はnull
	 * @param closeAction 通知を閉じる動作
	 */
	public void setup(
			List<String> messageList,
			String openMessage,
			Runnable openAction,
			Runnable closeAction) {

		// メッセージを設定する。
		labelTitle.setText("InstantSnap");
		textAreaMessage.setText(String.join(System.lineSeparator(), messageList));

		// 画像保存があり、フォルダを開く処理を搭載する場合だけボタンを表示する。
		if (openAction != null) {
			buttonOpenFolder.setText(openMessage);
			panelOpenFolder.setVisible(true);
			buttonOpenFolder.addActionListener(event -> {
				openAction.run();
			});
			registerOpenAction(openAction);
		} else {
			panelOpenFolder.setVisible(false);
			buttonOpenFolder.setText(null);
		}

		// 閉じるボタン押下時に通知を閉じるアクションを設定する。
		buttonClose.addActionListener(event -> {
			closeAction.run();
		});
	}

	/** タイトルと閉じるボタンを配置したヘッダーを作成します。 */
	private JPanel createHeaderPanel() {
		JPanel headerPanel = new JPanel(new BorderLayout(8, 0));
		headerPanel.setOpaque(false);
		headerPanel.setAlignmentX(LEFT_ALIGNMENT);
		headerPanel.add(labelTitle, BorderLayout.CENTER);
		headerPanel.add(buttonClose, BorderLayout.EAST);
		return headerPanel;
	}

	/** 通知タイトルを表示するラベルを作成します。 */
	private JLabel createTitleLabel() {
		Font baseFont = UIManager.getFont("Label.font");
		JLabel titleLabel = new JLabel();
		titleLabel.setForeground(ACCENT_COLOR);
		titleLabel.setFont(baseFont.deriveFont(Font.BOLD, 17.0f));
		return titleLabel;
	}

	/** 通知を閉じるボタンを作成します。 */
	private JButton createCloseButton() {
		Font baseFont = UIManager.getFont("Label.font");
		JButton closeButton = new JButton("×");
		closeButton.setForeground(CLOSE_COLOR);
		closeButton.setFont(baseFont.deriveFont(Font.BOLD, 20.0f));
		closeButton.setPreferredSize(new Dimension(30, 30));
		closeButton.setHorizontalAlignment(SwingConstants.CENTER);
		closeButton.setBorder(BorderFactory.createEmptyBorder());
		closeButton.setContentAreaFilled(false);
		closeButton.setFocusPainted(false);
		closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		closeButton.setToolTipText("Close");
		return closeButton;
	}

	/** 改行と折り返しに対応した通知本文を作成します。 */
	private JTextArea createMessageArea() {
		Font baseFont = UIManager.getFont("Label.font");
		JTextArea messageArea = new JTextArea();
		messageArea.setFont(baseFont.deriveFont(Font.PLAIN, 13.0f));
		messageArea.setForeground(TEXT_COLOR);
		messageArea.setOpaque(false);
		messageArea.setEditable(false);
		messageArea.setFocusable(false);
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		messageArea.setColumns(31);
		messageArea.setAlignmentX(LEFT_ALIGNMENT);
		return messageArea;
	}

	/** 保存先を開くボタンを配置する行を作成します。 */
	private JPanel createOpenFolderPanel() {
		JPanel openFolderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		openFolderPanel.setOpaque(false);
		openFolderPanel.setAlignmentX(LEFT_ALIGNMENT);
		openFolderPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		openFolderPanel.add(buttonOpenFolder);
		return openFolderPanel;
	}

	/** 保存先を開く青いアクションボタンを作成します。 */
	private JButton createOpenFolderButton() {
		Font baseFont = UIManager.getFont("Button.font");
		JButton openFolderButton = new JButton();
		openFolderButton.setFont(baseFont.deriveFont(Font.BOLD, 12.0f));
		openFolderButton.setForeground(Color.WHITE);
		openFolderButton.setBackground(ACCENT_COLOR);
		openFolderButton.setOpaque(true);
		openFolderButton.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(ACCENT_COLOR, 1, true),
				new EmptyBorder(6, 12, 6, 12)));
		openFolderButton.setFocusPainted(false);
		openFolderButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return openFolderButton;
	}

	/** 本文クリックでも保存先を開けるようにします。 */
	private void registerOpenAction(Runnable openAction) {
		textAreaMessage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		textAreaMessage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				openAction.run();
			}
		});
	}

	/** 通知内容から算出した高さを維持し、横幅だけを固定します。 */
	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();
		preferredSize.width = PANEL_WIDTH;
		return preferredSize;
	}

	/** 通知の背景、影、左側のアクセントラインを描画します。 */
	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);

		Graphics2D graphics2d = (Graphics2D) graphics.create();
		try {
			graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int x = SHADOW_SIZE;
			int y = SHADOW_SIZE;
			int width = getWidth() - (SHADOW_SIZE * 2);
			int height = getHeight() - (SHADOW_SIZE * 2);

			// 複数の半透明枠を重ね、JavaFX版に近い柔らかな影を描画する。
			for (int spread = 7; spread >= 1; spread--) {
				graphics2d.setColor(new Color(0, 0, 0, 7));
				graphics2d.fillRoundRect(
						x - spread,
						y - spread + 3,
						width + (spread * 2),
						height + (spread * 2),
						14,
						14);
			}

			// 通知本体となる白い角丸背景を描画する。
			graphics2d.setColor(Color.WHITE);
			graphics2d.fillRoundRect(x, y, width, height, 12, 12);

			// JavaFX版のnotification-accentに相当する青いラインを左側へ描画する。
			graphics2d.setColor(ACCENT_COLOR);
			graphics2d.fillRoundRect(x, y, 10, height, 12, 12);
			graphics2d.fillRect(x + 5, y, 5, height);

			// 通知の輪郭が背景に埋もれないよう、外周へ細い枠線を描画する。
			graphics2d.setColor(new Color(0xd0, 0xd7, 0xde));
			graphics2d.setStroke(new BasicStroke(1.0f));
			graphics2d.drawRoundRect(x, y, width, height, 12, 12);
		} finally {
			graphics2d.dispose();
		}
	}
}
