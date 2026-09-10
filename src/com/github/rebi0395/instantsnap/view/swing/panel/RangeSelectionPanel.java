package com.github.rebi0395.instantsnap.view.swing.panel;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.github.rebi0395.instantsnap.common.AppLogger;
import com.github.rebi0395.instantsnap.common.MessageKeyConstant;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.view.swing.dialog.RangeSelectionDialog.SelectionListener;

/**
 * 範囲選択画面を表示するパネルを作成するクラス
 */
public class RangeSelectionPanel extends JPanel {

	/** ユーザー設定値クラス */
	private final ConfigBean config;

	/** メッセージ変換クラス */
	private final MessageKeyConverter converter;

	/** マウスポインター移動用に使用するRobot */
	private Robot robot;

	/** 選択範囲外の暗幕色 */
	private static final Color SHADE_COLOR = new Color(0, 0, 0, 89);

	/** 完全透明によるマウスイベントのすり抜けを防止する背景色。*/
	private static final Color INTERACTION_COLOR = new Color(0, 0, 0, 1);

	/** 選択範囲の枠線 外側の色 */
	private static final Color SELECTION_BORDER_OUTER_COLOR = new Color(0, 0, 0, 180);

	/** 選択範囲の枠線 内側の色 */
	private static final Color SELECTION_BORDER_INNER_COLOR = new Color(0, 220, 255);

	/** 選択範囲の外側の枠線幅 */
	private static final float SELECTION_BORDER_OUTER_WIDTH = 3f;

	/** 選択範囲の内側の枠線幅 */
	private static final float SELECTION_BORDER_INNER_WIDTH = 1f;

	/** ガイド線外側の色 */
	private static final Color GUIDE_LINE_OUTER_COLOR = new Color(0, 0, 0, 180);

	/** ガイド線内側の色 */
	private static final Color GUIDE_LINE_INNER_COLOR = new Color(0, 220, 255);

	/** ドラッグ開始と判定する移動量 */
	private static final int DRAG_START_MOVING = 3;

	/** 範囲選択結果の通知先 */
	private final SelectionListener listener;

	/** パネルを表示しているモニターのAWT座標 */
	private final Rectangle screenArea;

	/** パネル内の選択開始位置 */
	private Point startPoint;

	/** パネル内の選択位置 */
	private Point currentPoint;

	/** 現在の選択範囲。パネル左上を原点とする */
	private Rectangle selectionArea;

	/** ドラッグで範囲選択をしている場合 true */
	private boolean dragging;

	/** 操作説明パネルに表示する文章 */
	private List<String> guideTexts = new ArrayList<String>();

	/** 範囲選択画面の状態 */
	private SelectionState state = SelectionState.WAITING;

