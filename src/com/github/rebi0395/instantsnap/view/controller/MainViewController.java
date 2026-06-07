package com.github.rebi0395.instantsnap.view.controller;

import java.io.File;

import com.github.rebi0395.instantsnap.app.InstantSnapCoordinator;
import com.github.rebi0395.instantsnap.common.AppLogger;
import com.github.rebi0395.instantsnap.common.NativeKeyCodeConverter;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.KeyCombinationBean;
import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.SnapAfter;
import com.github.rebi0395.instantsnap.model.enums.SnapArea;
import com.github.rebi0395.instantsnap.model.enums.TriggerType;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;
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

public class MainViewController {

	/** サービスクラスの司令塔クラス */
	private InstantSnapCoordinator facade;
	/** nativeキーコードを保持する変数 */
	private int nativeCode;

	// -- ラベル --
	/** 操作完了やエラーメッセージを表示するラベル */
	@FXML
	private Label labelStatus;

	/** 適応中のキー設定を表示するラベル */
	@FXML
	private Label labelKeyCombination;

	/** 適応中の保存先ファイルパスを表示するラベル */
	@FXML
	private Label labelFilePath;

	/** 適応中の拡張子を表示するラベル */
	@FXML
	private Label labelFormatType;

	/** 適応中の撮影範囲を表示するラベル */
	@FXML
	private Label labelSnapArea;

	/** 適応中の撮影後設定を表示するラベル */
	@FXML
	private Label labelSnapAfter;

	// -- 基本設定項目 --
	/** カスタムキー設定のキー入力を受け付けるテキストフィールド */
	@FXML
	private TextField textInputKey;
	/** コンビネーションキーとしてAltを使用するかを選択するチェックボックス */
	@FXML
	private CheckBox checkAlt;
	/** コンビネーションキーとしてCtrlを使用するかを選択するチェックボックス */
	@FXML
	private CheckBox checkCtrl;
	/** コンビネーションキーとしてShiftを使用するかを選択するチェックボックス */
	@FXML
	private CheckBox checkShift;

	/** 撮影ショートカットキーを選択するコンボボックス */
	@FXML
	private ComboBox<TriggerType> comboTriggerType;

	/** 保存先のフォルダを表示するテキストフィールド */
	@FXML
	private TextField textFilePath;

	/** 拡張子を選択するコンボボックス */
	@FXML
	private ComboBox<FormatType> comboFormatType;

	/** 撮影範囲を選択するコンボボックス */
	@FXML
	private ComboBox<SnapArea> comboSnapArea;

	/** 撮影後設定を選択するコンボボックス */
	@FXML
	private ComboBox<SnapAfter> comboSnapAfter;

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
		comboTriggerType.valueProperty().addListener((obs, oldType, newType) -> {
			updateCustomKeyFieldState();
		});

		// カスタムキーのテキストフィールドを監視するリスナー
		textInputKey.textProperty().addListener((obs, oldVal, newVal) -> {
			updateCustomKeyFieldState();
		});

		// カスタムキーのテキストフィールドの挙動を設定
		inputCustomKey(textInputKey);

		// -- ユーザー設定値のセット -- 
		// 保存先のフォルダを取得し、テキストフィールドにセット
		config.getSavePath();
		textFilePath.setText(config.getSavePath());

		// ショートカットキーのリストをセット
		comboTriggerType.getItems().addAll(TriggerType.values());
		comboTriggerType.setConverter(TriggerType.toConverter()); // コンバーターにより	Enumsをdisplaynameで変換が可能になる
		comboTriggerType.setValue(config.getTriggerType());

		// カスタムキーの取得
		KeyCombinationBean customKeys = config.getCustomKeyBean();
		// トリガーキーの保持、名前変換
		nativeCode = customKeys.getTriggerKey(); // nativeキーコードを保持
		if (!(nativeCode == VC_UNDEFINED)) {
			// 未定義でない場合、テキストフィールドにセットする
			textInputKey.setText(getKeyText(nativeCode));
		}

		// 修飾キー
		checkAlt.setSelected(customKeys.isUseAlt());
		checkCtrl.setSelected(customKeys.isUseCtrl());
		checkShift.setSelected(customKeys.isUseShift());

