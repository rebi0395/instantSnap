package com.github.rebi0395.instantsnap.app;

import static com.github.rebi0395.instantsnap.common.AppConstant.PROPERTIES_PATH;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.listener.SettingsChangeListener;
import com.github.rebi0395.instantsnap.service.SettingsFileWatcher;
import com.github.rebi0395.instantsnap.service.SettingsService;
import com.github.rebi0395.instantsnap.view.javafx.presenter.SettingWindowPresenter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * 子プロセス内でJavaFX設定画面と、その設定ファイル監視を管理します。
 * 設定画面を閉じるとこのプロセスの監視だけを停止し、常駐側の撮影監視は継続します。
 */
public class SettingWindowApp extends Application implements SettingsChangeListener {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SettingWindowApp.class);

	/** JavaFX設定画面の表示制御クラス */
	private SettingWindowPresenter settingWindowPresenter;

	/** JavaFXプロセス側で設定ファイルの変更を監視するクラス */
	private SettingsFileWatcher settingsFileWatcher;

	/**
	 * JavaFXアプリケーションスレッド上で設定画面を表示し、ファイル監視を開始します。
	 * 初期化に失敗した場合はログを残し、この設定プロセスを終了します。
	 *
	 * @param stage JavaFXランタイムから渡される設定画面のステージ
	 */
	@Override
	public void start(Stage stage) {
		try {
			// JavaFX側では設定の読込・保存に必要な機能だけを生成する。
			SettingsService settingsService = new SettingsService();

			// 設定画面のプレゼンタークラスを作成
			this.settingWindowPresenter = new SettingWindowPresenter(stage, settingsService);

			// 設定画面を表示する
			settingWindowPresenter.show();

			// 設定画面を開いた後の手動編集も画面へ反映するため、JavaFXプロセス側でも監視する。
			this.settingsFileWatcher = new SettingsFileWatcher(
					PROPERTIES_PATH,
					this);

		} catch (Exception e) {
			log.error("InstantSnapの起動に失敗しました", e);

			Platform.exit(); // JavaFX設定プロセスを終了させる
		}
	}

	/**
	 * 監視スレッドから設定変更を受け取り、JavaFXスレッドへ画面更新を予約します。
	 * 設定ファイルを直接編集した場合も、表示中の設定画面へ反映します。
	 */
	@Override
	public void onSettingsFileChanged() {
		Platform.runLater(() -> {
			if (settingWindowPresenter != null) {
				settingWindowPresenter.reloadSettings();
			}
		});
	}

	/**
	 * JavaFXランタイムの終了時に呼ばれ、このプロセスのファイル監視を停止します。
	 * 初期化途中での終了や、監視が停止済みの場合も呼び出せます。
	 */
	@Override
	public void stop() {
		if (settingsFileWatcher != null) {
			settingsFileWatcher.close();
			settingsFileWatcher = null;
		}
	}
}