	/**
	 * コンストラクタ
	 *
	 * @param screenArea パネルを表示するモニターのAWT座標
	 * @param config ユーザー設定値
	 * @param converter 多言語メッセージ変換インスタンス
	 * @param listener 範囲選択結果の通知先
	 */
	public RangeSelectionPanel(Rectangle screenArea, ConfigBean config,
			MessageKeyConverter converter, SelectionListener listener) {
		this.screenArea = new Rectangle(screenArea);
		this.config = config;
		this.converter = converter;
		this.listener = listener;

		// 初期状態と操作説明を設定する
		changeSelectionState(SelectionState.WAITING);

		// 背景を透過し、範囲選択用のカーソルに変更する
		setOpaque(false);
		setFocusable(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		registerMouseEvents(); // マウスイベントの登録

		try {
			robot = new Robot();
		} catch (AWTException e) {
			AppLogger.warn("{}：Robotインスタンスの生成に失敗しました", this.getClass().getName(), e);
		}
	}

	/**
	 * 選択状態を初期化する。
	 */
	public void resetSelection() {
		startPoint = null; // 選択開始位置
		selectionArea = null; // 選択範囲
		dragging = false;
		listener.updateRangeArea(null); // 通知先の範囲選択をリセットする
		changeSelectionState(SelectionState.WAITING); // 状態を更新
		repaint(); // 選択範囲と枠線を消し、全面を暗幕で覆う
	}

	/**
	 * パネル表示直後のマウス位置を初期値として設定する
	 */
	public void initializeCurrentPoint() {
		Point point = getMousePosition();

		if (point == null) {
			return;
		}
		currentPoint = clampPoint(point);
	}

	/**
	 * クリック、Enterなどで選択範囲状態に応じた処理を実施し、選択範囲状態を進める。
	 */
	public void advanceStateWithAction() {
		if (currentPoint == null) {
			// 座標がない場合、早期リターン
			return;
		}

		switch (state) {
		case WAITING:
			startSelection();
			break;

		case SELECTING:
			confirmSelection();
			break;

		case CONFIRMED:
			resetSelection();
			break;
		}
	}

	/**
	 * 矢印キーの入力で、マウス座標を移動させる
	 *
	 * @param movement 移動量
	 */
	public void moveMouse(Point movement) {
		if (currentPoint == null) {
			return;
		}
		if (robot == null) {
			try {
				robot = new Robot(); // 一回生成を試みる
			} catch (AWTException e) {
				AppLogger.warn("{}：Robotインスタンスの生成に失敗しました", this.getClass().getName(), e);
			}

			if (robot == null) {
				return; // robotがないとき、早期リターン
			}
		}
		int nextX = currentPoint.x + movement.x; // モニター座標からX座標を動かす
		int nextY = currentPoint.y + movement.y; // モニター座標からX座標を動かす
		Point nextPoint = clampPoint(new Point(nextX, nextY)); // マイナス座標にならない用に丸める

		// モニター全体を含めたスクリーン座標に変換する
		int screenX = screenArea.x + nextPoint.x;
		int screenY = screenArea.y + nextPoint.y;
		robot.mouseMove(screenX, screenY);
	}

	/**
	 * Ctrlキー + 矢印キーの入力で、選択範囲の枠を移動させる
	 *
	 * @param movement 移動量
	 */
	public void moveSelectionArea(Point movement) {
		if (state != SelectionState.SELECTING || selectionArea == null ||
				startPoint == null || currentPoint == null) {
			return; // 待機中の場合、早期リターン
		}
		if (robot == null) {
			try {
				robot = new Robot(); // 一回生成を試みる
			} catch (AWTException e) {
				AppLogger.warn("{}：Robotインスタンスの生成に失敗しました", this.getClass().getName(), e);
			}

			if (robot == null) {
				return; // robotがないとき、早期リターン
			}
		}
		// 移動量を補正する
		Point clampMovement = clampMoveDirection(movement);

		// 各点を移動させる
		startPoint.translate(clampMovement.x, clampMovement.y);
		currentPoint.translate(clampMovement.x, clampMovement.y);
		selectionArea.translate(clampMovement.x, clampMovement.y);

		// モニター全体を含めたスクリーン座標に変換する
		int screenX = screenArea.x + currentPoint.x;
		int screenY = screenArea.y + currentPoint.y;
		robot.mouseMove(screenX, screenY);
	}

	/**
	 * 範囲選択画面の状態を変更し、表示する操作説明を更新する
	 *
	 * @param newState 変更後の状態
	 */
	private void changeSelectionState(SelectionState newState) {
		state = newState;
		guideTexts = createGuideTexts();
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

			// 暗幕の描画
			drawShadeAreas(graphics2D);
			// 十字カーソルのガイド線を描画
			drawCrosshairGuide(graphics2D);
			// 操作説明を描画
			drawOperationGuide(graphics2D);
			// 選択範囲の枠線を描画
			drawSelectionBorder(graphics2D);

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
				currentPoint = clampPoint(event.getPoint()); // フィールドのマウス位置を更新
				if (currentPoint == null || !SwingUtilities.isLeftMouseButton(event) || dragging) {
					// 座標がとれないとき、左ボタンでないとき、ドラッグ選択した場合、早期リターン
					return;
				}
				advanceStateWithAction();
			}

			@Override
			public void mouseMoved(MouseEvent event) {
				currentPoint = clampPoint(event.getPoint()); // フィールドのマウス位置を更新
				if (currentPoint == null || state != SelectionState.SELECTING || dragging) {
					// マウス座標がとれないとき、選択中でないとき、ドラッグ中の場合
					repaint(); // ガイド線描画の更新
					return; // 早期リターン
				}
				updateSelection(); // 選択範囲を更新する
			}

			@Override
			public void mouseDragged(MouseEvent event) {
				currentPoint = clampPoint(event.getPoint()); // フィールドのマウス位置を更新
				// 押しっぱなしの状態はマスクで判定する
				boolean leftButtonDown = (event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;

				if (currentPoint == null || !leftButtonDown
						|| state != SelectionState.SELECTING || !isDragStarted()) {
					// マウス座標がとれないとき、左ボタンでないとき、選択中でないとき、移動量が極小の場合
					repaint(); // ガイド線描画の更新
					return; // 早期リターン
				}

				dragging = true; // ドラッグ中フラグをオンにする
				updateSelection(); // 選択範囲を更新する
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				currentPoint = clampPoint(event.getPoint()); // フィールドのマウス位置を更新
				if (currentPoint == null || !SwingUtilities.isLeftMouseButton(event) ||
						state != SelectionState.SELECTING || !dragging) {
					// マウス座標がとれないとき、マウスの左ボタンでないとき、
					// 選択中でないとき、ドラッグ中でない場合、早期リターン
					return;
				}
				dragging = false; // ドラッグ中フラグを解除
				advanceStateWithAction();
			}
		};

		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
	}

