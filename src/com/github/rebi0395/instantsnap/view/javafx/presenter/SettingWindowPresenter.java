package com.github.rebi0395.instantsnap.view.javafx.presenter;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.service.SettingsService;
import com.github.rebi0395.instantsnap.service.ScreenCaptureExclusionService;
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
 * JavaFX設定画面の生成と再読込時の表示反映を管理するクラスです。
 * 画面に関わるメソッドはJavaFXアプリケーションスレッド上で呼び出します。
 */
public class SettingWindowPresenter {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SettingWindowPresenter.class);

	/** 設定ウィンドウのステージ */
	private final Stage stage;

	/** 設定値の読込・保存を担当するクラス */
	private final SettingsService settingsService;

	/** JavaFX設定ウィンドウの撮影対象への表示・非表示を切り替えるサービス */
	private final ScreenCaptureExclusionService screenCaptureExclusionService;

	/** 設定画面の入力部品と表示内容を管理するコントローラ */
	private SettingWindowController settingController;

	/** ウィンドウの最大の横幅 */
	private static final double SCENE_MAX_WIDTH = 680;

	/** Windows APIから設定ウィンドウを特定するときにも使用するタイトル */
	private static final String WINDOW_TITLE = "InstantSnap Settings";

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
		this.screenCaptureExclusionService = new ScreenCaptureExclusionService();
	}

	/**
	 * FXMLから設定画面を生成し、現在の設定とテーマを適用して表示します。
	 *
	 * @throws IOException FXMLの読込または画面の構築に失敗した場合
	 */
	public void show() throws IOException {
		// コントローラを生成
		SettingWindowController createdController = new SettingWindowController(settingsService);

		// FXMLファイルローダーを作成する
		FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_SETTING_WINDOW));
		loader.setController(createdController); // FXMLローダーにコントローラをセットする
		Parent settingRoot = loader.load();
		this.settingController = createdController;

		// シーンの設定 (ウィンドウのファイル、横幅、高さを設定)
		Scene scene = createScene(settingRoot);
		StyleSheetUtil.applyStyleSheet(scene, CSS_SETTING_WINDOW); // css適用
		StyleSheetUtil.changeTheme(settingRoot, settingsService.getConfigBean().getThemeType()); // テーマcss適用

		// エンターキーの動作を登録
		EnterKeyNavigation.enableEnterAction(settingRoot);

		// アイコンを設定する
		setIcon();

		// ウィンドウを設定し、表示する
		stage.setTitle(WINDOW_TITLE);
		stage.setScene(scene);
		stage.show();
		applyScreenCaptureSetting();
	}

	/**
	 * 設定ファイルを再読込し、成功した場合に表示中の設定画面へ反映します。
	 * 画面生成前は何もせず、読込に失敗した場合も入力部品の更新を行いません。
	 */
	public void reloadSettings() {
		if (settingController == null) {
			return;
		}
		if (settingsService.reload()) {
			settingController.reloadSettings();
			applyScreenCaptureSetting();
		}
	}

	/**
	 * 最新の設定値に従って、JavaFX設定ウィンドウを撮影対象へ含めるか切り替えます。
	 * JavaFXのStageはAWTコンポーネントではないため、現在の子プロセスが所有する
	 * ウィンドウをタイトルから特定してWindows APIへ設定します。
	 */
	private void applyScreenCaptureSetting() {
		boolean excluded = !settingsService.getConfigBean().getIsIncludeSettingWindow();
		screenCaptureExclusionService.setCurrentProcessWindowExcluded(WINDOW_TITLE, excluded);
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
