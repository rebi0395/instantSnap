package com.github.rebi0395.instantsnap.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.service.SettingsService;
import com.github.rebi0395.instantsnap.view.javafx.presenter.SettingWindowPresenter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * JavaFX設定画面を起動するアプリケーションクラス
 */
public class SettingWindowApp extends Application {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SettingWindowApp.class);

	/**
	 * 設定サービスとPresenterを生成し、設定画面を表示する
	 */
	@Override
	public void start(Stage stage) {
		try {
			// JavaFX側では設定の読込・保存に必要な機能だけを生成する。
			SettingsService settingsService = new SettingsService();

			// 設定画面のプレゼンタークラスを作成
			SettingWindowPresenter settingPresenter = new SettingWindowPresenter(stage, settingsService);

			// 設定画面を表示する
			settingPresenter.show();

		} catch (Exception e) {
			log.error("InstantSnapの起動に失敗しました", e);

			Platform.exit(); // JavaFX設定プロセスを終了させる
		}
	}
}
