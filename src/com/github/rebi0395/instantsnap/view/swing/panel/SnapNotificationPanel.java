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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import com.github.rebi0395.instantsnap.view.swing.presenter.SnapNotificationPresenter;

/**
 * 撮影結果通知画面のSwingパネルクラスです。
 * <p>
 * JavaFX版でFXMLと通知コントローラが担当していた、通知部品の生成、
 * 文言とアクションの設定、背景描画をこのクラスにまとめています。
 * </p>
 */
public final class SnapNotificationPanel extends JPanel implements ActionListener {

	/** シリアルバージョンUID */
	private static final long serialVersionUID = 1L;

	/** 通知全体の横幅 */
	private static final int PANEL_WIDTH = 376;

	/** 影描画用の外側余白 */
	private static final int SHADOW_SIZE = 8;

	/** 通知のアクセント色 */
	private static final Color ACCENT_COLOR = new Color(0x09, 0x69, 0xda);

	/** 保存先表示へマウスを乗せたときの色 */
	private static final Color ACTION_HOVER_COLOR = new Color(0x07, 0x57, 0xb8);

	/** 保存先表示を押しているときの色 */
	private static final Color ACTION_PRESSED_COLOR = new Color(0x05, 0x4a, 0x9b);

	/** 通知の背景色 */
	private static final Color NOTIFICATION_BACKGROUND_COLOR = Color.WHITE;

	/** 通知の影色 */
	private static final Color SHADOW_COLOR = new Color(0, 0, 0, 7);

	/** 通知の枠線色 */
	private static final Color BORDER_COLOR = new Color(0xd0, 0xd7, 0xde);

	/** JavaFXの日本語Systemフォントに近いWindows標準フォント */
	private static final String NOTIFICATION_FONT_FAMILY = "Yu Gothic UI";

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

	/** 保存先を開く操作を表示するラベル */
	private final JLabel labelOpenFolder;

	/** 通知を閉じるボタン */
	private final JButton buttonClose;

	/** 通知上の操作を受け取るプレゼンター */
	private final SnapNotificationPresenter presenter;

	/**
	 * 通知に必要なSwing部品を生成して配置します。
	 * EDT上で生成し、文言はsetupで設定します。
	 *
	 * @param presenter 保存先操作と通知を閉じる処理の呼び出し先
	 */
	public SnapNotificationPanel(SnapNotificationPresenter presenter) {
		this.presenter = presenter;
		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// JavaFX版のアクセント幅6px、本文余白14/16/14/12pxへ影の余白を加える。
		setBorder(new EmptyBorder(22, 26, 22, 24));

		labelTitle = createTitleLabel();
		buttonClose = createCloseButton();
		textAreaMessage = createMessageArea();
		labelOpenFolder = createOpenFolderLabel();
		panelOpenFolder = createOpenFolderPanel();
		// 文言の再設定でリスナーが増えないよう、イベント登録は生成時に一度だけ行う。
		buttonClose.addActionListener(this);
		registerOpenFolderRequest();
		registerOpenDirectoryRequest();

		add(createHeaderPanel());
		add(Box.createVerticalStrut(7));
		add(textAreaMessage);
		add(panelOpenFolder);

		getAccessibleContext().setAccessibleName("InstantSnap notification");
	}

