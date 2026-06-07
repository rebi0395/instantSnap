package com.github.rebi0395.instantsnap.app;

import java.awt.Toolkit;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.view.controller.ExitDialogController;
import com.github.rebi0395.instantsnap.view.controller.MainViewController;
import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * JavaFXの起動を行うアプリケーションクラス
 */
public class InstantSnapApp extends Application {

	/** 司令塔クラス */
	private InstantSnapCoordinator coordinator;
	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(InstantSnapApp.class);

	/**
	 * コントローラにサービスクラスを注入し、初期起動を行う
	 */
	@Override
	public void start(Stage stage) {
		try {
			// サービスクラスの司令塔クラスを生成し、初期化処理を行う
			this.coordinator = new InstantSnapCoordinator();

			// コントローラを生成
			MainViewController mainController = new MainViewController();
			mainController.setCoordinator(coordinator);

			// FXMLファイルローダーを作成する
			FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_MAIN_VIEW));
			loader.setController(mainController); // FXMLローダーにコントローラをセットする
			Parent mainRoot;

			mainRoot = loader.load();

			// シーン (ウィンドウのファイル、横幅、高さを設定)
			Scene scene = new Scene(mainRoot, 600, 800);

			// ウィンドウを閉じるとアプリを終了させるため、確認ウィンドウを表示する
			stage.setOnCloseRequest(event -> {
				event.consume(); // ウィンドウの閉じる動作を止める
				showExitDialog(stage); //確認ダイアログ表示
			});

			// アイコンを設定する
			InputStream iconStream = getClass().getResourceAsStream(ICON_APP);
			if (iconStream != null) {
				stage.getIcons().add(new Image(iconStream));
			} else {
				log.warn("アイコンファイルが見つかりません: {}", ICON_APP);
			}

			// ウィンドウを設定し、表示する
			stage.setTitle("InstantSnap Menu");
			stage.setScene(scene);
			stage.sizeToScene();
			stage.show();

		} catch (Exception e) {
			log.error("InstantSnapの起動に失敗しました", e);

			if (coordinator != null) {
				coordinator.exit(1);
			} else {
				System.exit(1);
			}
		}
	}

	/**
	 * 終了確認ダイアログを表示する
	 *
	 * @param owner 親ウィンドウ
	 */
	private void showExitDialog(Stage owner) {
		try {
			ExitDialogController exitController = new ExitDialogController();

			FXMLLoader exitLoader = new FXMLLoader(getClass().getResource(FXML_EXIT_DIALOG));
			exitLoader.setController(exitController);

			Parent exitRoot = exitLoader.load();

			Stage dialog = new Stage();
			dialog.initOwner(owner);
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setResizable(false);
			dialog.initStyle(StageStyle.UTILITY);
			dialog.setScene(new Scene(exitRoot));
			dialog.setTitle("確認");

			Toolkit.getDefaultToolkit().beep();

			dialog.showAndWait();

			if (exitController.getResult()) {
				coordinator.exit(0);
			}

		} catch (Exception e) {
			log.error("表示終了ダイアログの表示に失敗しました", e);
			coordinator.exit(1);
		}
	}
}
