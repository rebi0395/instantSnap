package com.github.rebi0395.instantsnap.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;

import javafx.application.Platform;

public class NotificationService {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

	/** コンストラクタ */
	public NotificationService() {
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

	}

}