	/**
	 * 通知の文言と保存先を開けるかどうかを設定します。
	 *
	 * @param messageList 表示するメッセージリスト
	 * @param openMessage クリック時にフォルダを開けることを通知するメッセージ
	 * @param openDirectoryAvailable 保存先を開ける場合true
	 */
	public void setup(
			List<String> messageList,
			String openMessage,
			boolean openDirectoryAvailable) {

		// メッセージを設定する。
		labelTitle.setText("InstantSnap");
		textAreaMessage.setText(String.join(System.lineSeparator(), messageList));

		// 画像保存があり、フォルダを開く処理を搭載する場合だけボタンを表示する。
		if (openDirectoryAvailable) {
			labelOpenFolder.setText(openMessage);
			// FlowLayoutとBoxLayoutの丸めで末尾が省略されないよう、計測幅へ余裕を持たせる。
			labelOpenFolder.setPreferredSize(null);
			Dimension openFolderSize = labelOpenFolder.getPreferredSize();
			openFolderSize.width += 6;
			labelOpenFolder.setPreferredSize(openFolderSize);
			labelOpenFolder.setBackground(ACCENT_COLOR);
			panelOpenFolder.setVisible(true);
			textAreaMessage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else {
			panelOpenFolder.setVisible(false);
			labelOpenFolder.setText(null);
			textAreaMessage.setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * 通知上のボタン操作に対応するPresenterのメソッドを呼び出します。
	 *
	 * @param event 保存先を開くボタン、または閉じるボタンの操作イベント
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object eventSource = event.getSource();
		if (eventSource == buttonClose) {
			presenter.closeNotification();
		}
	}

	/**
	 * タイトルと閉じるボタンを配置したヘッダーを作成します。
	 *
	 * @return 左にタイトル、右に閉じるボタンを配置した透明背景のパネル
	 */
	private JPanel createHeaderPanel() {
		JPanel headerPanel = new JPanel(new BorderLayout(8, 0));
		headerPanel.setOpaque(false);
		headerPanel.setAlignmentX(LEFT_ALIGNMENT);
		headerPanel.add(labelTitle, BorderLayout.CENTER);
		headerPanel.add(buttonClose, BorderLayout.EAST);
		return headerPanel;
	}

	/**
	 * 通知タイトルを表示するラベルを作成します。
	 *
	 * @return アクセント色と太字を設定したタイトルラベル
	 */
	private JLabel createTitleLabel() {
		JLabel titleLabel = new JLabel();
		titleLabel.setForeground(ACCENT_COLOR);
		titleLabel.setFont(createNotificationFont(Font.BOLD, 17.0f));
		return titleLabel;
	}

	/**
	 * 通知を閉じるボタンを作成します。
	 *
	 * @return 枠と背景を描画しない閉じるボタン
	 */
	private JButton createCloseButton() {
		JButton closeButton = new JButton("×");
		closeButton.setForeground(CLOSE_COLOR);
		closeButton.setFont(createNotificationFont(Font.BOLD, 20.0f));
		closeButton.setPreferredSize(new Dimension(30, 30));
		closeButton.setHorizontalAlignment(SwingConstants.CENTER);
		closeButton.setBorder(BorderFactory.createEmptyBorder());
		closeButton.setContentAreaFilled(false);
		closeButton.setFocusPainted(false);
		closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		closeButton.setToolTipText("Close");
		return closeButton;
	}

	/**
	 * 改行と折り返しに対応した通知本文を作成します。
	 *
	 * @return 編集・フォーカス取得を無効にした通知本文の表示部品
	 */
	private JTextArea createMessageArea() {
		JTextArea messageArea = new JTextArea();
		messageArea.setFont(createNotificationFont(Font.PLAIN, 13.0f));
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

	/**
	 * 保存先を開くボタンを配置する行を作成します。
	 *
	 * @return 保存先を開くボタンを左寄せに配置した透明背景のパネル
	 */
	private JPanel createOpenFolderPanel() {
		JPanel openFolderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		openFolderPanel.setOpaque(false);
		openFolderPanel.setAlignmentX(LEFT_ALIGNMENT);
		openFolderPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		openFolderPanel.add(labelOpenFolder);
		return openFolderPanel;
	}

	/**
	 * JavaFX版と同じ配色・余白の、保存先を開くアクション表示を作成します。
	 * SwingのLook &amp; Feelによるボタン色の上書きを避けるため、JLabelで描画します。
	 *
	 * @return 文言をsetupで設定する保存先ラベル
	 */
	private JLabel createOpenFolderLabel() {
		JLabel openFolderLabel = new JLabel();
		openFolderLabel.setFont(createNotificationFont(Font.BOLD, 12.0f));
		openFolderLabel.setForeground(Color.WHITE);
		openFolderLabel.setBackground(ACCENT_COLOR);
		openFolderLabel.setOpaque(true);
		openFolderLabel.setBorder(new EmptyBorder(6, 12, 6, 12));
		openFolderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		openFolderLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return openFolderLabel;
	}

	/**
	 * 保存先ラベルへクリックとマウスオーバー時の表示を登録します。
	 * リスナーはパネル生成時に一度だけ登録し、通知の再利用では増やしません。
	 */
	private void registerOpenFolderRequest() {
		labelOpenFolder.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				presenter.openSaveDirectory();
			}

			@Override
			public void mouseEntered(MouseEvent event) {
				labelOpenFolder.setBackground(ACTION_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent event) {
				labelOpenFolder.setBackground(ACCENT_COLOR);
			}

			@Override
			public void mousePressed(MouseEvent event) {
				labelOpenFolder.setBackground(ACTION_PRESSED_COLOR);
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				if (labelOpenFolder.contains(event.getPoint())) {
					labelOpenFolder.setBackground(ACTION_HOVER_COLOR);
				} else {
					labelOpenFolder.setBackground(ACCENT_COLOR);
				}
			}
		});
	}

	/**
	 * 本文クリックでもPresenterへ保存先を開くよう要求します。
	 */
	private void registerOpenDirectoryRequest() {
		textAreaMessage.addMouseListener(new MouseAdapter() {
			/**
			 * 本文クリックを保存先操作へ渡します。保存先の有無はPresenterが判定します。
			 *
			 * @param event 本文上で発生したクリックイベント
			 */
			@Override
			public void mouseClicked(MouseEvent event) {
				presenter.openSaveDirectory();
			}
		});
	}

	/**
	 * JavaFX版のSystemフォントに近いフォントを生成します。
	 * Yu Gothic UIがない環境では、現在のSwing Look &amp; Feelのラベルフォントを使用します。
	 *
	 * @param style {@link Font#PLAIN}または{@link Font#BOLD}
	 * @param size 文字サイズ
	 * @return 通知表示用フォント
	 */
	private Font createNotificationFont(int style, float size) {
		Font notificationFont = new Font(NOTIFICATION_FONT_FAMILY, style, Math.round(size));
		if (NOTIFICATION_FONT_FAMILY.equalsIgnoreCase(notificationFont.getFamily())) {
			return notificationFont.deriveFont(style, size);
		}

		Font fallbackFont = UIManager.getFont("Label.font");
		return fallbackFont.deriveFont(style, size);
	}

	/**
	 * 通知内容から算出した高さを維持し、横幅だけを固定します。
	 *
	 * @return 通知本文の折り返しを含めた推奨表示サイズ
	 */
	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();
		preferredSize.width = PANEL_WIDTH;
		return preferredSize;
	}

