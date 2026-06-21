package com.github.rebi0395.instantsnap.view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ExitDialogController {

	/** 終了ボタン */
	@FXML
	private Button okButton;

	/** 終了キャンセルボタン */
	@FXML
	private Button cancelButton;

	/** 終了するかの結果 */
	private boolean result = false;

	/**
	 * 初期化メソッド ボタンのアクションを設定
	 */
	@FXML
	public void initialize() {
		okButton.setOnAction(e -> {
			result = true;
			close();
		});

		cancelButton.setOnAction(e -> {
			result = false;
			close();
		});
	}

	/**
	 * アプリを終了するかを返却する
	 * @return アプリの終了するか trueで終了する
	 */
	public boolean getResult() {
		return result;
	}

	/**
	 * ダイアログを閉じる
	 */
	private void close() {
		Stage stage = (Stage) okButton.getScene().getWindow();
		stage.close();
	}
}
