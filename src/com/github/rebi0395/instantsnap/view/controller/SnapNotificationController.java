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
	 * 通知表示内容を設定する
	 *
	 * @param messages 通知メッセージ一覧
	 * @param openFolderText フォルダを開けることを通知する文言
	 * @param openAction 通知押下時の処理。画像保存、一時保存がない場合はnull
	 * @param closeAction 閉じる押下時の処理
	 */
	public void setup(List<String> messages, String openFolderText, Runnable openAction, Runnable closeAction) {
		labelTitle.setText("InstantSnap");
		labelMessage.setText(String.join(System.lineSeparator(), messages));

		// 閉じるラベルを押下で通知を閉じるアクションを設定
		labelClose.setCursor(Cursor.HAND); // マウスカーソルを手の形にする
		labelClose.setOnMouseClicked(event -> {
			event.consume();
			closeAction.run();
		});

		if (openAction == null) {
			// ラベルを表示しない
			labelOpenFolder.setVisible(false);
			labelOpenFolder.setManaged(false);
			labelOpenFolder.setText(null);
		} else {
			// ラベルを表示する
			labelOpenFolder.setText(openFolderText);
			labelOpenFolder.setVisible(true);
			labelOpenFolder.setManaged(true);
			// 外枠クリックでフォルダを開くアクションを設定
			rootNotification.setCursor(Cursor.HAND); // マウスカーソルを手の形にする
			rootNotification.setOnMouseClicked(event -> {
				openAction.run();
			});
		}
	}
}