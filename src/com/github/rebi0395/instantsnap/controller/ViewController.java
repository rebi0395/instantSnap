package com.github.rebi0395.instantsnap.controller;

import java.io.File;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ViewController {

	@FXML private Label statusLabel;
	
	@FXML private RadioButton mode1;
	@FXML private RadioButton mode2;
	@FXML private RadioButton mode3;
	@FXML private RadioButton modeOther;
	
	@FXML private TextField opt1;
	@FXML private TextField opt2;
	@FXML private TextField opt3;

	@FXML private TextField filePath;
	
	// String型のまま紐付け
	@FXML private ComboBox<String> rangeBox;
	@FXML private ComboBox<String> extBox;
	@FXML private ComboBox<String> afterBox;
	@FXML private CheckBox checkOption;

	@FXML
	public void initialize() {
		// コンボボックスの初期選択（FXMLの1番上の項目が選ばれます）
		rangeBox.getSelectionModel().selectFirst();
		extBox.getSelectionModel().selectFirst();
		afterBox.getSelectionModel().selectFirst();

		// ToggleGroup をコード側で作る
		ToggleGroup group = new ToggleGroup();
		mode1.setToggleGroup(group);
		mode2.setToggleGroup(group);
		mode3.setToggleGroup(group);
		modeOther.setToggleGroup(group);
		mode1.setSelected(true); // デフォルト選択

		// その他選択時のテキスト有効化・無効化
		modeOther.selectedProperty().addListener((obs, oldV, newV) -> {
			opt1.setDisable(!newV);
			opt2.setDisable(!newV);
			opt3.setDisable(!newV);
		});
	}

	/**
	 * ② 保存先フォルダ選択ボタンの処理
	 */
	@FXML
	public void onSelectFile() {
		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle("保存先フォルダを選択");
		File folder = dc.showDialog(new Stage());
		if (folder != null) {
			filePath.setText(folder.getAbsolutePath());
		}
	}

	/**
	 * 「設定を反映」ボタンが押されたときの処理
	 */
	@FXML
	public void onApplyConfig() {
		String folderPath = filePath.getText();
		String selectedExt = extBox.getValue(); // そのまま String で取得
		boolean isOptionEnabled = checkOption.isSelected();
		
		if (folderPath == null || folderPath.isEmpty()) {
			statusLabel.setText("ステータス: 保存先フォルダを選択してください。");
			return;
		}

		// 反映された値をコンソールで確認
		System.out.println("--- 設定を反映しました ---");
		System.out.println("フォルダパス: " + folderPath);
		System.out.println("選択された拡張子: " + selectedExt);
		System.out.println("オプション: " + isOptionEnabled);

		statusLabel.setText("ステータス: 設定を反映しました。");
	}

	/**
	 * 「設定を初期化」ボタンが押されたときの処理
	 */
	@FXML
	public void onResetConfig() {
		mode1.setSelected(true); // ラジオボタン初期化
		filePath.clear();         // フォルダパスを空に
		
		// 各コンボボックスを一番上にリセット
		rangeBox.getSelectionModel().selectFirst();
		extBox.getSelectionModel().selectFirst();
		afterBox.getSelectionModel().selectFirst();
		
		checkOption.setSelected(false); // チェック解除
		
		statusLabel.setText("ステータス: 設定を初期化しました。");
	}
}