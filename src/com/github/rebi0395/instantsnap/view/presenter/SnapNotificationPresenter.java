package com.github.rebi0395.instantsnap.view.presenter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.MessageKeyConstant;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.model.bean.SnapResultBean;
import com.github.rebi0395.instantsnap.service.FileOpenService;
import com.github.rebi0395.instantsnap.view.StyleSheetUtil;
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
	private final Stage stage;

	/** 通知表示秒数 */
	private static final double NOTIFICATION_DISPLAY_SECONDS = 5.0;

	/** 一時保存時の通知表示秒数 */
	private static final double NOTIFICATION_DISPLAY_LONG_SECONDS = 8.0;

	/** 通知表示位置の画面端からの余白 */
	private static final double NOTIFICATION_MARGIN = 20.0;

	/** 通知フェードイン移動量 */
	private static final double NOTIFICATION_FADE_IN_DISTANCE = 40.0;

	/** 通知フェードイン時間 */
	private static final double NOTIFICATION_FADE_IN_MILLIS = 250.0;

	/** 通知フェードアウト移動量 */
	private static final double NOTIFICATION_FADE_OUT_DISTANCE = 40.0;

	/** 通知フェードアウト時間 */
	private static final double NOTIFICATION_FADE_OUT_MILLIS = 250.0;

	/** 通知非表示時の透明度 */
	private static final double NOTIFICATION_HIDE_OPACITY = 0.0;

	/** 通知表示時の透明度 */
	private static final double NOTIFICATION_SHOW_OPACITY = 1.0;

	/** コンストラクタ */
	public SnapNotificationPresenter(Stage stage) {
		this.stage = stage;
	}

	/**
	 * 通知を表示する。
	 * 
	 * @param result 撮影結果インスタンス
	 * @param fileOpen ファイルを開くインスタンス
	 * @param converter メッセージキーをメッセージに変換するクラス
	 */
	public void show(SnapResultBean result, FileOpenService fileOpen, MessageKeyConverter converter) {

		if (!result.hasResult()) {
			// 通知の表示が不要な場合、早期リターン
			return;
		}

		// 通知で使用するデータをレコードにまとめる
		final NotificationData notificationData = createNotificationData(result, converter);

		// JavaFXの専用スレッドに処理を依頼する
		Platform.runLater(() -> {
			showNotification(notificationData, fileOpen);
		});

	}

	/**
	 * 通知画面の作成を行い、通知を表示する
	 * 
	 * @param notificationData 通知表示に必要な情報
	 * @param fileOpen ファイルを開く処理を使用する
	 */
	private void showNotification(NotificationData notificationData, FileOpenService fileOpen) {

		try {
			// FXMLローダーを作成
			FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_NOTIFICATION));

			// 通知コントローラを生成し、FXMLローダーにセットする
			SnapNotificationController controller = new SnapNotificationController();
			loader.setController(controller);

			// FXMLに読み込む
			Parent root = loader.load();
			
			// cssを適用する
			StyleSheetUtil.applyStyleSheet(root, CSS_NOTIFICATION);

			// Popupを設定
			Popup popup = new Popup();
			popup.setAutoFix(true); // 画面外に出にくくする
			popup.setHideOnEscape(true); // Esc押下で閉じる

			// 通知が持つアクションを取得する
			NotificationAction notificationAction = createNotificationAction(notificationData, popup, root, fileOpen);

			// コントローラに通知の文言、通知アクションをまとめてセットする
			controller.setup(notificationData.messageList(), notificationData.openMessage(),
					notificationAction.openAction(), notificationAction.closeAction());

			popup.getContent().add(root); // Popupにrootを注入
			showWithFadeIn(popup, root); // 通知を画面右下に配置し、下からフェードイン表示する

			// 通知の表示時間を設定する
			double displaySecond = NOTIFICATION_DISPLAY_SECONDS;
			if (notificationData.isTmpSaved()) {
				// 一時保存のときは文言が長いため、表示時間を長くする
				displaySecond = NOTIFICATION_DISPLAY_LONG_SECONDS;
			}
			// 指定秒数待つタイマーを作成
			PauseTransition delay = new PauseTransition(Duration.seconds(displaySecond));

			// 指定秒数が過ぎると自動で消える処理を登録
			delay.setOnFinished(event -> {
				hideWithFadeOut(popup, root);
			});
			delay.play(); // 秒数カウント開始

		} catch (Exception e) {
			log.warn("通知表示に失敗しました", e);
		}
	}

	/**
	 * 撮影結果を元に、通知に必要な情報を通知情報レコードにまとめ、返却する
	 * @param result 撮影結果クラス
	 * @param converter メッセージ取得クラス
	 * @return 通知情報
	 */
	private NotificationData createNotificationData(SnapResultBean result, MessageKeyConverter converter) {
		// メッセージリスト
		List<String> noticeMessageList = new ArrayList<String>();

		if (result.getIsImageSaved()) {
			// 画像保存に成功
			noticeMessageList.add(converter.getMessage(MessageKeyConstant.NOTIFICATION_IMAGE_SAVED));
		}

		if (result.getIsClipboardCopied()) {
			// クリップボードコピーに成功
			noticeMessageList.add(converter.getMessage(MessageKeyConstant.NOTIFICATION_CLIPBOARD_SAVED));
		}

		if (result.getIsTmpSaved()) {
			// 一時保存に成功
			noticeMessageList.add(converter.getMessage(MessageKeyConstant.NOTIFICATION_TMP_IMAGE_SAVED));
			noticeMessageList.add(converter.getMessage(MessageKeyConstant.NOTIFICATION_TMP_WARN));
		}

		// 通知クリックでフォルダが開けることを伝える文言を取得
		String openMessage = converter.getMessage(MessageKeyConstant.NOTIFICATION_OPEN_SAVE_DIR);

		return new NotificationData(
				List.copyOf(noticeMessageList),
				openMessage,
				result.getTargetPath(),
				result.isOpenActionAvailable(),
				result.getIsTmpSaved());
	}

	/**
	 * 通知が持つアクションを作成し、通知アクションレコードにまとめ、返却する
	 * 
	 * @param notificationData 通知情報
	 * @param popup 通知ポップアップ
	 * @param root ポップアップの中身
	 * @param fileOpen ファイルを開く処理を持つインスタンス
	 * @return 通知アクションを持つレコード
	 */
	private NotificationAction createNotificationAction(NotificationData notificationData, Popup popup,
			Parent root, FileOpenService fileOpen) {

		// 画像保存がある場合、通知クリック時の処理を設定
		Runnable openAction = null;
		if (notificationData.openActionAvailable) {
			openAction = new Runnable() {
				@Override
				public void run() {
					hideWithFadeOut(popup, root);
					fileOpen.openDirectory(notificationData.targetPath());
				}
			};
		}

		// 閉じる押下時の処理を設定
		Runnable closeAction = new Runnable() {
			@Override
			public void run() {
				hideWithFadeOut(popup, root);
			}
		};

		return new NotificationAction(openAction, closeAction);
	}

	/**
	 * 通知を右下に設定し、下からフェードインして表示する
	 * <p>
	 * Popup本体は動かさず、中身のrootだけを動かす。
	 * rootは通知UI全体を含むため、見た目上は通知全体が動いて見える。
	 * </p>
	 * 
	 * @param popup 通知ウィンドウ
	 * @param root FXML の最上位ノード（Parent）
	 */
	private void showWithFadeIn(Popup popup, Parent root) {

		// popupの中身を透明にし、show直後に見えないようにする
		root.setOpacity(NOTIFICATION_HIDE_OPACITY);

		// show後にpopupの幅、高さが取得できるようになるため、一度先に表示
		popup.show(stage); // メイン画面を親と指定

		// 通知の表示座標を取得する
		Point2D endPosition = calculatePopupPosition(popup);

		// Popup自体は最終位置に置く
		popup.setX(endPosition.getX());
		popup.setY(endPosition.getY());

		// フェードイン開始時。中身(root)をポップアップの座標から下にずらす
		KeyFrame startFrame = new KeyFrame(
				Duration.ZERO,
				// translateYは本来の座標からどれだけずらすかを指定する
				new KeyValue(root.translateYProperty(), NOTIFICATION_FADE_IN_DISTANCE),
				new KeyValue(root.opacityProperty(), NOTIFICATION_HIDE_OPACITY));
		// フェードイン終了時。中身の座標を元に戻し、透明化を解除する
		KeyFrame endFrame = new KeyFrame(
				Duration.millis(NOTIFICATION_FADE_IN_MILLIS),
				new KeyValue(root.translateYProperty(), 0),
				new KeyValue(root.opacityProperty(), NOTIFICATION_SHOW_OPACITY)); // 表示させる

		// 通知表示アニメーションを作成  JavaFXが二つの状態の間を補間し、なめらかに変化させる
		Timeline fadeIn = new Timeline(startFrame, endFrame);
		// 再生
		fadeIn.play();
	}

	/**
	 * 通知をフェードアウトして、閉じる
	 * <p>
	 * Popup本体は動かさず、中身のrootだけを動かす。
	 * rootは通知UI全体を含むため、見た目上は通知全体が動いて見える。
	 * </p>
	 * 
	 * @param popup 通知ウィンドウ
	 * @param root FXML の最上位ノード（Parent）
	 */
	private void hideWithFadeOut(Popup popup, Parent root) {
		// もう閉じているなら早期リターン
		if (!popup.isShowing()) {
			return;
		}
		// フェードアウト開始時点
		KeyFrame startFrame = new KeyFrame(
				Duration.ZERO,
				new KeyValue(root.translateYProperty(), 0),
				new KeyValue(root.opacityProperty(), NOTIFICATION_SHOW_OPACITY));
		// フェードアウト終了時点
		KeyFrame endFrame = new KeyFrame(
				Duration.millis(NOTIFICATION_FADE_OUT_MILLIS),
				new KeyValue(root.translateYProperty(), NOTIFICATION_FADE_OUT_DISTANCE),
				new KeyValue(root.opacityProperty(), NOTIFICATION_HIDE_OPACITY));

		Timeline fadeOut = new Timeline(startFrame, endFrame);
		// フェードアウト終了時、ポップアップを閉じるイベントを登録
		fadeOut.setOnFinished(event -> {
			popup.hide();
		});
		fadeOut.play(); // 再生
	}

	/**
	 * 通知を画面右下に表示するための座標計算を行う
	 * 
	 * @param popup 通知
	 * @return 通知の左上座標
	 */
	private Point2D calculatePopupPosition(Popup popup) {
		// メインモニターの表示可能領域を取得 (タスクバーなどを除いた領域)
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

		// 右下にいくほどX,Yが増加するため、画面の右下の座標はMaxで取得し
		// ポップアップと幅(高さ)と、指定した余白を引き、ポップアップ左上の座標を算出する
		double x = bounds.getMaxX() - popup.getWidth() - NOTIFICATION_MARGIN;
		double y = bounds.getMaxY() - popup.getHeight() - NOTIFICATION_MARGIN;

		return new Point2D(x, y); // ポップアップ左上の座標
	}

	/**
	 * 通知表示に必要な情報をまとめたレコード
	 *
	 * @param messageList 通知メッセージリスト
	 * @param openMessage 保存先フォルダを開く表示文言
	 * @param targetPath 保存先パス
	 * @param openActionAvailable 通知クリックで保存先を開けるならtrue
	 * @param isTmpSaved 一時保存ならtrue
	 */
	private record NotificationData(
			List<String> messageList,
			String openMessage,
			Path targetPath,
			boolean openActionAvailable,
			boolean isTmpSaved) {
	}

	/**
	 * 通知に必要なアクションをまとめて持つレコード
	 * 
	 * @param openAction 保存先フォルダを開くアクション 画像保存でない場合はnull
	 * @param closeAction 通知を閉じるアクション
	 */
	private record NotificationAction(
			Runnable openAction,
			Runnable closeAction) {
	}
}
