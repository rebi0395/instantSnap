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

/**
 * 範囲選択画面の表示の下準備をするプレゼンタークラス
 */
public class RangeSelectionPresenter {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(RangeSelectionPresenter.class);

	/** 範囲決定通知リスナー */
	private SelectionListener listener;

	/** 表示しているStage */
	private Stage stage;

	/** コンストラクタ */
	public RangeSelectionPresenter(SelectionListener listener) {
		this.listener = listener;
	}

	/**
	 * 撮影選択完了時の通知インターフェース
	 */
	public interface SelectionListener {

		/**
		 * 撮影範囲選択後に呼び出されるリスナー
		 * 
		 * @param selectRectangle
		 */
		void onSelected(Rectangle selectArea);

		/**
		 * 範囲選択解除後に呼ばれるリスナー
		 */
		void onClosed();

	}

	/**
	 * 撮影範囲選択を受け付ける画面を表示する
	 * 
	 * @param rectangle 表示するモニターの座標
	 */
	public void show(Rectangle screenArea) {
		// JavaFXの専用スレッドに処理を依頼する
		Platform.runLater(() -> {
			showRangeSelection(screenArea);
		});

	}
	
	public boolean isShowing() {
		return stage != null && stage.isShowing();
	}

	/**
	 * 撮影範囲選択を受け付ける画面を表示する
	 */
	private void showRangeSelection(Rectangle screenArea) {
		try {
			RangeSelectionController controller = new RangeSelectionController(listener);

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
					controller.resetSelection();
					listener.onClosed();
					stage.close();
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

		} catch (Exception e) {
			log.error("範囲選択画面の表示に失敗しました", e);
		}
	}
}
