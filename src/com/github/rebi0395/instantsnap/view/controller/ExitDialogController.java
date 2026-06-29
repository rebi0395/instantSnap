package com.github.rebi0395.instantsnap.view.controller;

import com.github.rebi0395.instantsnap.common.MessageKeyConstant;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * アプリ終了ダイアログのコントローラクラス
 */
public class ExitDialogController {

	/** メッセージキー変換クラス */
	private MessageKeyConverter converter;

	/** 終了確認文言ラベル */
	@FXML
	private Label labelExitConfirmMessage;

	/** 一時保存フォルダ削除確認ラベル */
	@FXML
	private Label labelDeleteTmpConfirmMessage;

	/** 終了ボタン */
	@FXML
	private Button buttonApply;

	/** キャンセルボタン */
	@FXML
	private Button buttonCancel;

	/** 終了するかの結果 */
	private boolean result = false;

	/** コンストラクタ */
	public ExitDialogController(MessageKeyConverter converter) {
		this.converter = converter;
	}

	/**
	 * 初期化メソッド ボタンのアクションを設定
	 */
	@FXML
	public void initialize() {

		// アプリ終了文言
		labelExitConfirmMessage.setText(converter.getMessage(MessageKeyConstant.DIALOG_EXIT_CONFIRM_MESSAGE));
		// フォルダ削除文言
		labelDeleteTmpConfirmMessage.setText(converter.getMessage(MessageKeyConstant.DIALOG_EXIT_DELETE_TMP));
		// 終了ボタン
		buttonApply.setText(converter.getMessage(MessageKeyConstant.DIALOG_EXIT_BUTTON_APPLY));
		// キャンセルボタン
		buttonCancel.setText(converter.getMessage(MessageKeyConstant.DIALOG_EXIT_BUTTON_CANCEL));
		
		// 終了ボタン押下処理登録
		buttonApply.setOnAction(e -> {
			result = true;
			close();
		});

		// キャンセルボタン押下処理登録
		buttonCancel.setOnAction(e -> {
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
		Stage stage = (Stage) buttonApply.getScene().getWindow();
		stage.close();
	}
}
