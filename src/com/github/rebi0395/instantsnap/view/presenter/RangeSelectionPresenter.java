package com.github.rebi0395.instantsnap.view.presenter;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.awt.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.view.StyleSheetUtil;
import com.github.rebi0395.instantsnap.view.controller.RangeSelectionController;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * 範囲選択画面の表示の下準備をするプレゼンタークラス
 */
public class RangeSelectionPresenter {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(RangeSelectionPresenter.class);

	/** 範囲選択コントローラ */
	private RangeSelectionController controller;

	/** 範囲決定通知リスナー */
	private SelectionListener listener;

	/** 表示しているStage */
	private Stage stage;

	/** コンストラクタ */
	public RangeSelectionPresenter(SelectionListener listener) {
		this.listener = listener;
		this.controller = new RangeSelectionController(listener);
	}

	/**
	 * 撮影選択完了時の通知インターフェース
	 */
	public interface SelectionListener {

		/**
		 * 範囲選択画面表示時に呼ばれるリスナー
		 */
		void onShown();

		/**
		 * 範囲選択解除後に呼ばれるリスナー
		 */
		void onClosed();

		/**
		 * 撮影範囲更新後に呼び出されるリスナー
		 *
		 * @param selectArea 更新後の撮影範囲
		 */
		void onUpdatedArea(Rectangle selectArea);
	}

	/**
	 * 範囲選択モードの有効・無効を切り替える。
	 * 画面が表示中なら閉じ、表示されていなければ開く。
	 *
	 * @param screenArea 範囲選択画面を表示するモニター領域
	 */
	public void switchRangeSelectionMode(Rectangle screenArea) {
		Platform.runLater(() -> {
			if (isShowing()) {
				closeRangeSelectionWindow();
				return;
			}

			showRangeSelectionWindow(screenArea);
		});
	}

	/** 
	 * 範囲選択画面のウィンドウを取得する。
	 * @return 範囲選択画面のウィンドウ
	 */
	public Window getWindow() {
		return stage;
	}

	/**
	 * 撮影範囲選択を受け付ける画面を表示する
	 *
	 * @param screenArea 表示する画面の座標
	 */
	private void showRangeSelectionWindow(Rectangle screenArea) {
		try {

			// FXMLファイルローダーを作成する
			FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_RANGE_SELECTION));
			loader.setController(controller); // FXMLローダーにコントローラをセットする
			Pane root = loader.load(); // パネルをrootとする

			// シーンの設定 (ウィンドウのファイル、横幅、高さを設定)
			Scene scene = new Scene(root);
			StyleSheetUtil.applyStyleSheet(scene, CSS_RANGE_SELECTION); // css適用
			scene.setFill(Color.TRANSPARENT);

			// ESCキーで画面を閉じる処理の登録
			scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
				if (event.getCode() == KeyCode.ESCAPE) {
					closeRangeSelectionWindow();
					event.consume();
				}
			});

			// ステージの設定
			stage = new Stage(StageStyle.TRANSPARENT);
			stage.setScene(scene);
			stage.setX(screenArea.x);
			stage.setY(screenArea.y);
			stage.setWidth(screenArea.width);
			stage.setHeight(screenArea.height);
			stage.setResizable(false);

			stage.show();
			listener.onShown(); // 画面表示を通知

		} catch (Exception e) {
			log.error("範囲選択画面の表示に失敗しました", e);
		}
	}

	/**
	 * 撮影範囲選択を受け付ける画面を閉じる
	 */
	private void closeRangeSelectionWindow() {
		if (!isShowing()) {
			return;
		}
		controller.resetSelection();
		stage.close();
		listener.onClosed(); // リスナー側の設定値を破棄させる
	}

	/**
	 * 画面を表示しているかを取得する。
	 * @return 画面を表示中ならtrue
	 */
	private boolean isShowing() {
		return stage != null && stage.isShowing();
	}
}
