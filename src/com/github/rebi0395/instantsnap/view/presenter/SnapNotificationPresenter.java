package com.github.rebi0395.instantsnap.view.presenter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.view.controller.SnapNotificationController;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class SnapNotificationPresenter {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SnapNotificationPresenter.class);
	
	/** メインウィンドウのステージ */
	private Stage stage;

	/** コンストラクタ */
	public SnapNotificationPresenter(Stage stage) {
		this.stage = stage;
	}

	/**
	 * 撮影結果の通知を表示する
	 * 
	 * @param config ユーザー設定値
	 * @param targetPath ファイルを保存したパス
	 * @param isImageSaved 画像保存に成功したか
	 * @param isClipboardCopied クリップボード保存に成功したか
	 */
	public void showNotification(ConfigBean config, Path targetPath,
			boolean isImageSaved, boolean isClipboardCopied) {

		// ボタンフラグ
		boolean isButtonRequired = false;
		// メッセージリスト
		List<String> noticeMessageList = new ArrayList<String>();

		if (config.getIsSaveImage() && isImageSaved) {
			// 画像を保存している場合
			isButtonRequired = (targetPath != null);
			noticeMessageList.add("スクリーンショットを保存しました");

		} else if (!config.getIsSaveImage() && isImageSaved) {
			// 一時保存ファイルに画像を保存した場合
			isButtonRequired = (targetPath != null);
			noticeMessageList.add("一時保存ファイルにスクリーンショットを保存しました");

		}

		if (isClipboardCopied) {
			// クリップボードに保存している場合
			noticeMessageList.add("クリップボードに画像を保存しました");
		}

		if (noticeMessageList.isEmpty()) {
			// メッセージがない場合、早期リターン
			return;
		}

		// ラムダ式を使うため、final変数に代入
		final boolean isFinalButtonRequired = isButtonRequired;
		final List<String> messages = List.copyOf(noticeMessageList);

		// JavaFXの専用スレッドに処理を依頼する
		Platform.runLater(() -> {
			try {
				show(messages, targetPath, isFinalButtonRequired);
			} catch (ServiceException e) {
				log.warn("通知表示に失敗しました", e);
			}

		});

	}

	private void show(List<String> noticeMessageList,
			Path targetPath, boolean isButtonRequired) throws ServiceException {

		try {

			// FXMLローダーを作成
			FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_NOTIFICATION));
			Parent root = loader.load();

			// fxmlファイルに紐づいたコントローラを取得 (fxml側でコントローラ指定が前提)
			SnapNotificationController controller = loader.getController();

			// Popupを設定
			Popup popup = new Popup();
			popup.setAutoFix(true); // 画面外に出にくくする
			popup.setAutoHide(true); // 外側クリックで閉じる
			popup.setHideOnEscape(true); // Esc押下で閉じる

			// ボタン押下時の処理を設定
			Runnable openAction = null;
			if (isButtonRequired) {
				openAction = new Runnable() {
					@Override
					public void run() {
						popup.hide();
						// TODO ファイルを開く処理
					}
				};
			}

			//TODO set部分をつくる
			
			// Popupにrootを注入
			popup.getContent().add(root);
			
			//TODO Stageを引数にしたい
			
			
			
			
			
		} catch (Exception e) {

		}

	}

}