		// 撮影範囲のリストをセット
		comboSnapArea.getItems().addAll(SnapArea.values());
		comboSnapArea.setConverter(SnapArea.toConverter()); // コンバーターにより	Enumsをdisplaynameで変換が可能になる
		comboSnapArea.setValue(config.getSnapArea());

		// 拡張子のリストをセット
		comboFormatType.getItems().addAll(FormatType.values());
		comboFormatType.setConverter(FormatType.toConverter());
		comboFormatType.setValue(config.getFormatType());

		// 撮影後設定のリストをセット
		comboSnapAfter.getItems().addAll(SnapAfter.values());
		comboSnapAfter.setConverter(SnapAfter.toConverter());
		comboSnapAfter.setValue(config.getSnapAfter());

		// クリップボードに画像をするかのチェックボックスをセット
		checkClipboard.setSelected(config.getIsCopyToClipboard());

		// -- 文字列反映 --
		updateSettingLabels(config, customKeys);

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
			textFilePath.setText(folder.getAbsolutePath().replace("\\", "/")); // パス文字を置換する
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
			// ログレベルがtraceであればキーログを出力する
			AppLogger.keyPressed(keyCode);

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
		boolean isCustom = (comboTriggerType.getValue() == TriggerType.CUSTOM);
		boolean hasKey = (isNotBlankSafe(textInputKey.getText()));

		// Custom設定ならテキストフィールドを活性
		textInputKey.setDisable(!isCustom);

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
		config.setSavePath(textFilePath.getText()); // 保存パス
		config.setTriggerType(comboTriggerType.getValue()); // トリガーキー
		config.setSnapArea(comboSnapArea.getValue()); // 撮影範囲
		config.setFormatType(comboFormatType.getValue()); // 拡張子
		config.setSnapAfter(comboSnapAfter.getValue()); // 撮影後設定
		config.setIsCopyToClipboard(checkClipboard.isSelected()); // クリップボード保存の有無

		// カスタムキーの設定を保存する
		KeyCombinationBean customKeys = config.getCustomKeyBean();
		customKeys.setTriggerKey(nativeCode); // 保持しているキーコードをいれる
		customKeys.setUseAlt(checkAlt.isSelected());
		customKeys.setUseCtrl(checkCtrl.isSelected());
		customKeys.setUseShift(checkShift.isSelected());

		// 文字列を更新する
		updateSettingLabels(config, customKeys);

		// configを更新し、司令塔クラスに返す
		facade.save(config);
		labelStatus.setText("ステータス: 設定を反映しました。");
	}

	/**
	 * 「設定を初期化」ボタンが押されたときの処理
	 */
	@FXML
	public void onResetConfig() {
		// フォルダパスをデフォルトに設定
		textFilePath.setText(DEFAULT_SAVE_PATH);

		// カスタムキーの設定をリセットする
		textInputKey.clear();
		checkAlt.setSelected(false);
		checkCtrl.setSelected(false);
		checkShift.setSelected(false);

		// 各コンボボックスを一番上にリセット
		comboTriggerType.getSelectionModel().selectFirst();
		comboFormatType.getSelectionModel().selectFirst();
		comboSnapArea.getSelectionModel().selectFirst();
		comboSnapAfter.getSelectionModel().selectFirst();

		checkClipboard.setSelected(true); // チェックをつける
		labelStatus.setText("ステータス: 入力値を初期化しました");
	}

	/**
	 * 設定更新時、現在の設定値を表示するラベルを一斉に更新する
	 * @param config ユーザー設定値
	 * @param customKeys ユーザーのカスタムキーの設定値
	 */
	public void updateSettingLabels(ConfigBean config, KeyCombinationBean customKeys) {
		labelKeyCombination
				.setText(config.getTriggerType().resolveKeyCombination(customKeys).resolveKeyCombinationName());
		labelFilePath.setText(config.getSavePath());
		labelFormatType.setText(config.getFormatType().getDisplayName());
		labelSnapArea.setText(config.getSnapArea().getDisplayName());
		labelSnapAfter.setText(config.getSnapAfter().getDisplayName());
	}

	/**
	 * サービスクラスの司令塔クラスを取得する
	 * @return facade
	 */
	public InstantSnapCoordinator getFacade() {
		return facade;
	}

	/**
	 * サービスクラスの司令塔クラスをセットする
	 * @param facade セットする facade
	 */
	public void setFacade(InstantSnapCoordinator facade) {
		this.facade = facade;
	}
}