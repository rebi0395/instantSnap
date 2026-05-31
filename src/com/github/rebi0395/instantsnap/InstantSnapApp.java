package com.github.rebi0395.instantsnap;

import com.github.rebi0395.instantsnap.controller.ViewController;
import com.github.rebi0395.instantsnap.service.ServiceFacade;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFXの起動を行うアプリケーションクラス
 */
public class InstantSnapApp extends Application {
	
	private ServiceFacade facade;
	
	/**
	 * コントローラにサービスクラスを注入し、初期起動を行う
	 */
	@Override
	public void start(Stage stage) throws Exception {
		// コントローラを生成
		ViewController controller = new ViewController();
		// サービスクラスの司令塔クラスを起動し、プロパティファイルの読み取りなどの初期化処理を行う
		this.facade = new ServiceFacade();
		facade.init();
		controller.setFacade(facade); // コントローラにセットする
		// FXMLファイルローダーを作成し、FXMLファイルをセットする
		FXMLLoader loader = new FXMLLoader(
				getClass().getResource("/com/github/rebi0395/instantsnap/view/MainView.fxml"));
		// コントローラをFXMLローダーにセットする
		loader.setController(controller);
		// FXMLファイルを読み取る
		Parent root = loader.load();
		// シーン(ウィンドウの設定)
		Scene scene = new Scene(root, 600, 800);
		// ウィンドウを設定する
		stage.setTitle("InstantSnap Menu");
		stage.setScene(scene);
		stage.sizeToScene();
		// ウィンドウを表示する
		stage.show();
	}
}