	/**
	 * 通知の背景、影、左側のアクセントラインを描画します。
	 *
	 * @param graphics Swingから渡された描画コンテキスト
	 */
	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);

		// Swingから借りたGraphicsを直接変更せず、複製した描画コンテキストだけを解放する。
		Graphics2D graphics2d = (Graphics2D) graphics.create();
		try {
			graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int x = SHADOW_SIZE;
			int y = SHADOW_SIZE;
			int width = getWidth() - (SHADOW_SIZE * 2);
			int height = getHeight() - (SHADOW_SIZE * 2);

			// 複数の半透明枠を重ね、JavaFX版に近い柔らかな影を描画する。
			for (int spread = 7; spread >= 1; spread--) {
				graphics2d.setColor(SHADOW_COLOR);
				graphics2d.fillRoundRect(
						x - spread,
						y - spread + 3,
						width + (spread * 2),
						height + (spread * 2),
						14,
						14);
			}

			// 通知本体となる白い角丸背景を描画する。
			graphics2d.setColor(NOTIFICATION_BACKGROUND_COLOR);
			graphics2d.fillRoundRect(x, y, width, height, 12, 12);

			// JavaFX版のnotification-accentに相当する青いラインを左側へ描画する。
			graphics2d.setColor(ACCENT_COLOR);
			graphics2d.fillRoundRect(x, y, 10, height, 12, 12);
			graphics2d.fillRect(x + 5, y, 5, height);

			// 通知の輪郭が背景に埋もれないよう、外周へ細い枠線を描画する。
			graphics2d.setColor(BORDER_COLOR);
			graphics2d.setStroke(new BasicStroke(1.0f));
			graphics2d.drawRoundRect(x, y, width, height, 12, 12);
		} finally {
			graphics2d.dispose();
		}
	}
}
