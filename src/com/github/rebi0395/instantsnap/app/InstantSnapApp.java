package com.github.rebi0395.instantsnap.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.view.presenter.ExitDialogPresenter;
import com.github.rebi0395.instantsnap.view.presenter.MainWindowPresenter;
import com.github.rebi0395.instantsnap.view.presenter.SnapNotificationPresenter;

import javafx.application.Application;
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
	 * 各種PresenterとCoordinatorを生成し、メインウィンドウを表示する
	 */
	@Override
	public void start(Stage stage) {
		try {
			// 通知表示クラスを生成
			this.notificationPresenter = new SnapNotificationPresenter(stage);

			// 司令塔クラスを生成し、初期化処理を行う
			this.coordinator = new InstantSnapCoordinator(notificationPresenter);

			// 終了ダイアログ表示クラスを生成
			this.exitDialogPresenter = new ExitDialogPresenter(coordinator.getMessageKeyConverter());

			// メインウィンドウのプレゼンタークラスを作成
			MainWindowPresenter mainPresenter = new MainWindowPresenter(stage, coordinator, exitDialogPresenter);

			// メインウィンドウを表示する
			mainPresenter.show();

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
