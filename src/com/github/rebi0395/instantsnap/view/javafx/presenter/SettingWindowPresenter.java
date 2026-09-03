package com.github.rebi0395.instantsnap.view.javafx.presenter;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.service.SettingsService;
import com.github.rebi0395.instantsnap.view.javafx.controller.SettingWindowController;
import com.github.rebi0395.instantsnap.view.javafx.util.EnterKeyNavigation;
import com.github.rebi0395.instantsnap.view.javafx.util.StyleSheetUtil;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * JavaFX設定画面の表示制御を行うクラス
 */
public class SettingWindowPresenter {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SettingWindowPresenter.class);

	/** 設定ウィンドウのステージ */
	private final Stage stage;

	/** 設定値の読込・保存を担当するクラス */
	private final SettingsService settingsService;

	/** ウィンドウの最大の横幅 */
	private static final double SCENE_MAX_WIDTH = 680;

	/** ウィンドウの最大の高さ */
	private static final double SCENE_MAX_HEIGHT = 900;

	/** ウィンドウの横幅の余白 */
	private static final double SCENE_MARGIN_WIDTH = 68;

	/** ウィンドウの高さの余白 */
	private static final double SCENE_MARGIN_HEIGHT = 90;

	/**
	 * JavaFX設定画面の表示制御クラスを生成します。
	 *
	 * @param stage 表示対象のステージ
	 * @param settingsService 設定値の提供元
	 */
	public SettingWindowPresenter(Stage stage, SettingsService settingsService) {
		this.stage = stage;
		this.settingsService = settingsService;
	}

	/**
	 * 設定画面を表示する
	 * @throws IOException ルート読み込み失敗時
	 */
	public void show() throws IOException {
		// コントローラを生成
		SettingWindowController settingController = new SettingWindowController(settingsService);

		// FXMLファイルローダーを作成する
		FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_SETTING_WINDOW));
		loader.setController(settingController); // FXMLローダーにコントローラをセットする
		Parent settingRoot = loader.load();

		// シーンの設定 (ウィンドウのファイル、横幅、高さを設定)
		Scene scene = createScene(settingRoot);
		StyleSheetUtil.applyStyleSheet(scene, CSS_SETTING_WINDOW); // css適用
		StyleSheetUtil.changeTheme(settingRoot, settingsService.getConfigBean().getThemeType()); // テーマcss適用

		// エンターキーの動作を登録
		EnterKeyNavigation.enableEnterAction(settingRoot);

		// アイコンを設定する
		setIcon();

		// ウィンドウを設定し、表示する
		stage.setTitle("InstantSnap Settings");
		stage.setScene(scene);
		stage.show();
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
	 * 設定画面のSceneを作成する
	 * 表示可能領域に収まるサイズに調整する
	 * 
	 * @param root JavaFXの最上位ノード
	 * @return 設定画面のScene
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
