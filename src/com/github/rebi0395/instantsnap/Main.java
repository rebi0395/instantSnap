package com.github.rebi0395.instantsnap;

import com.github.rebi0395.instantsnap.common.DpiUtil;
import com.github.rebi0395.instantsnap.controller.MainController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 起動クラス
 */
public class Main extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(
				getClass().getResource("/com/github/rebi0395/instantsnap/view/MainView.fxml"));

		Parent root = loader.load();
		Scene scene = new Scene(root, 600, 750);

		scene.getStylesheets().add(
				getClass().getResource("/com/github/rebi0395/instantsnap/view/main.css").toExternalForm());

		stage.setTitle("InstantSnap Menu");
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}

	/**
	 * 起動メソッド
	 * @param args
	 */
	public static void main(String[] args) {
		DpiUtil.enablePerMonitorV2();
		MainController controller = new MainController();
		controller.init();
		launch(args);
	}
}
