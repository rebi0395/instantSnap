package com.github.rebi0395.instantsnap.view.presenter;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.app.InstantSnapCoordinator;
import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.view.StyleSheetUtil;
import com.github.rebi0395.instantsnap.view.controller.MainWindowController;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * メインウィンドウの表示制御を行うクラス
 */
public class MainWindowPresenter {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(MainWindowPresenter.class);

	/** メインウィンドウのステージ */
	private final Stage stage;

	/** 司令塔クラス */
	private final InstantSnapCoordinator coordinator;

	/** 終了通知を表示させるプレゼンター */
	private final ExitDialogPresenter exitDialogPresenter;

	/** ウィンドウの最大の横幅 */
	private static final double SCENE_MAX_WIDTH = 650;

	/** ウィンドウの最大の高さ */
	private static final double SCENE_MAX_HEIGHT = 900;
	 
	/** ウィンドウの横幅の余白 */
	private static final double SCENE_MARGIN_WIDTH = 65;

	/** ウィンドウの高さの余白 */
	private static final double SCENE_MARGIN_HEIGHT = 90;

	public MainWindowPresenter(Stage stage, InstantSnapCoordinator coordinator,
			ExitDialogPresenter exitDialogPresenter) {
		this.stage = stage;
		this.coordinator = coordinator;
		this.exitDialogPresenter = exitDialogPresenter;
	}

	/**
	 * メイン画面を表示する
	 * @throws IOException ルート読み込み失敗時
	 */
	public void show() throws IOException {
		// コントローラを生成
		MainWindowController mainController = new MainWindowController(coordinator);

		// FXMLファイルローダーを作成する
		FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_MAIN_WINDOW));
		loader.setController(mainController); // FXMLローダーにコントローラをセットする
		Parent mainRoot = loader.load();

		// シーンの設定 (ウィンドウのファイル、横幅、高さを設定)
		Scene scene = createScene(mainRoot);
		StyleSheetUtil.applyStyleSheet(scene, CSS_MAIN_WINDOW); // css適用
		StyleSheetUtil.applyTheme(mainRoot, CSS_MAIN_THEME_STORYBOOK, "theme-storybook"); // テーマcss適用

		// ウィンドウを閉じようとすると、確認ウィンドウを表示する処理を登録
		registerCloseRequest();

		// アイコンを設定する
		setIcon();

		// ウィンドウを設定し、表示する
		stage.setTitle("InstantSnap Menu");
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * ウィンドウを閉じる要求時に、確認ウィンドウを表示する処理を登録する
	 */
	private void registerCloseRequest() {
		// ウィンドウを閉じる要求がされた時
		stage.setOnCloseRequest(event -> {
			event.consume(); // 閉じる動作を止める
			try {
				boolean isEnd = exitDialogPresenter.show(stage);
				if (isEnd) {
					// 確認ウィンドウで終了するが選ばれた場合、アプリを終了する
					coordinator.exit();
				}
			} catch (ServiceException e) {
				log.warn("終了確認ダイアログの表示に失敗したため、アプリを終了します", e);
				coordinator.exit();
			}
		});
	}

	/**
	 * アプリのアイコンを設定する
	 */
	private void setIcon() {
		try (InputStream iconStream = getClass().getResourceAsStream(ICON_APP)) {
			if (iconStream != null) {
				stage.getIcons().add(new Image(iconStream));
			} else {
				log.warn("アイコンファイルが見つかりません: {}", ICON_APP);
			}
		} catch (IOException e) {
			log.warn("アイコンファイルの読み込みに失敗しました: {}", ICON_APP, e);
		}
	}

	/**
	 * メイン画面のSceneを作成する
	 * 表示可能領域に収まるサイズに調整する
	 * 
	 * @param root JavaFXの最上位ノード
	 * @return メイン画面のScene
	 */
	private Scene createScene(Parent root) {

		Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds(); // 画面サイズ取得

		// 使用している画面の幅、高さから余白を引いた値を取得する
		double width = visualBounds.getWidth() - SCENE_MARGIN_WIDTH;
		double height = visualBounds.getHeight() - SCENE_MARGIN_HEIGHT;

		// 最大値か、画面サイズから余白を引いた大きさのうち、小さいサイズを適用する
		double sceneWidth = Math.min(SCENE_MAX_WIDTH, width);
		double sceneHeight = Math.min(SCENE_MAX_HEIGHT, height);
		return new Scene(root, sceneWidth, sceneHeight);
	}
}
