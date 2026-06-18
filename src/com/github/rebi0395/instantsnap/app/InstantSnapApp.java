package com.github.rebi0395.instantsnap.app;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.view.controller.MainViewController;
import com.github.rebi0395.instantsnap.view.presenter.ExitDialogPresenter;
import com.github.rebi0395.instantsnap.view.presenter.SnapNotificationPresenter;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * JavaFXの起動を行うアプリケーションクラス
 */
public class InstantSnapApp extends Application {

	/** 司令塔クラス */
	private InstantSnapCoordinator coordinator;

	/** 通知表示クラス */
	private SnapNotificationPresenter notificationPresenter;

	/** 終了ダイアログクラス */
	private ExitDialogPresenter exitDialogPresenter;

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(InstantSnapApp.class);

	/**
	 * コントローラにサービスクラスを注入し、初期起動を行う
	 */
	@Override
	public void start(Stage stage) {
		try {
			// 通知表示クラスを生成
			this.notificationPresenter = new SnapNotificationPresenter(stage);

			// 終了ダイアログ表示クラスを生成
			this.exitDialogPresenter = new ExitDialogPresenter();

			// 司令塔クラスを生成し、初期化処理を行う
			this.coordinator = new InstantSnapCoordinator(notificationPresenter);

			// コントローラを生成
			MainViewController mainController = new MainViewController();
			mainController.setCoordinator(coordinator);

			// FXMLファイルローダーを作成する
			FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_MAIN_VIEW));
			loader.setController(mainController); // FXMLローダーにコントローラをセットする
			Parent mainRoot;

			mainRoot = loader.load();

			// シーン (ウィンドウのファイル、横幅、高さを設定)
			Scene scene = new Scene(mainRoot, 600, 900);

			// ウィンドウを閉じようとすると、確認ウィンドウを表示する処理を登録
			stage.setOnCloseRequest(event -> {
				event.consume(); // ウィンドウの閉じる動作を止める
				try {
					boolean isEnd = exitDialogPresenter.show(stage);
					if (isEnd) {
						coordinator.exit();
					}
				} catch (ServiceException e) {
					coordinator.exit();
				}
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
				coordinator.exit();
			} else {
				System.exit(1);
			}
		}
	}
}
