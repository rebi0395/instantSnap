package com.github.rebi0395.instantsnap.view.controller;

import com.github.rebi0395.instantsnap.view.ScreenRectangleConverter;
import com.github.rebi0395.instantsnap.view.presenter.RangeSelectionPresenter.SelectionListener;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class RangeSelectionController {

	/** 範囲選択結果の通知先 */
	private final SelectionListener listener;

	/** 選択範囲外の暗幕色 */
	private static final Color SHADE_COLOR = Color.rgb(0, 0, 0, 0.35);

	/** 選択範囲の枠線色 */
	private static final Color SELECTION_BORDER_COLOR = Color.WHITE;

	/** 選択範囲の枠線幅 */
	private static final double SELECTION_BORDER_WIDTH = 2.0;

	/** ドラッグ開始位置のX座標 */
	private double startX;

	/** ドラッグ開始位置のY座標 */
	private double startY;

	/** 現在の選択範囲(パネルのローカル座標) */
	private Rectangle2D selectionBounds;

	/** ドラッグ中かどうか */
	private boolean dragging = false;

	/** 選択範囲が確定しているか */
	private boolean selectionConfirmed = false;

	@FXML
	private Pane selectionPane;

	@FXML
	private Rectangle topShadeArea;

	@FXML
	private Rectangle bottomShadeArea;

	@FXML
	private Rectangle leftShadeArea;

	@FXML
	private Rectangle rightShadeArea;

	@FXML
	private Rectangle selectionBorder;

	/**
	 * コンストラクタ
	 *
	 * @param listener イベントリスナー
	 */
	public RangeSelectionController(SelectionListener listener) {
		this.listener = listener;
	}

	/**
	 * 初期化処理
	 */
	@FXML
	public void initialize() {
		setupShadeAreas(); // 暗幕の初期化
		setupSelectionBorder(); // 枠線の初期化
		registerMouseEvents(); // マウスイベントの登録
		registerPaneResizeEvents(); // 暗幕の自動更新処理

		// 範囲選択用のカーソルに変更する
		selectionPane.setCursor(Cursor.CROSSHAIR);

		// Pane自体が透明でも、Paneの範囲全体でマウスイベントを受け取れるようにする。
		selectionPane.setPickOnBounds(true);
	}

	/**
	 * 選択状態を初期化する
	 */
	public void resetSelection() {
		dragging = false; // ドラッグ中か
		selectionConfirmed = false; // 選択範囲が確定しているか
		selectionBounds = null; // 選択範囲

		// 枠線の座標をリセットし、非表示にする。
		selectionBorder.setVisible(false);
		selectionBorder.setWidth(0);
		selectionBorder.setHeight(0);

		updateShadeAreas(); // 暗幕の更新
	}

	/**
	 * 範囲が決定しているかを取得する
	 * @return
	 */
	public boolean isSelectionConfirmed() {
		return selectionConfirmed;
	}

	/**
	 * 撮影範囲外の上下左右の暗幕を初期化する
	 */
	private void setupShadeAreas() {
		Rectangle[] shadeAreas = { topShadeArea, bottomShadeArea, leftShadeArea, rightShadeArea };

		for (Rectangle shadeArea : shadeAreas) {
			shadeArea.setFill(SHADE_COLOR); // 背景色設定
			shadeArea.setMouseTransparent(true); // 暗幕をマウスイベントの対象から除外
		}
	}

	/**
	 * 選択範囲の枠線を初期化する。
	 */
	private void setupSelectionBorder() {

		selectionBorder.setFill(Color.TRANSPARENT); // 選択範囲の内側は透明にし、元の画面を見えるようにする
		selectionBorder.setStroke(SELECTION_BORDER_COLOR); // 図形の輪郭線の設定
		selectionBorder.setStrokeWidth(SELECTION_BORDER_WIDTH); // 枠線の太さを設定
		selectionBorder.setMouseTransparent(true); // 枠自体のマウスイベントを無効
		selectionBorder.setVisible(false); // ドラッグをしていない段階では枠線は不可視
	}

	/**
	 * 範囲選択用のマウスイベントを登録する
	 */
	private void registerMouseEvents() {

		// マウスボタン押下時
		selectionPane.setOnMousePressed(mouseEvent -> {
			startSelection(mouseEvent);
		});

		// マウスをドラッグしたとき
		selectionPane.setOnMouseDragged(mouseEvent -> {
			dragSelection(mouseEvent);
		});

		// マウスボタンを離したとき
		selectionPane.setOnMouseReleased(mouseEvent -> {
			confirmSelection(mouseEvent);
		});
	}

	/**
	 * パネルのサイズが変更された場合の、暗幕の自動更新処理を登録する
	 */
	private void registerPaneResizeEvents() {
		// Paneのサイズ変更時、暗幕も更新する
		selectionPane.widthProperty().addListener((observable, oldValue, newValue) -> {
			updateShadeAreas();
		});

		// Paneのサイズ変更時、暗幕も更新する
		selectionPane.heightProperty().addListener((observable, oldValue, newValue) -> {
			updateShadeAreas();
		});
	}

	/**
	 * ドラッグによる範囲選択を開始する
	 * 
	 * @param mouseEvent マウスイベント
	 */
	private void startSelection(MouseEvent mouseEvent) {
		if (mouseEvent.getButton() != MouseButton.PRIMARY) {
			// マウスイベントのメインボタンでないとき、早期リターン
			return;
		}

		// 範囲選択済みの場合、選択済状態を解除する
		if (selectionConfirmed) {
			selectionConfirmed = false;
		}

		// ドラッグ開始位置を設定する
		startX = clamp(mouseEvent.getX(), 0, selectionPane.getWidth());
		startY = clamp(mouseEvent.getY(), 0, selectionPane.getHeight());

		dragging = true; // ドラッグ中フラグを設定
		selectionBorder.setVisible(true);

		// 押下位置から新しい選択範囲を開始する
		updateSelection(startX, startY);
	}

	/**
	 * ドラッグ中の選択範囲を更新する
	 * 
	 * @param mouseEvent マウスイベント
	 */
	private void dragSelection(MouseEvent mouseEvent) {
		if (!dragging || !mouseEvent.isPrimaryButtonDown()) {
			// ドラッグ中フラグがfalse、マウスボタンが押されていない場合は早期リターン
			return;
		}
		// ドラッグした最新位置を変数で設定する
		double currentX = clamp(mouseEvent.getX(), 0, selectionPane.getWidth());
		double currentY = clamp(mouseEvent.getY(), 0, selectionPane.getHeight());
		// 選択範囲を更新する
		updateSelection(currentX, currentY);
	}

	/**
	 * マウスを離した位置で選択範囲を確定する
	 * 
	 * @param mouseEvent マウスイベント
	 */
	private void confirmSelection(MouseEvent mouseEvent) {
		if (!dragging || mouseEvent.getButton() != MouseButton.PRIMARY) {
			// ドラッグ中フラグがfalse、マウスボタンでない場合は早期リターン
			return;
		}

		// 実際にドラッグをしていない場合は、通常クリックと見なす
		if (mouseEvent.isStillSincePress()) {
			resetSelection();
			listener.onSelected(null); // 通知先に、選択範囲が空であることを通知する
			return;
		}

		// ドラッグした最新位置を変数で設定する
		double currentX = clamp(mouseEvent.getX(), 0, selectionPane.getWidth());
		double currentY = clamp(mouseEvent.getY(), 0, selectionPane.getHeight());
		// 選択範囲を更新する
		updateSelection(currentX, currentY);

		// フラグを更新する
		dragging = false;
		selectionConfirmed = true;

		// 白線の非表示
		selectionBorder.setVisible(false);

		// 撮影範囲の更新を通知する
		Rectangle2D screenArea = convertScreenArea(selectionBounds);
		listener.onSelected(ScreenRectangleConverter.toAwt(screenArea));
	}

	/**
	 * 選択範囲を更新する
	 * 
	 * @param currentX 現在のX座標
	 * @param currentY 現在のY座標
	 */
	private void updateSelection(double currentX, double currentY) {

		// 撮影範囲の左上の(X,Y)座標を算出する
		double x = Math.min(startX, currentX);
		double y = Math.min(startY, currentY);

		// 選択範囲の高さと横幅を算出する
		double w = Math.abs(currentX - startX); // 絶対値を取得する
		double h = Math.abs(currentY - startY);

		// 選択範囲を更新する
		selectionBounds = new Rectangle2D(x, y, w, h);

		// 選択範囲の枠線を更新する
		selectionBorder.setX(x);
		selectionBorder.setY(y);
		selectionBorder.setWidth(w);
		selectionBorder.setHeight(h);
		selectionBorder.setVisible(true);

		// 選択範囲外の暗幕を更新する
		updateShadeAreas();
	}

	/**
	 * 選択範囲の上下左右に位置する四つの暗幕を更新する
	 */
	private void updateShadeAreas() {

		double paneW = selectionPane.getWidth();
		double paneH = selectionPane.getHeight();

		if (selectionBounds == null) {
			// 上部分の領域のみを全面に表示する
			setShadeAreaBounds(topShadeArea, 0, 0, paneW, paneH);

			// 下、左、右の領域はないものとして初期化
			setShadeAreaBounds(bottomShadeArea, 0, 0, 0, 0);
			setShadeAreaBounds(leftShadeArea, 0, 0, 0, 0);
			setShadeAreaBounds(rightShadeArea, 0, 0, 0, 0);

			return;
		}

		// 選択範囲の左上の（X,Y）と、幅、高さを取得する
		double selectX = selectionBounds.getMinX();
		double selectY = selectionBounds.getMinY();
		double selectW = selectionBounds.getWidth();
		double selectH = selectionBounds.getHeight();

		// 上下左右の暗幕を設定する
		// 選択範囲の上側 選択範囲のYを高さとして使用する
		setShadeAreaBounds(topShadeArea, 0, 0, paneW, selectY);
		// 選択範囲の下側 高さの算出：パネル全体の高さ - (左上のY + 選択範囲の高さ)
		setShadeAreaBounds(bottomShadeArea, 0, selectY + selectH,
				paneW, Math.max(0, paneH - (selectY + selectH)));
		// 選択範囲の左側 選択範囲のXを幅として使用する
		setShadeAreaBounds(leftShadeArea, 0, selectY, selectX, selectH); // 選択範囲の左側
		// 選択範囲の右側 幅の算出：パネル全体の幅 - (左側のX + 選択範囲の幅)
		setShadeAreaBounds(rightShadeArea, selectX + selectW, selectY,
				Math.max(0, paneW - (selectX + selectW)), selectH);
	}

	/**
	 * 暗幕の表示領域を設定する
	 * 
	 * @param shadeArea 暗幕用のRectangle
	 * @param x 暗幕左上のX座標
	 * @param y 暗幕左上のY座標
	 * @param width 暗幕の幅
	 * @param height 暗幕の高さ
	 */
	private void setShadeAreaBounds(Rectangle shadeArea, double x, double y, double width, double height) {
		shadeArea.setX(x);
		shadeArea.setY(y);
		shadeArea.setWidth(width);
		shadeArea.setHeight(height);
	}

	/**
	 * Pane内の座標をスクリーン座標へ変換する
	 *
	 * @param localArea Pane内の選択範囲
	 * @return スクリーン上の選択範囲
	 */
	private Rectangle2D convertScreenArea(Rectangle2D localArea) {
		// stageの座標にPaneの座標を加算して、左上のスクリーン座標(X,Y)を取得する
		Point2D topLeft = selectionPane.localToScreen(
				localArea.getMinX(), // Paneの左上のX
				localArea.getMinY()); // Paneの左上のY

		// stageの座標にPaneの座標を加算して、右下のスクリーン座標(X,Y)を取得する
		Point2D bottomRight = selectionPane.localToScreen(
				localArea.getMaxX(), // Paneの右下のX
				localArea.getMaxY()); // Paneの右下のY

		if (topLeft == null || bottomRight == null) {
			return null;
		}

		return new Rectangle2D(
				topLeft.getX(),
				topLeft.getY(),
				bottomRight.getX() - topLeft.getX(),
				bottomRight.getY() - topLeft.getY());
	}

	/**
	 * 指定された値を、最小値から最大値の範囲内に収める
	 * @param value 補正する値
	 * @param min 最小値
	 * @param max 最大値
	 * @return 補正後の値
	 */
	private double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		}

		if (value > max) {
			return max;
		}

		return value;
	}
}