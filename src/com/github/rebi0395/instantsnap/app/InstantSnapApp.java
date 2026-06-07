package com.github.rebi0395.instantsnap.app;

import java.awt.Toolkit;

import com.github.rebi0395.instantsnap.view.controller.ExitDialogController;
import com.github.rebi0395.instantsnap.view.controller.MainViewController;
import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * JavaFXの起動を行うアプリケーションクラス
 */
public class InstantSnapApp extends Application {

	/** 司令塔クラス */
	private InstantSnapCoordinator coordinator;

	/**
	 * コントローラにサービスクラスを注入し、初期起動を行う
	 */
	@Override
	public void start(Stage stage) throws Exception {
		// サービスクラスの司令塔クラスを生成し、初期化処理を行う
		this.coordinator = new InstantSnapCoordinator();
		
		// コントローラを生成
		MainViewController mainController = new MainViewController();
		mainController.setFacade(coordinator);
		
		// FXMLファイルローダーを作成する
		FXMLLoader loader = new FXMLLoader(
				getClass().getResource(FXML_MAIN_VIEW));
		loader.setController(mainController); // FXMLローダーにコントローラをセットする
		Parent mainRoot = loader.load(); // FXMLファイルを読み取る
		
		// シーン(ウィンドウの設定)
		Scene scene = new Scene(mainRoot, 600, 800);

		// ウィンドウを閉じたらアプリを完全終了させる
		stage.setOnCloseRequest(event -> {
			event.consume();
			try {
				// コントローラーの生成
				ExitDialogController exitController = new ExitDialogController();
				// FXMLファイルローダーを作成する
				FXMLLoader exitLoader = new FXMLLoader(
						getClass().getResource(FXML_EXIT_DIALOG));
				exitLoader.setController(exitController); // FXMLローダーにコントローラをセットする
				Parent exitRoot = exitLoader.load(); // FXMLファイルを読み取る
				
				// ダイアログをメイン画面の子モーダルとして設定する
				Stage dialog = new Stage();
				dialog.initOwner(stage); // メインウィンドウを親に設定
				dialog.initModality(Modality.APPLICATION_MODAL); // モーダルとする
				dialog.setResizable(false);
				dialog.initStyle(StageStyle.UTILITY);
				
				// ダイアログの表示処理
				dialog.setScene(new Scene(exitRoot));
				dialog.setTitle("確認");
				Toolkit.getDefaultToolkit().beep(); // アラート音
				
				dialog.showAndWait();

				if (exitController.getResult()) {
					Platform.exit();
					System.exit(0);
				}

			} catch (Exception e) {
				Platform.exit();
				System.exit(0);
			}
		});

		// ウィンドウを設定する
		stage.setTitle("InstantSnap Menu");
		stage.setScene(scene);
		stage.sizeToScene();
		// ウィンドウを表示する
		stage.show();
	}
}
