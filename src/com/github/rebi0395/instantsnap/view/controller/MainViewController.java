package com.github.rebi0395.instantsnap.view.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.rebi0395.instantsnap.app.InstantSnapCoordinator;
import com.github.rebi0395.instantsnap.common.AppLogger;
import com.github.rebi0395.instantsnap.common.MessageManager;
import com.github.rebi0395.instantsnap.common.NativeKeyCodeConverter;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.KeyCombinationBean;
import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class MainViewController {

	/** サービスクラスの司令塔クラス */
	private InstantSnapCoordinator coordinator;
	/** メッセージの多言語変換クラス */
	private MessageManager mm;
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
	
	/** 画像を保存する設定の有効/無効を表示するラベル */
	@FXML
	private Label labelSaveImage;
	
	/** クリップボードに保存する設定の有効/無効を表示するラベル */
	@FXML
	private Label labelClipboard;
	
	/** 通知を表示する設定の有効/無効を表示するラベル */
	@FXML
	private Label labelNotification;
	
	/** 撮影音を鳴らす設定の有効/無効を表示するラベル */
	@FXML
	private Label labelSound;
	
	// -- 言語設定 --
	/** 撮影ショートカットキーを選択するコンボボックス */
	@FXML
	private ComboBox<LanguageType> comboLanguageType;

	// -- 撮影設定 --
	/** 撮影ショートカットキーを選択するコンボボックス */
	@FXML
	private ComboBox<TriggerType> comboTriggerType;

	/** 撮影範囲を選択するコンボボックス */
	@FXML
	private ComboBox<SnapArea> comboSnapArea;

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

	// -- 出力設定 --
	/** 画像を保存する設定のチェックボックス */
	@FXML
	private CheckBox checkSaveImage;

	/** クリップボードに画像を保存するチェックボックス */
	@FXML
	private CheckBox checkClipboard;

	/** 保存先のフォルダを表示するテキストフィールド */
	@FXML
	private TextField textFilePath;

	/** 拡張子を選択するコンボボックス */
	@FXML
	private ComboBox<FormatType> comboFormatType;

	// -- 撮影後設定 --
	/** 撮影後設定を選択するラジオボタンをグループ化するトグルボタン */
	@FXML
	private final ToggleGroup radioSnapAfterGroup = new ToggleGroup();
	/** 撮影後設定を選択するラジオボタンの一つ「何もしない」 */
	@FXML
	private RadioButton radioSnapAfterNothing;
	/** 撮影後設定を選択するラジオボタンの一つ「画像を開く」 */
	@FXML
	private RadioButton radioSnapAfterOpenImage;
	/** 撮影後設定を選択するラジオボタンの一つ「ペイントで開く」 */
	@FXML
	private RadioButton radioSnapAfterOpenPaint;
	/** snapAfterとラジオボタンを紐付けるマップ */
	private Map<SnapAfter, RadioButton> snapAfterRadioMap;

	// -- 通知設定 --
	/** 通知表示設定のチェックボックス */
	@FXML
	private CheckBox checkNotification;

	/** 通知音設定のチェックボックス */
	@FXML
	private CheckBox checkSound;

	/**
	 * 初期化処理。FXMLファイルの要素を取り込んだ後に呼び出す
	 */
	@FXML
	public void initialize() {

		ConfigBean config = coordinator.getConfigBean(); // 設定値保持クラス
		this.mm = new MessageManager(config.getLanguageType()); // メッセージ変換クラス

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

		// ～～ ユーザー設定値のセット ～～ 
		// 言語設定のリスト、コンバーター(変換ルール)、初期設定値をセット
		comboLanguageType.getItems().addAll(LanguageType.values());
		comboLanguageType.setConverter(LanguageType.toConverter());
		comboLanguageType.setValue(config.getLanguageType());
		registerLanguageChangeListener(); // 言語切り替え時の処理を設定

		// -- 撮影設定 --

		// ショートカットキーのリスト、コンバーター(変換ルール)、初期設定値をセット
		comboTriggerType.getItems().addAll(TriggerType.values());
		comboTriggerType.setConverter(TriggerType.toConverter());
		comboTriggerType.setValue(config.getTriggerType());
		// カスタムキーの取得
		KeyCombinationBean customKeys = config.getCustomKeyBean();
		// トリガーキーの保持、名前変換
		nativeCode = customKeys.getTriggerKey(); // nativeキーコードを保持
		if (!(nativeCode == VC_UNDEFINED)) {
			// 未定義でない場合、テキストフィールドにキーの名前をセットする
			textInputKey.setText(NativeKeyEvent.getKeyText(nativeCode));
		}
		// 修飾キーの初期値を設定
		checkCtrl.setSelected(customKeys.isUseCtrl());
		checkAlt.setSelected(customKeys.isUseAlt());
		checkShift.setSelected(customKeys.isUseShift());
		// 撮影範囲のリスト、コンバーター(変換ルール)、初期設定値をセット
		comboSnapArea.getItems().addAll(SnapArea.values());
		comboSnapArea.setConverter(SnapArea.toConverter()); // コンバーターにより	Enumsをdisplaynameで変換が可能になる
		comboSnapArea.setValue(config.getSnapArea());

		// -- 出力設定 --

		// 画像保存の有無を設定するチェックボックスをセット
		checkSaveImage.setSelected(config.getIsSaveImage());
		// クリップボードに画像をするかのチェックボックスをセット
		checkClipboard.setSelected(config.getIsCopyToClipboard());
		// 保存先のフォルダを取得し、テキストフィールドにセット
		config.getSavePath();
		textFilePath.setText(config.getSavePath());
		// 拡張子のリスト、コンバーター(変換ルール)、初期設定値をセット
		comboFormatType.getItems().addAll(FormatType.values());
		comboFormatType.setConverter(FormatType.toConverter());
		comboFormatType.setValue(config.getFormatType());

		// -- 撮影後設定 --

		// 撮影後設定のラジオボタンの初期設定
		snapAfterRadioMap = createSnapAfterRadioMap(); // 紐付けマップ作成
		setupAfterRadioButtons(snapAfterRadioMap); // ラジオボタンの初期設定
		radioSnapAfterGroup.selectToggle(snapAfterRadioMap.get(config.getSnapAfter())); // 初期選択
		// 通知表示設定のチェックボックスを設定する
		checkNotification.setSelected(config.getIsShowNotification());
		// 撮影音のチェックボックスを設定する
		checkSound.setSelected(config.getIsSoundEnabled());

		// -- 適用設定表示ラベルの反映 --
		updateSettingLabels(config, customKeys);
	}

	/**
	 * 言語設定が変更されたときのリスナーを登録する
	 * 言語設定のみプロパティファイルを更新し、uiの表示言語切り替える(未実装)
	 */
	private void registerLanguageChangeListener() {
		comboLanguageType.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal == null || newVal == oldVal) {
				return;
			}

			ConfigBean config = coordinator.getConfigBean();
			config.setLanguageType(newVal); // 最新の言語設定をセット
			coordinator.save(); // 内部では全項目更新
			
			mm.changeLanguage(config.getLanguageType()); // リソースバンドルを読み直す
			updateSettingLabels(config, config.getCustomKeyBean()); // ラベル更新

		});
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
	 * カスタムキーの入力処理を行う、リスナーを登録する
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
		checkCtrl.setDisable(!(isCustom && hasKey));
		checkAlt.setDisable(!(isCustom && hasKey));
		checkShift.setDisable(!(isCustom && hasKey));
	}

	/**
	 * SnapAfterと各種ラジオボタンを紐付けたマップを作成する
	 * @return SnapAfterと各種ラジオボタンを紐付けたマップ
	 */
	private Map<SnapAfter, RadioButton> createSnapAfterRadioMap() {
		// 
		Map<SnapAfter, RadioButton> snapAfterRadioMap = new HashMap<SnapAfter, RadioButton>();
		snapAfterRadioMap.put(SnapAfter.NOTHING, radioSnapAfterNothing);
		snapAfterRadioMap.put(SnapAfter.OPEN_IMAGE, radioSnapAfterOpenImage);
		snapAfterRadioMap.put(SnapAfter.OPEN_PAINT, radioSnapAfterOpenPaint);
		return snapAfterRadioMap;
	}

	/**
	 * 撮影後設定のラジオボタンに、ラジオボタンのグループ化、
	 * SnapAfterのEnumのセット、表示名の設定を行う
	 * @param snapAfterRadioMap SnapAfterと各種ラジオボタンを紐付けたマップ
	 */
	private void setupAfterRadioButtons(Map<SnapAfter, RadioButton> snapAfterRadioMap) {

		// SnapAfterとRadioButtonの組み合わせを一つずつ取得し、ラジオボタンに各種設定をしていく
		for (Map.Entry<SnapAfter, RadioButton> entry : snapAfterRadioMap.entrySet()) {
			SnapAfter snapAfter = entry.getKey();
			RadioButton radioButton = entry.getValue();

			radioButton.setToggleGroup(radioSnapAfterGroup);
			radioButton.setUserData(snapAfter);
			radioButton.setText(snapAfter.getDisplayName());
		}
	}

	/**
	 * 「設定を反映」ボタンが押されたとき、ユーザーの設定値を保存する	
	 */
	@FXML
	public void onApplyConfig() {

		ConfigBean config = coordinator.getConfigBean();
		config.setLanguageType(comboLanguageType.getValue()); // 言語設定

		// -- 撮影設定 --
		config.setTriggerType(comboTriggerType.getValue()); // トリガーキー
		config.setSnapArea(comboSnapArea.getValue()); // 撮影範囲
		// カスタムキー
		KeyCombinationBean customKeys = config.getCustomKeyBean();
		customKeys.setTriggerKey(nativeCode); // 保持しているキーコードをいれる
		customKeys.setUseCtrl(checkCtrl.isSelected());
		customKeys.setUseAlt(checkAlt.isSelected());
		customKeys.setUseShift(checkShift.isSelected());

		// -- 出力設定 --
		config.setIsSaveImage(checkSaveImage.isSelected()); // 画像保存の有無
		config.setIsCopyToClipboard(checkClipboard.isSelected()); // クリップボード保存の有無
		config.setSavePath(textFilePath.getText()); // 保存パス
		config.setFormatType(comboFormatType.getValue()); // 拡張子

		// -- 撮影後設定 --
		config.setSnapAfter((SnapAfter) radioSnapAfterGroup.getSelectedToggle().getUserData()); // 撮影後設定
		config.setIsShowNotification(checkNotification.isSelected()); // 通知表示の有無
		config.setIsSoundEnabled(checkSound.isSelected()); // 撮影音を鳴らす有無

		// 文字列を更新する
		updateSettingLabels(config, customKeys);

		// configを更新し、司令塔クラスに返す
		coordinator.save();
		labelStatus.setText("ステータス: 設定を反映しました。");
	}

	/**
	 * 「設定を初期化」ボタンが押されたときの処理
	 */
	@FXML
	public void onResetConfig() {
		// -- 撮影設定 --
		comboTriggerType.getSelectionModel().selectFirst();
		comboSnapArea.getSelectionModel().selectFirst();
		// カスタムキーの設定をリセットする
		textInputKey.clear();
		checkCtrl.setSelected(false);
		checkAlt.setSelected(false);
		checkShift.setSelected(false);

		// -- 出力設定 --
		checkSaveImage.setSelected(true); // チェックを入れる
		checkClipboard.setSelected(true); // チェックを入れる
		textFilePath.setText(DEFAULT_SAVE_DIR); // フォルダパスをデフォルトに設定
		comboFormatType.getSelectionModel().selectFirst();

		// -- 撮影後設定 --
		radioSnapAfterGroup.selectToggle(radioSnapAfterNothing);
		checkNotification.setSelected(false);
		checkSound.setSelected(false);

		labelStatus.setText("ステータス: 入力値を初期化しました");
	}

	/**
	 * 設定更新時、現在の設定値を表示するラベルを一斉に更新する
	 * @param config ユーザー設定値
	 * @param customKeys ユーザーのカスタムキーの設定値
	 */
	public void updateSettingLabels(ConfigBean config, KeyCombinationBean customKeys) {
		
		// -- 撮影設定
		labelKeyCombination
				.setText(config.getTriggerType().resolveKeyCombination(customKeys).resolveKeyCombinationName());
		labelSnapArea.setText(config.getSnapArea().getDisplayName());
		
		// -- 出力設定
		labelSaveImage.setText(mm.getEnabledMessage(config.getIsSaveImage()));
		labelClipboard.setText(mm.getEnabledMessage(config.getIsCopyToClipboard()));
		labelFilePath.setText(config.getSavePath());
		labelFormatType.setText(config.getFormatType().getDisplayName());

		// -- 撮影後設定
		labelSnapAfter.setText(config.getSnapAfter().getDisplayName());
		labelNotification.setText(mm.getEnabledMessage(config.getIsShowNotification()));
		labelSound.setText(mm.getEnabledMessage(config.getIsSoundEnabled()));
	}

	/**
	 * サービスクラスの司令塔クラスを取得する
	 * @return coordinator
	 */
	public InstantSnapCoordinator getCoordinator() {
		return coordinator;
	}

	/**
	 * サービスクラスの司令塔クラスをセットする
	 * @param coordinator セットする coordinator
	 */
	public void setCoordinator(InstantSnapCoordinator coordinator) {
		this.coordinator = coordinator;
	}
}