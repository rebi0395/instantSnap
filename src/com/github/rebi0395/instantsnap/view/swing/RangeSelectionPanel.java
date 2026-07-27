package com.github.rebi0395.instantsnap.view.swing;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.rebi0395.instantsnap.view.swing.RangeSelectionWindow.SelectionListener;

/**
 * 範囲選択画面の暗幕描画とマウス操作を行うSwingパネル
 */
class RangeSelectionPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/** 選択範囲外の暗幕色 */
	private static final Color SHADE_COLOR = new Color(0, 0, 0, 89);

	/**
	 * 完全透明によるマウスイベントのすり抜けを防止する背景色。
	 * 透明度は目視できない最小値にする。
	 */
	private static final Color INTERACTION_COLOR = new Color(0, 0, 0, 1);

	/** 選択範囲の枠線色 */
	private static final Color SELECTION_BORDER_COLOR = Color.WHITE;

	/** 選択範囲の枠線幅 */
	private static final float SELECTION_BORDER_WIDTH = 2.0f;

	/** 範囲選択結果の通知先 */
	private final SelectionListener listener;

	/** パネルを表示しているモニターのAWT座標 */
	private final Rectangle screenArea;

	/** ドラッグ開始位置 */
	private Point startPoint;

	/** 現在の選択範囲。パネル左上を原点とする */
	private Rectangle selectionArea;

	/** ドラッグ中ならtrue */
	private boolean dragging;

	/**
	 * コンストラクタ
	 *
	 * @param screenArea パネルを表示するモニターのAWT座標
	 * @param listener 範囲選択結果の通知先
	 */
	public RangeSelectionPanel(Rectangle screenArea, SelectionListener listener) {
		this.screenArea = new Rectangle(screenArea);
		this.listener = listener;

		// 背景を透過し、範囲選択用のカーソルに変更する
		setOpaque(false);
		setFocusable(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		registerMouseEvents(); // マウスイベントの登録
	}

	/**
	 * 選択状態を初期化する。
	 */
	public void resetSelection() {
		startPoint = null; // ドラッグ開始位置
		selectionArea = null; // 選択範囲
		dragging = false; // ドラッグ中か

		repaint(); // 選択範囲と枠線を消し、全面を暗幕で覆う
	}

	/**
	 * 暗幕、選択範囲、ドラッグ中の枠線を描画する。
	 *
	 * @param graphics 描画オブジェクト
	 */
	@Override
	protected void paintComponent(Graphics graphics) {
		Graphics2D graphics2D = (Graphics2D) graphics.create();

		try {
			// 前回描画した暗幕を消し、透明な状態から描き直す
			graphics2D.setComposite(AlphaComposite.Clear);
			graphics2D.fillRect(0, 0, getWidth(), getHeight());
			graphics2D.setComposite(AlphaComposite.SrcOver);

			// 選択範囲内でもマウスイベントを受け取れるよう、ごく薄い背景を全面に描画する
			graphics2D.setColor(INTERACTION_COLOR);
			graphics2D.fillRect(0, 0, getWidth(), getHeight());

			drawShadeAreas(graphics2D);

			if (dragging && selectionArea != null) {
				drawSelectionBorder(graphics2D);
			}

		} finally {
			graphics2D.dispose();
		}
	}

	/**
	 * マウス押下、ドラッグ、解放時の処理を登録する。
	 */
	private void registerMouseEvents() {
		MouseAdapter mouseHandler = new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent event) {
				// マウスボタン押下時
				startSelection(event);
			}

			@Override
			public void mouseDragged(MouseEvent event) {
				// マウスをドラッグしたとき
				dragSelection(event);
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				// マウスボタンを離したとき
				confirmSelection(event);
			}
		};

		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
	}

	/**
	 * ドラッグによる範囲選択を開始する。
	 *
	 * @param event マウスイベント
	 */
	private void startSelection(MouseEvent event) {
		if (!SwingUtilities.isLeftMouseButton(event)) {
			// マウスイベントのメインボタンでないとき、早期リターン
			return;
		}

		// 通知先の範囲選択をリセットする
		listener.onUpdatedArea(null);

		// ドラッグ開始位置を設定する
		startPoint = clampPoint(event.getPoint());

		// 押下位置から新しい選択範囲を開始する
		selectionArea = new Rectangle(startPoint.x, startPoint.y, 0, 0);
		dragging = true; // ドラッグ中フラグを設定
		repaint();
	}

	/**
	 * ドラッグ中の選択範囲を更新する。
	 *
	 * @param event マウスイベント
	 */
	private void dragSelection(MouseEvent event) {
		boolean leftButtonDown = (event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
		if (!dragging || !leftButtonDown) {
			// ドラッグ中フラグがfalse、マウスボタンが押されていない場合は早期リターン
			return;
		}

		// ドラッグした最新位置を変数で設定する
		Point currentPoint = clampPoint(event.getPoint());

		// 選択範囲を更新する
		updateSelection(currentPoint);
	}

	/**
	 * マウスを離した位置で選択範囲を確定する。
	 *
	 * @param event マウスイベント
	 */
	private void confirmSelection(MouseEvent event) {
		if (!dragging || !SwingUtilities.isLeftMouseButton(event)) {
			// ドラッグ中フラグがfalse、マウスボタンでない場合は早期リターン
			return;
		}

		// ドラッグした最新位置を変数で設定する
		Point currentPoint = clampPoint(event.getPoint());

		// 選択範囲を更新する
		updateSelection(currentPoint);
		dragging = false; // フラグを更新する

		if (selectionArea.width <= 0 || selectionArea.height <= 0) {
			// 実際にドラッグしていなければ通常クリックとして選択状態を解除する
			selectionArea = null;
			paintImmediately(0, 0, getWidth(), getHeight());
			listener.onUpdatedArea(null);
			return;
		}

		// パネル内の座標へモニター原点を加え、スクリーン座標を算出する
		Rectangle selectedScreenArea = new Rectangle(
				screenArea.x + selectionArea.x,
				screenArea.y + selectionArea.y,
				selectionArea.width,
				selectionArea.height);

		// 撮影可能の通知より先に再描画し、撮影画像へ白い枠線が入ることを防ぐ
		paintImmediately(0, 0, getWidth(), getHeight());
		listener.onUpdatedArea(selectedScreenArea);
	}

	/**
	 * ドラッグ開始位置と現在位置から選択範囲を更新する。
	 *
	 * @param currentPoint 現在のマウス位置
	 */
	private void updateSelection(Point currentPoint) {
		// 撮影範囲の左上の(X,Y)座標を算出する
		int x = Math.min(startPoint.x, currentPoint.x);
		int y = Math.min(startPoint.y, currentPoint.y);

		// 選択範囲の高さと横幅を算出する
		int width = Math.abs(currentPoint.x - startPoint.x);
		int height = Math.abs(currentPoint.y - startPoint.y);

		// 選択範囲を更新する
		selectionArea.setBounds(x, y, width, height);

		// 選択範囲の枠線と、選択範囲外の暗幕を更新する
		repaint();
	}

	/**
	 * 選択範囲の上下左右へ暗幕を描画する。
	 *
	 * @param graphics2D 描画オブジェクト
	 */
	private void drawShadeAreas(Graphics2D graphics2D) {
		graphics2D.setColor(SHADE_COLOR);

		if (selectionArea == null) {
			// 未選択時は画面全体を暗幕で覆う
			graphics2D.fillRect(0, 0, getWidth(), getHeight());
			return;
		}

		int selectX = selectionArea.x;
		int selectY = selectionArea.y;
		int selectWidth = selectionArea.width;
		int selectHeight = selectionArea.height;

		// 上下左右の暗幕を設定する
		// 選択範囲の上側 選択範囲のYを高さとして使用する
		graphics2D.fillRect(0, 0, getWidth(), selectY);
		// 選択範囲の下側 高さの算出：パネル全体の高さ - (左上のY + 選択範囲の高さ)
		graphics2D.fillRect(
				0,
				selectY + selectHeight,
				getWidth(),
				Math.max(0, getHeight() - (selectY + selectHeight)));

		// 選択範囲の左側 選択範囲のXを幅として使用する
		graphics2D.fillRect(0, selectY, selectX, selectHeight);
		// 選択範囲の右側 幅の算出：パネル全体の幅 - (左上のX + 選択範囲の幅)
		graphics2D.fillRect(
				selectX + selectWidth,
				selectY,
				Math.max(0, getWidth() - (selectX + selectWidth)),
				selectHeight);
	}

	/**
	 * ドラッグ中の選択範囲へ白い枠線を描画する。
	 *
	 * @param graphics2D 描画オブジェクト
	 */
	private void drawSelectionBorder(Graphics2D graphics2D) {
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.setColor(SELECTION_BORDER_COLOR);
		graphics2D.setStroke(new BasicStroke(SELECTION_BORDER_WIDTH));

		// 線が選択範囲外へはみ出さないよう、右端と下端を1px内側に収める
		int width = Math.max(0, selectionArea.width - 1);
		int height = Math.max(0, selectionArea.height - 1);
		graphics2D.drawRect(selectionArea.x, selectionArea.y, width, height);
	}

	/**
	 * マウス座標をパネル内へ収める。
	 *
	 * @param point 補正前のマウス座標
	 * @return パネル内へ補正したマウス座標
	 */
	private Point clampPoint(Point point) {
		// 指定された座標を、パネルの最小値から最大値の範囲内に収める
		int x = Math.max(0, Math.min(point.x, getWidth()));
		int y = Math.max(0, Math.min(point.y, getHeight()));
		return new Point(x, y);
	}
}
