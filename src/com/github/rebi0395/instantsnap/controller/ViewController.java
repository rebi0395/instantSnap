package com.github.rebi0395.instantsnap.controller;

import java.io.File;

import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.SnapAfter;
import com.github.rebi0395.instantsnap.model.enums.SnapArea;
import com.github.rebi0395.instantsnap.service.ServiceFacade;

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

	/** サービスクラスの司令塔クラス */
	private ServiceFacade facade;
	
	@FXML private Label statusLabel;
	
	@FXML private RadioButton mode1;
	@FXML private RadioButton mode2;
	@FXML private RadioButton mode3;
	@FXML private RadioButton modeOther;
	
	@FXML private TextField opt1;
	@FXML private TextField opt2;
	@FXML private TextField opt3;

	/** 保存先のフォルダを表示するテキストフィールド */
	@FXML private TextField filePath;
	
	/** 撮影範囲を選択するコンボボックス */
	@FXML private ComboBox<SnapArea> snapAreaBox;
	/** 拡張子を選択するコンボボックス */
	@FXML private ComboBox<FormatType> formatBox;
	/** 撮影後設定を選択するコンボボックス */
	@FXML private ComboBox<SnapAfter> afterBox;
	/** クリップボードに画像を保存するチェックボックス */
	@FXML private CheckBox checkOption;

	/**
	 * 初期化処理。FXMLファイルの要素を取り込んだ後に呼び出す
	 */
	@FXML
	public void initialize() {
		
		ConfigBean config = facade.getConfigBean();
		
		// 保存先のフォルダをテキストフィールドにセット
		config.getSavePath();
		filePath.setText(config.getSavePath());
		
		// 撮影範囲のリストをセット
		snapAreaBox.getItems().addAll(SnapArea.values());
		snapAreaBox.setConverter(SnapArea.toConverter()); // コンバーターにより	Enumsをdisplaynameで変換が可能になる
		snapAreaBox.setValue(config.getSnapArea());
		
		// 拡張子のリストをセット
		formatBox.getItems().addAll(FormatType.values());
		formatBox.setConverter(FormatType.toConverter());
		formatBox.setValue(config.getFormatType());

		// 撮影後設定のリストをセット
		afterBox.getItems().addAll(SnapAfter.values());
		afterBox.setConverter(SnapAfter.toConverter());
		afterBox.setValue(config.getSnapAfter());
		
		// クリップボードに画像をするかのチェックボックスをセット
		checkOption.setSelected(config.getIsCopyToClipboard());

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
	 * 保存先フォルダ選択ボタンの処理
	 */
	@FXML
	public void onSelectFile() {
		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle("保存先フォルダを選択");
		File folder = dc.showDialog(new Stage());
		if (folder != null) {
			filePath.setText(folder.getAbsolutePath().replace("\\", "/")); // パス文字を置換する
		}
	}

	/**
	 * 「設定を反映」ボタンが押されたときの処理
	 */
	@FXML
	public void onApplyConfig() {
		
		ConfigBean config = facade.getConfigBean();
		// configに値をセット
		config.setSavePath(filePath.getText()); // 保存パス
		config.setSnapArea(snapAreaBox.getValue()); // 撮影範囲
		config.setFormatType(formatBox.getValue()); // 拡張子
		config.setSnapAfter(afterBox.getValue()); // 撮影後設定
		config.setIsCopyToClipboard(checkOption.isSelected()); // クリップボード保存の有無
		
		// configを更新し、司令塔クラスに返す
		facade.reset(config);
		
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
		snapAreaBox.getSelectionModel().selectFirst();
		formatBox.getSelectionModel().selectFirst();
		afterBox.getSelectionModel().selectFirst();
		
		checkOption.setSelected(false); // チェック解除
		
		statusLabel.setText("ステータス: 設定を初期化しました。");
	}

	/**
	 * サービスクラスの司令塔クラスを取得する
	 * @return facade
	 */
	public ServiceFacade getFacade() {
		return facade;
	}

	/**
	 * サービスクラスの司令塔クラスをセットする
	 * @param facade セットする facade
	 */
	public void setFacade(ServiceFacade facade) {
		this.facade = facade;
	}
}