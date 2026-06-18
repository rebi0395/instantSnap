package com.github.rebi0395.instantsnap.view.presenter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.SnapResultBean;
import com.github.rebi0395.instantsnap.service.SnapAfterService;
import com.github.rebi0395.instantsnap.view.controller.SnapNotificationController;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.shape.MoveTo;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 撮影後、通知を表示するクラス
 */
public class SnapNotificationPresenter {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SnapNotificationPresenter.class);

	/** メインウィンドウのステージ */
	private Stage stage;

	/** 通知表示秒数 */
	public static final double NOTIFICATION_DISPLAY_SECONDS = 5.0;

	/** 通知表示位置の画面端からの余白 */
	public static final double NOTIFICATION_MARGIN = 20.0;

	/** 通知スライド移動量 */
	public static final double NOTIFICATION_SLIDE_DISTANCE = 40.0;

	/** 通知スライド表示時間 */
	public static final double NOTIFICATION_SLIDE_MILLIS = 250.0;

	/** 通知フェード開始時の透明度 */
	public static final double NOTIFICATION_FADE_START_OPACITY = 0.0;

	/** 通知フェード終了時の透明度 */
	public static final double NOTIFICATION_FADE_END_OPACITY = 1.0;

	/** コンストラクタ */
	public SnapNotificationPresenter(Stage stage) {
		this.stage = stage;
	}

	/**
	 * 通知を表示する
	 * @param result 撮影結果インスタンス
	 */
	public void show(SnapResultBean result, SnapAfterService afterService) {

		// メッセージリスト
		List<String> noticeMessageList = new ArrayList<String>();

		if (!result.hasResult()) {
			// 通知の表示が不要な場合、早期リターン
			return;
		}

		if (result.getIsImageSaved()) {
			// 画像保存に成功
			noticeMessageList.add("スクリーンショットを保存しました");

		} else if (result.getIsTmpSaved()) {
			// 一時保存に成功
			noticeMessageList.add("一時保存ファイルにスクリーンショットを保存しました");
		}

		if (result.getIsClipboardCopied()) {
			// クリップボードコピーに成功
			noticeMessageList.add("クリップボードに画像を保存しました");
		}

		// ラムダ式を使うため、final変数に代入
		final List<String> messages = List.copyOf(noticeMessageList);

		// JavaFXの専用スレッドに処理を依頼する
		Platform.runLater(() -> {
			try {
				showNotification(messages, result.getTargetPath(), 
						result.isOpenActionAvailable(), afterService);
			} catch (ServiceException e) {
				log.warn("通知表示に失敗しました", e);
			}
		});

	}

	/**
	 * 通知画面の作成を行い、通知を表示する
	 * 
	 * @param noticeMessageKeyList 通知に表示するメッセージキーのリスト
	 * @param targetPath 直前の画像保存パス
	 * @param isButtonRequired 通知に保存先パスに遷移するボタンを表示するか trueで表示
	 * @throws ServiceException
	 */
	private void showNotification(List<String> noticeMessageKeyList,
			Path targetPath, boolean isButtonRequired, SnapAfterService afterService) throws ServiceException {

		try {
			// FXMLローダーを作成
			FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_NOTIFICATION));
			Parent root = loader.load();

			// fxmlファイルに紐づいたコントローラを取得 (fxml側でコントローラ指定が前提)
			SnapNotificationController controller = loader.getController();

			// Popupを設定
			Popup popup = new Popup();
			popup.setAutoFix(true); // 画面外に出にくくする
			popup.setHideOnEscape(true); // Esc押下で閉じる

			// 通知押下時の処理を設定
			Runnable openAction = null;
			if (isButtonRequired) {
				openAction = new Runnable() {
					@Override
					public void run() {
						popup.hide();
						afterService.openDirectory(targetPath);
						
					}
				};
			}
			// 閉じる押下時の処理を設定
			Runnable closeAction = new Runnable() {
				@Override
				public void run() {
					popup.hide();
				}
			};
			

			// コントローラにセットする
			controller.setup(noticeMessageKeyList, "保存先フォルダを開く", openAction, closeAction);

			popup.getContent().add(root); // Popupにrootを注入
			popup.show(stage); // メイン画面を親として、popup表示
			slideInFromBottom(popup, root); // 通知を右下から上に流す

			// 表示期間が過ぎると自動で消える処理
			PauseTransition delay = new PauseTransition(Duration.seconds(NOTIFICATION_DISPLAY_SECONDS));
			delay.setOnFinished(event -> popup.hide());
			delay.play();

		} catch (Exception e) {
			log.warn("通知表示に失敗しました", e);
		}
	}

	private void slideInFromBottom(Popup popup, Parent root) {
		Point2D endPosition = calculateBottomRightPosition(popup);

		// Popup自体は最終位置に置く
		popup.setX(endPosition.getX());
		popup.setY(endPosition.getY());

		// 中身だけ下にずらして、透明にする
		root.setTranslateY(NOTIFICATION_SLIDE_DISTANCE);
		root.setOpacity(NOTIFICATION_FADE_START_OPACITY);

		Timeline timeline = new Timeline(
				new KeyFrame(
						Duration.ZERO,
						new KeyValue(root.translateYProperty(), NOTIFICATION_SLIDE_DISTANCE),
						new KeyValue(root.opacityProperty(), NOTIFICATION_FADE_START_OPACITY)),
				new KeyFrame(
						Duration.millis(NOTIFICATION_SLIDE_MILLIS),
						new KeyValue(root.translateYProperty(), 0),
						new KeyValue(root.opacityProperty(), NOTIFICATION_FADE_END_OPACITY)));

		timeline.play();
	}

	private Point2D calculateBottomRightPosition(Popup popup) {
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

		double x = bounds.getMaxX() - popup.getWidth() - NOTIFICATION_MARGIN;
		double y = bounds.getMaxY() - popup.getHeight() - NOTIFICATION_MARGIN;

		return new Point2D(x, y);
	}

}
