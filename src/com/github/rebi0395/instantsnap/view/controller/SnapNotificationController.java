package com.github.rebi0395.instantsnap.view.controller;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * 撮影結果通知画面のコントローラクラス
 */
public class SnapNotificationController {

	/** 外枠 */
	@FXML
	private HBox rootNotification;

	/** 通知タイトル */
	@FXML
	private Label labelTitle;

	/** 通知メッセージ */
	@FXML
	private Label labelMessage;

	/** 保存先を開く文言のラベル */
	@FXML
	private Label labelOpenFolder;

	/** 通知を閉じるラベル */
	@FXML
	private Label labelClose;

	/**
	 * 通知の文言やアクションを設定する
	 * 
	 * @param messageList 表示するメッセージリスト
	 * @param openMessage クリック時にフォルダを開けることを通知するメッセージ
	 * @param openAction クリック時にフォルダを開く動作 画像保存でない場合はnull
	 * @param closeAction 通知を閉じる動作
	 */
	public void setup(List<String> messageList, String openMessage, Runnable openAction, Runnable closeAction) {

		// メッセージを設定
		labelTitle.setText("InstantSnap");
		labelMessage.setText(String.join(System.lineSeparator(), messageList));

		// 画像保存があり、フォルダを開く処理を搭載する場合
		if (openAction != null) {
			// ラベルを表示する
			labelOpenFolder.setText(openMessage);
			labelOpenFolder.setVisible(true);
			labelOpenFolder.setManaged(true);
			// 外枠クリックでフォルダを開くアクションを設定
			rootNotification.setCursor(Cursor.HAND); // マウスカーソルを手の形にする
			rootNotification.setOnMouseClicked(event -> {
				openAction.run();
			});
			// 画像保存がなく、フォルダを開く処理が不要な場合
		} else {
			// ラベルを表示しない
			labelOpenFolder.setVisible(false);
			labelOpenFolder.setManaged(false);
			labelOpenFolder.setText(null);
		}

		// 閉じるラベルを押下で通知を閉じるアクションを設定
		labelClose.setCursor(Cursor.HAND); // マウスカーソルを手の形にする
		labelClose.setOnMouseClicked(event -> {
			event.consume();
			closeAction.run();
		});
	}
}