	/**
	 * 選択開始位置から一定距離以上マウスが移動したか判定する
	 * @return ドラッグ開始と判定する場合true
	 */
	private boolean isDragStarted() {
		if (startPoint == null || currentPoint == null) {
			return false;
		}

		int movedX = Math.abs(currentPoint.x - startPoint.x);
		int movedY = Math.abs(currentPoint.y - startPoint.y);

		return movedX >= DRAG_START_MOVING || movedY >= DRAG_START_MOVING;
	}

	/**
	 * ドラッグによる範囲選択を開始する。
	 */
	private void startSelection() {
		// 通知先の範囲選択をリセットする
		listener.updateRangeArea(null);
		// ドラッグ開始位置を設定する
		startPoint = clampPoint(currentPoint);
		// 押下位置から新しい選択範囲を開始する
		selectionArea = new Rectangle(startPoint.x, startPoint.y, 0, 0);
		// 状態ステートマシンの更新
		changeSelectionState(SelectionState.SELECTING);
		repaint(); // 再描画
	}

	/**
	 * 選択範囲を確定する。
	 */
	private void confirmSelection() {
		if (state != SelectionState.SELECTING) {
			// 選択中状態でない場合、早期リターン
			return;
		}

		// ドラッグフラグをoffにする。
		dragging = false;
		// 選択範囲を更新する
		updateSelection();

		if (selectionArea.width <= 0 || selectionArea.height <= 0) {
			// 範囲を選択していなければ、選択状態を解除し、状態を待機中に戻す
			resetSelection();
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
		listener.updateRangeArea(selectedScreenArea);

		if (config.getIsAutoSnapAfterRangeSelectionEnabled()) {
			if (!config.getIsCloseRangeSelectionAfterSnapEnabled()) {
				// 自動撮影後もモードを継続する場合は、次の範囲選択を受け付ける。
				prepareNextSelection();
			}
		} else {
			// 自動撮影が無効な場合は、従来どおり確定状態で待機する
			changeSelectionState(SelectionState.CONFIRMED);
		}
		repaint();
	}

	/**
	 * 撮影後に選択範囲を破棄し、次の範囲選択待ちに戻します。
	 * 撮影用の範囲は通知先でコピー済みのため、nullの再通知は行いません。
	 */
	public void prepareNextSelection() {
		startPoint = null;
		selectionArea = null;
		dragging = false;
		changeSelectionState(SelectionState.WAITING);
	}

	/**
	 * ドラッグ開始位置と現在位置から選択範囲を更新する
	 */
	private void updateSelection() {
		if (startPoint == null || currentPoint == null) {
			// 座標がない場合、早期リターン
			return;
		}

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
	 * 選択範囲の上下左右へ暗幕を描画する
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
	 * 十字カーソルのガイド線を描画する
	 * 範囲確定時、マウス座標がない場合、描画しない
	 *
	 * @param graphics2D 描画オブジェクト
	 */
	private void drawCrosshairGuide(Graphics2D graphics2D) {
		if (state == SelectionState.CONFIRMED || currentPoint == null) {
			return; // 範囲確定時、マウス座標がない場合、早期リターン
		}

		// アンチエイリアスをoff
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		// 外側の線を描画する
		graphics2D.setColor(GUIDE_LINE_OUTER_COLOR);
		graphics2D.setStroke(new BasicStroke(SELECTION_BORDER_OUTER_WIDTH)); // 線の太さを選択範囲の外線と合わせる
		drawCrosshairLines(graphics2D);

		// 内側の線を描画し、二重線にする
		graphics2D.setColor(GUIDE_LINE_INNER_COLOR);
		graphics2D.setStroke(new BasicStroke(SELECTION_BORDER_INNER_WIDTH)); // 線の太さを選択範囲の内線と合わせる
		drawCrosshairLines(graphics2D);
	}

	/**
	 * 現在の色と線幅で十字ガイド線を描画する
	 *
	 * @param graphics2D 描画オブジェクト
	 */
	private void drawCrosshairLines(Graphics2D graphics2D) {
		int maxX = Math.max(0, getWidth() - 1); // 座標は0始まりのため、-1する
		int maxY = Math.max(0, getHeight() - 1); // 座標は0始まりのため、-1する
		int spacing = 20; // 表示をしない範囲

		// 描画座標をパネル内へ収める
		int guideX = Math.min(maxX, currentPoint.x);
		int guideY = Math.min(maxY, currentPoint.y);

		// カーソルから離れた座標を取得する
		int leftLineEnd = Math.max(0, guideX - spacing); // 左側から引くラインの終点
		int rightLineStart = Math.min(maxX, guideX + spacing); // 右側に引くラインの始点
		int topLineEnd = Math.max(0, guideY - spacing); // 上側から引くラインの終点
		int bottomLineStart = Math.min(maxY, guideY + spacing); // 下側から引くラインの終点

		// 選択範囲の描画中
		if (state == SelectionState.SELECTING && selectionArea != null) {
			// 選択範囲から少し離れた位置に上書きする
			leftLineEnd = Math.max(0, selectionArea.x - spacing);
			rightLineStart = Math.min(maxX, selectionArea.x + selectionArea.width + spacing);
			topLineEnd = Math.max(0, selectionArea.y - spacing);
			bottomLineStart = Math.min(maxY, selectionArea.y + selectionArea.height + spacing);

			// 右・下方向へ選択している場合、撮影範囲内の最終ピクセルへ合わせる
			if (currentPoint.x > startPoint.x) {
				guideX--;
			}
			if (currentPoint.y > startPoint.y) {
				guideY--;
			}
		}

		// カーソル、または選択範囲の左右へ横線を描画する
		graphics2D.drawLine(0, guideY, leftLineEnd, guideY);
		graphics2D.drawLine(rightLineStart, guideY, maxX, guideY);
		// カーソル、または選択範囲の上下へ縦線を描画する
		graphics2D.drawLine(guideX, 0, guideX, topLineEnd);
		graphics2D.drawLine(guideX, bottomLineStart, guideX, maxY);

	}

	/**
	 * ドラッグ中の選択範囲へ枠線を描画する
	 * 待機中の場合、選択範囲がない場合、描画しない
	 * @param graphics2D 描画オブジェクト
	 */
	private void drawSelectionBorder(Graphics2D graphics2D) {
		if (state == SelectionState.WAITING || selectionArea == null) {
			return; // 待機中のとき、選択範囲がない場合、早期リターン
		}

		int x = selectionArea.x;
		int y = selectionArea.y;
		// 線が選択範囲外へはみ出さないよう、右端と下端を1px内側に収める
		int width = Math.max(0, selectionArea.width - 1);
		int height = Math.max(0, selectionArea.height - 1);

		// レンダリングのアンチエイジングをoff
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

		// 外側線を描画
		graphics2D.setColor(SELECTION_BORDER_OUTER_COLOR);
		graphics2D.setStroke(new BasicStroke(SELECTION_BORDER_OUTER_WIDTH));
		graphics2D.drawRect(x, y, width, height);

		// 内側線を描画
		graphics2D.setColor(SELECTION_BORDER_INNER_COLOR);
		graphics2D.setStroke(new BasicStroke(SELECTION_BORDER_INNER_WIDTH));
		graphics2D.drawRect(x, y, width, height);
	}

	/**
	 * 画面左上へ操作説明を描画する
	 * @param graphics2D 描画オブジェクト
	 */
	private void drawOperationGuide(Graphics2D graphics2D) {
		FontMetrics metrics = graphics2D.getFontMetrics(); // フォントの描画情報を取得する
		int lineHeight = metrics.getHeight(); // 文字フォントから行間を含む、一行の高さを取得する

		int panelX = 20; // パネルの左上X座標 
		int panelY = 20; // パネルの左上Y座標
		int paddingX = 15; // 文字横の余白
		int paddingY = 25; // 文字列先頭行の上余白、最終行の下余白

		int panelWidth = 360; // パネルの横幅
		int panelHeight = paddingY + (guideTexts.size() * lineHeight); // 行数に応じたパネルの高さ

		int textX = panelX + paddingX; // 文字列のX座標
		int textY = panelY + paddingY; // 文字列のY座標

		graphics2D.setColor(new Color(20, 20, 20, 190)); // 半透明の背景
		graphics2D.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10); // 枠の表示
		graphics2D.setColor(Color.WHITE); // 文字色の設定

		for (String text : guideTexts) {
			graphics2D.drawString(text, textX, textY);
			textY += lineHeight; // 文字列の表示するY座標を一行分、下にずらす
		}
	}

	/**
	 * 範囲選択状態から、操作説明パネルに表示する文章リストを生成する
	 * @return 操作説明パネルに表示する文章リスト
	 */
	private List<String> createGuideTexts() {
		List<String> texts = new ArrayList<String>();
		switch (state) {
		case WAITING: {
			// クリック説明
			texts.add(converter.getMessage(MessageKeyConstant.RANGE_SELECTION_GUIDE_WAITING));
			// マウス操作、矢印キーで範囲動かせる説明
			texts.add(converter.getMessage(MessageKeyConstant.RANGE_SELECTION_GUIDE_MOVE_POINTER));
			// Shiftで高速移動できる説明
			texts.add(converter.getMessage(MessageKeyConstant.RANGE_SELECTION_GUIDE_FAST_MOVE));
			break;
		}
		case SELECTING: {
			// クリック説明			
			texts.add(converter.getMessage(MessageKeyConstant.RANGE_SELECTION_GUIDE_SELECTING));
			// マウス操作、矢印キーで範囲動かせる説明
			texts.add(converter.getMessage(MessageKeyConstant.RANGE_SELECTION_GUIDE_MOVE_POINTER));
			// Ctrl + 矢印キーで枠全体を動かせる説明
			texts.add(converter.getMessage(MessageKeyConstant.RANGE_SELECTION_GUIDE_MOVE_AREA));
			// Shiftで高速移動できる説明
			texts.add(converter.getMessage(MessageKeyConstant.RANGE_SELECTION_GUIDE_FAST_MOVE));
			break;
		}
		case CONFIRMED: {
			// クリック説明
			texts.add(converter.getMessage(MessageKeyConstant.RANGE_SELECTION_GUIDE_CONFIRMED));
			// 撮影キーの説明
			texts.add(config.getSnapKeyBean().resolveKeyCombinationName() +
					converter.getMessage(MessageKeyConstant.RANGE_SELECTION_GUIDE_SNAP));
			break;
		}
		default:
			// 何もしない
		}
		// 閉じる説明は共通
		texts.add("ESC / " + config.getSelectionKeyBean().resolveKeyCombinationName() +
				converter.getMessage(MessageKeyConstant.RANGE_SELECTION_GUIDE_CLOSE));
		return texts;
	}

	/**
	 * マウス座標をパネル内へ収める。
	 *
	 * @param point 補正前のマウス座標
	 * @return パネル内へ補正したマウス座標
	 */
	private Point clampPoint(Point point) {
		// 指定された座標を、パネルの最小値から最大値の範囲内に収める
		int x = Math.max(0, Math.min(point.x, getWidth() - 1));
		int y = Math.max(0, Math.min(point.y, getHeight() - 1));
		return new Point(x, y);
	}

	/**
	 * 枠全体がパネル内に収まるよう、移動量を補正する。
	 *
	 * @param movement 移動量
	 * @return 枠の適切な移動量
	 */
	private Point clampMoveDirection(Point movement) {
		// 選択範囲の座標取得
		int left = Math.min(startPoint.x, currentPoint.x);
		int right = Math.max(startPoint.x, currentPoint.x);
		int top = Math.min(startPoint.y, currentPoint.y);
		int bottom = Math.max(startPoint.y, currentPoint.y);

		// パネルの最大座標
		int panelMaxX = Math.max(0, getWidth() - 1);
		int panelMaxY = Math.max(0, getHeight() - 1);

		int moveX = Math.max(-left, Math.min(movement.x, panelMaxX - right));
		int moveY = Math.max(-top, Math.min(movement.y, panelMaxY - bottom));

		return new Point(moveX, moveY);
	}

	/**
	 * 範囲選択画面の操作状態
	 */
	private enum SelectionState {

		/** 範囲選択の操作待ちの状態 */
		WAITING,

		/** 撮影範囲を変更中の状態 */
		SELECTING,

		/** 撮影範囲が確定している状態 */
		CONFIRMED;
	}
}
