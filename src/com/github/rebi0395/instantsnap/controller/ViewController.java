package com.github.rebi0395.instantsnap.controller;

import java.io.File;

import com.github.rebi0395.instantsnap.common.NativeKeyCodeConverter;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.KeyCombinationBean;
import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.SnapAfter;
import com.github.rebi0395.instantsnap.model.enums.SnapArea;
import com.github.rebi0395.instantsnap.model.enums.TriggerType;
import com.github.rebi0395.instantsnap.service.ServiceFacade;
import static com.github.rebi0395.instantsnap.common.StrCheckUtil.*;
import static com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ViewController {

	/** サービスクラスの司令塔クラス */
	private ServiceFacade facade;
	/** nativeキーコードを保持する変数 */
	private int nativeCode;

	@FXML
	private Label statusLabel;
	@FXML
	private Label keyCombinationLabel;

	@FXML
	private TextField inputKeyText;
	@FXML
	private CheckBox checkShift;
	@FXML
	private CheckBox checkAlt;
	@FXML
	private CheckBox checkCtrl;

	/** 保存先のフォルダを表示するテキストフィールド */
	@FXML
	private TextField filePath;

	/** 撮影ショートカットキーを選択するコンボボックス */
	@FXML
	private ComboBox<TriggerType> triggerKeyBox;
	/** 撮影範囲を選択するコンボボックス */
	@FXML
	private ComboBox<SnapArea> snapAreaBox;
	/** 拡張子を選択するコンボボックス */
	@FXML
	private ComboBox<FormatType> formatBox;
	/** 撮影後設定を選択するコンボボックス */
	@FXML
	private ComboBox<SnapAfter> afterBox;
	/** クリップボードに画像を保存するチェックボックス */
	@FXML
	private CheckBox checkClipboard;

	/**
	 * 初期化処理。FXMLファイルの要素を取り込んだ後に呼び出す
	 */
	@FXML
	public void initialize() {

		ConfigBean config = facade.getConfigBean();

		// -- 活性・非活性の動的な制御の登録 -- 
		// ショートカットキーのリストを監視するリスナー
		triggerKeyBox.valueProperty().addListener((obs, oldType, newType) -> {
			updateCustomKeyFieldState();
		});

		// カスタムキーのテキストフィールドを監視するリスナー
		inputKeyText.textProperty().addListener((obs, oldVal, newVal) -> {
			updateCustomKeyFieldState();
		});

		// カスタムキーのテキストフィールドの挙動を設定
		inputCustomKey(inputKeyText);

		// -- ユーザー設定値のセット -- 
		// 保存先のフォルダを取得し、テキストフィールドにセット
		config.getSavePath();
		filePath.setText(config.getSavePath());

		// ショートカットキーのリストをセット
		triggerKeyBox.getItems().addAll(TriggerType.values());
		triggerKeyBox.setConverter(TriggerType.toConverter()); // コンバーターにより	Enumsをdisplaynameで変換が可能になる
		triggerKeyBox.setValue(config.getTriggerType());

		// カスタムキーの取得
		KeyCombinationBean customKeys = config.getCustomKeyBean();
		// トリガーキーの保持、名前変換
		nativeCode = customKeys.getTriggerKey(); // nativeキーコードを保存しておく
		inputKeyText.setText(getKeyText(nativeCode));
		// 修飾キー
		checkAlt.setSelected(customKeys.isUseAlt());
		checkCtrl.setSelected(customKeys.isUseCtrl());
		checkShift.setSelected(customKeys.isUseShift());

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
		checkClipboard.setSelected(config.getIsCopyToClipboard());

		// -- 文字列反映 --
		// キー監視しているショートカットキーを表示する
		keyCombinationLabel.setText(config.getTriggerType().getKeyCombination(customKeys).toDisplayname());
	}

	/**
	 * 保存先フォルダ選択ボタン押下時、ディレクトリフォルダを選択させる
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
	 * カスタムキーの入力処理を行う
	 * @param textField テキストフィールド
	 */
	public void inputCustomKey(TextField textField) {
		// 文字入力イベントをキャンセルする
		textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
			// 通常のキー入力をキャンセルさせる
			event.consume();
		});

		// キーが押された瞬間に、キーコードとキーの表示名を表示する処理
		textField.setOnKeyPressed(event -> {
			// JavaFXのキーコードを取得
			KeyCode keyCode = event.getCode();
			// Nativeキーコードに変換し、フィールドで保持
			nativeCode = NativeKeyCodeConverter.toNativeKeyCode(keyCode);
			if (nativeCode == VC_UNDEFINED) {
				return; // マップにないキーは入力処理、イベント消費をしない
			}
			// 通常のキー押下イベントを消費させる
			event.consume();
			// テキストフィールドに表示する処理
			String keyText = getKeyText(nativeCode);
			textField.setText(keyText);
			textField.end(); // カーソルを右に移動
		});
	}

	/**
	 * カスタムキーのテキストフィールド、チェックボックスの活性、非活性を制御する
	 */
	public void updateCustomKeyFieldState() {
		boolean isCustom = (triggerKeyBox.getValue() == TriggerType.CUSTOM);
		boolean hasKey = (isNotBlankSafe(inputKeyText.getText()));

		// Custom設定ならテキストフィールドを活性
		inputKeyText.setDisable(!isCustom);

		// Custom設定かつ、カスタムキーのテキストフィールドに値があれば活性
		checkAlt.setDisable(!(isCustom && hasKey));
		checkCtrl.setDisable(!(isCustom && hasKey));
		checkShift.setDisable(!(isCustom && hasKey));
	}

	/**
	 * 「設定を反映」ボタンが押されたとき、ユーザーの設定値を保存する	
	 */
	@FXML
	public void onApplyConfig() {

		ConfigBean config = facade.getConfigBean();
		// configに値をセット
		config.setSavePath(filePath.getText()); // 保存パス
		config.setTriggerType(triggerKeyBox.getValue()); // トリガーキー
		config.setSnapArea(snapAreaBox.getValue()); // 撮影範囲
		config.setFormatType(formatBox.getValue()); // 拡張子
		config.setSnapAfter(afterBox.getValue()); // 撮影後設定
		config.setIsCopyToClipboard(checkClipboard.isSelected()); // クリップボード保存の有無

		// カスタムキーの設定を保存する
		KeyCombinationBean customKeys = config.getCustomKeyBean();
		customKeys.setTriggerKey(nativeCode); // 保持しているキーコードをいれる
		customKeys.setUseAlt(checkAlt.isSelected());
		customKeys.setUseCtrl(checkCtrl.isSelected());
		customKeys.setUseShift(checkShift.isSelected());

		// 最新設定のTriggerTypeから、コンビネーションキーを取得し、
		// 文字列を更新する
		keyCombinationLabel.setText(config.getTriggerType().getKeyCombination(customKeys).toDisplayname());

		// configを更新し、司令塔クラスに返す
		facade.save(config);
		statusLabel.setText("ステータス: 設定を反映しました。");
	}

	/**
	 * 「設定を初期化」ボタンが押されたときの処理
	 */
	@FXML
	public void onResetConfig() {
		filePath.clear(); // フォルダパスを空に

		// 各コンボボックスを一番上にリセット
		snapAreaBox.getSelectionModel().selectFirst();
		formatBox.getSelectionModel().selectFirst();
		afterBox.getSelectionModel().selectFirst();

		checkClipboard.setSelected(false); // チェック解除

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