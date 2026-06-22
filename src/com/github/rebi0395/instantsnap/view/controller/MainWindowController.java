package com.github.rebi0395.instantsnap.view.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.github.rebi0395.instantsnap.app.InstantSnapCoordinator;
import com.github.rebi0395.instantsnap.common.AppLogger;
import com.github.rebi0395.instantsnap.common.MessageKeyConstant;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.common.NativeKeyCodeConverter;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.KeyCombinationBean;
import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.model.enums.AfterSnapActionType;
import com.github.rebi0395.instantsnap.model.enums.SnapAreaType;
import com.github.rebi0395.instantsnap.model.enums.TriggerType;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;
import static com.github.rebi0395.instantsnap.common.StrCheckUtil.*;
import static com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;

/**
 * メイン画面のコントローラクラス
 */
public class MainWindowController {

	/** サービスクラスの司令塔クラス */
	private final InstantSnapCoordinator coordinator;

	/** メッセージの多言語変換クラス */
	private MessageKeyConverter converter;

	/** nativeキーコードを保持する変数 */
	private int nativeCode;

	// -- タブ --
	/** 基本設定タブ */
	@FXML
	private Tab tabBasicSettings;

	/** その他設定タブ */
	@FXML
	private Tab tabOtherSettings;

	// -- 折りたたみ項目 --
	/** 画像保存先設定の折りたたみ項目 */
	@FXML
	private TitledPane accordionSaveDestination;

	// -- ボタン --
	/** 入力値初期化ボタン */
	@FXML
	private Button buttonReset;

	/** 設定反映ボタン */
	@FXML
	private Button buttonApply;

	// -- セクションラベル --
	/** ショートカットキー選択セクションのラベル */
	@FXML
	private Label sectionShortcutKey;

	/** カスタムキー設定セクションのラベル */
	@FXML
	private Label sectionCustomKey;

	/** 撮影範囲セクションのラベル */
	@FXML
	private Label sectionSnapArea;

	/** 出力セクションのラベル */
	@FXML
	private Label captionOutput;

	/** 保存方法セクションのラベル */
	@FXML
	private Label sectionSaveMethod;

	/** 保存先選択セクションのラベル */
	@FXML
	private Label sectionSelectSaveDirectory;

	/** 画像形式セクションのラベル */
	@FXML
	private Label sectionFormat;

	/** 撮影後の動作セクションのラベル */
	@FXML
	private Label sectionAfterSnapAction;

	/** 通知表示セクションのラベル */
	@FXML
	private Label sectionNotification;

	/** サウンド設定セクションのラベル */
	@FXML
	private Label sectionSoundTitle;

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

	/** 通知音を鳴らす設定の有効/無効を表示するラベル */
	@FXML
	private Label labelNotificationSound;

	/** 撮影音を鳴らす設定の有効/無効を表示するラベル */
	@FXML
	private Label labelSnapSound;

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
	private ComboBox<SnapAreaType> comboSnapArea;

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

	/** フォルダ選択ボタン */
	@FXML
	private Button buttonSelectFile;

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

	/** 撮影後設定を選択するラジオボタンの一つ「一時保存フォルダに保存」 */
	@FXML
	private RadioButton radioSnapAfterSaveToTmp;

	/** snapAfterとラジオボタンを紐付けるマップ */
	private Map<AfterSnapActionType, RadioButton> snapAfterRadioMap;

	// -- 通知設定 --
	/** 通知表示設定のチェックボックス */
	@FXML
	private CheckBox checkNotification;

	/** 通知音設定のチェックボックス */
	@FXML
	private CheckBox checkNotificationSound;

	/** 撮影音設定のチェックボックス */
	@FXML
	private CheckBox checkSnapSound;

	/** コンストラクタ */
	public MainWindowController(InstantSnapCoordinator coordinator) {
		this.coordinator = coordinator;
	}

	/**
	 * 初期化処理。FXMLファイルの要素を取り込んだ後に呼び出す
	 */
	@FXML
	public void initialize() {
		this.converter = coordinator.getMessageKeyConverter();
		ConfigBean config = coordinator.getConfigBean(); // 設定値保持クラス

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
			textInputKey.setText(NativeKeyCodeConverter.toFXKeyCode(nativeCode).getName());
		}
		// 修飾キーの初期値を設定
		checkCtrl.setSelected(customKeys.isUseCtrl());
		checkAlt.setSelected(customKeys.isUseAlt());
		checkShift.setSelected(customKeys.isUseShift());
		// 撮影範囲のリスト、コンバーター(変換ルール)、初期設定値をセット
		comboSnapArea.getItems().addAll(SnapAreaType.values());
		comboSnapArea.setConverter(SnapAreaType.toConverter(converter)); // コンバーターにより	Enumsをdisplaynameで変換が可能になる
		comboSnapArea.setValue(config.getSnapAreaType());

		// -- 出力設定 --

		// 画像保存の有無を設定するチェックボックスをセット
		checkSaveImage.setSelected(config.getIsSaveImage());
		// クリップボードに画像をするかのチェックボックスをセット
		checkClipboard.setSelected(config.getIsCopyToClipboard());
		// 保存先のフォルダを取得し、テキストフィールドにセット
		textFilePath.setText(config.getSavePath());
		// 拡張子のリスト、コンバーター(変換ルール)、初期設定値をセット
		comboFormatType.getItems().addAll(FormatType.values());
		comboFormatType.setConverter(FormatType.toConverter());
		comboFormatType.setValue(config.getFormatType());

		// -- 撮影後設定 --

		// 撮影後設定のラジオボタンの初期設定
		snapAfterRadioMap = createAfterSnapActionRadioMap(); // 紐付けマップ作成
		setupAfterRadioButtons(snapAfterRadioMap); // ラジオボタンの初期設定
		radioSnapAfterGroup.selectToggle(snapAfterRadioMap.get(config.getAfterSnapActionType())); // 初期選択
		// 通知表示設定のチェックボックスを設定する
		checkNotification.setSelected(config.getIsShowNotification());
		// 通知音のチェックボックスを設定する
		checkNotificationSound.setSelected(config.getIsNotificationSoundEnabled());
		// 撮影音のチェックボックスを設定する
		checkSnapSound.setSelected(config.getIsSnapSoundEnabled());

		// -- 適用設定表示ラベルの反映 --
		updateSettingLabels(config, customKeys);
		// 固定文言の反映
		updateFixedTexts();
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

			converter.changeLanguage(config.getLanguageType()); // リソースバンドルを読み直す
			updateLanguageTexts(config); // 言語変更を反映させる

		});
	}

	/**
	 * 保存先フォルダ選択ボタン押下時、ディレクトリフォルダを選択させる
	 */
	@FXML
	public void onSelectFile() {
		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle("保存先フォルダを選択");
		File folder = dc.showDialog(textFilePath.getScene().getWindow());
		if (folder != null) {
			textFilePath.setText(folder.getAbsolutePath().replace("\\", "/")); // パス文字を置換する
		}
	}

	/**
	 * カスタムキーの入力処理を行う、リスナーを登録する
	 * @param textField テキストフィールド
	 */
	public void inputCustomKey(TextField textField) {
		// IMEによる入力文字の確定をキャンセルする
		textField.addEventFilter(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, event -> {
			event.consume();
		});

		// 文字入力イベントをキャンセルする
		textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
			// 通常のキー入力をキャンセルさせる
			event.consume();
		});

		// キーが押された瞬間に、キーコードとキーの表示名を表示する処理
		textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
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
			textField.setText(keyCode.getName()); // JavaFXのキーコード使用
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
	private Map<AfterSnapActionType, RadioButton> createAfterSnapActionRadioMap() {
		// 
		Map<AfterSnapActionType, RadioButton> afterSnapActionRadioMap = new HashMap<AfterSnapActionType, RadioButton>();
		afterSnapActionRadioMap.put(AfterSnapActionType.NONE, radioSnapAfterNothing);
		afterSnapActionRadioMap.put(AfterSnapActionType.OPEN_IMAGE, radioSnapAfterOpenImage);
		afterSnapActionRadioMap.put(AfterSnapActionType.OPEN_PAINT, radioSnapAfterOpenPaint);
		afterSnapActionRadioMap.put(AfterSnapActionType.SAVE_TO_TMP, radioSnapAfterSaveToTmp);
		return afterSnapActionRadioMap;
	}

	/**
	 * 撮影後設定のラジオボタンに、ラジオボタンのグループ化、
	 * SnapAfterのEnumのセット、表示名の設定を行う
	 * @param snapAfterRadioMap SnapAfterと各種ラジオボタンを紐付けたマップ
	 */
	private void setupAfterRadioButtons(Map<AfterSnapActionType, RadioButton> snapAfterRadioMap) {

		// SnapAfterとRadioButtonの組み合わせを一つずつ取得し、ラジオボタンに各種設定をしていく
		for (Map.Entry<AfterSnapActionType, RadioButton> entry : snapAfterRadioMap.entrySet()) {
			AfterSnapActionType afterSnapActionType = entry.getKey();
			RadioButton radioButton = entry.getValue();

			radioButton.setToggleGroup(radioSnapAfterGroup); // ラジオボタングループに登録
			radioButton.setUserData(afterSnapActionType); // Enumごと登録
			radioButton.setText(afterSnapActionType.getMessage(converter)); // メッセージキー変換した文言セット
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
		config.setSnapAreaType(comboSnapArea.getValue()); // 撮影範囲
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
		config.setAfterSnapActionType((AfterSnapActionType) radioSnapAfterGroup.getSelectedToggle().getUserData()); // 撮影後設定
		config.setIsShowNotification(checkNotification.isSelected()); // 通知表示の有無
		config.setIsNotificationSoundEnabled(checkNotificationSound.isSelected()); // 通知音を鳴らす有無
		config.setIsSnapSoundEnabled(checkSnapSound.isSelected()); // 撮影音を鳴らす有無

		// 文字列を更新する
		updateLanguageTexts(config);

		// 更新されたconfigを用いて、司令塔クラスにプロパティ更新を依頼する
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
		checkCtrl.setSelected(DEFAULT_USE_CTRL);
		checkAlt.setSelected(DEFAULT_USE_ALT);
		checkShift.setSelected(DEFAULT_USE_SHIFT);

		// -- 出力設定 --
		checkSaveImage.setSelected(DEFAULT_SAVE_IMAGE_ENABLED); // チェックを入れる
		checkClipboard.setSelected(DEFAULT_CLIPBOARD_ENABLED); // チェックを入れる
		textFilePath.setText(DEFAULT_SAVE_DIR); // フォルダパスをデフォルトに設定
		comboFormatType.getSelectionModel().selectFirst();

		// -- 撮影後設定 --
		radioSnapAfterGroup.selectToggle(radioSnapAfterNothing);
		checkNotification.setSelected(DEFAULT_NOTIFICATION_ENABLED);
		checkNotificationSound.setSelected(DEFAULT_SOUND_SNAP_ENABLED);
		checkSnapSound.setSelected(DEFAULT_SOUND_NOTIFICATION_ENABLED);

		labelStatus.setText("ステータス: 入力値を初期化しました");
	}

	/**
	 * 言語切り替え後、画面表示を更新する
	 * 
	 * @param config ユーザー設定値
	 */
	private void updateLanguageTexts(ConfigBean config) {

		// ComboBoxの表示を更新
		updateComboBoxConverters();

		// 撮影後設定のラジオボタン表示を更新
		updateAfterSnapActionRadioTexts();

		// 適用中ラベルを更新
		updateSettingLabels(config, config.getCustomKeyBean());

		// 固定文言を更新
		updateFixedTexts();
	}

	/**
	 * ComboBoxのコンバーターを再設定し、表示を更新する
	 * 
	 * @param config ユーザー設定値
	 */
	private void updateComboBoxConverters() {

		// 今の選択状態を退避
		SnapAreaType snapAreaType = comboSnapArea.getValue();

		// 選択値を入れ直して、ComboBoxの表示を再描画させる
		comboSnapArea.getItems().clear();
		comboSnapArea.getItems().addAll(SnapAreaType.values());
		comboSnapArea.setConverter(SnapAreaType.toConverter(converter)); // コンバーターにより	Enumsをdisplaynameで変換が可能になる
		comboSnapArea.setValue(snapAreaType);
	}

	/**
	 * 撮影後設定のラジオボタン表示を更新する
	 */
	private void updateAfterSnapActionRadioTexts() {

		for (Map.Entry<AfterSnapActionType, RadioButton> entry : snapAfterRadioMap.entrySet()) {
			AfterSnapActionType afterSnapActionType = entry.getKey();
			RadioButton radioButton = entry.getValue();

			radioButton.setText(afterSnapActionType.getMessage(converter));
		}
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
		labelSnapArea.setText(config.getSnapAreaType().getMessage(converter));

		// -- 出力設定
		labelSaveImage.setText(converter.getEnabledMessage(config.getIsSaveImage()));
		labelClipboard.setText(converter.getEnabledMessage(config.getIsCopyToClipboard()));
		labelFilePath.setText(config.getSavePath());
		labelFormatType.setText(config.getFormatType().getBaseDisplayName());

		// -- 撮影後設定
		labelSnapAfter.setText(config.getAfterSnapActionType().getMessage(converter));
		labelNotification.setText(converter.getEnabledMessage(config.getIsShowNotification()));
		labelNotificationSound.setText(converter.getEnabledMessage(config.getIsNotificationSoundEnabled()));
		labelSnapSound.setText(converter.getEnabledMessage(config.getIsSnapSoundEnabled()));
	}

	/**
	 * 固定表示文言を更新する
	 */
	private void updateFixedTexts() {
		// タブ
		tabBasicSettings.setText(converter.getMessage(MessageKeyConstant.TAB_BASIC_SETTINGS, "Basic settings"));
		tabOtherSettings.setText(converter.getMessage(MessageKeyConstant.TAB_OTHER_SETTINGS, "Other settings"));

		// キャプション
		captionOutput.setText(converter.getMessage(MessageKeyConstant.CAPTION_OUTPUT, "Output Settings"));

		// セクション
		sectionShortcutKey.setText(converter.getMessage(MessageKeyConstant.SECTION_SHORTCUT_KEY, "Shortcut key"));
		sectionCustomKey
				.setText(converter.getMessage(MessageKeyConstant.SECTION_CUSTOM_KEY, "Custom key settings"));
		sectionSnapArea.setText(converter.getMessage(MessageKeyConstant.SECTION_SNAP_AREA, "Capture area"));
		sectionSaveMethod.setText(converter.getMessage(MessageKeyConstant.SECTION_SAVE_METHOD, "Save options"));
		sectionSelectSaveDirectory.setText(converter.getMessage(MessageKeyConstant.SECTION_SELECT_SAVE_DIRECTORY, "Save destination folder"));
		sectionFormat.setText(converter.getMessage(MessageKeyConstant.SECTION_FORMAT, "Image format"));
		sectionAfterSnapAction
				.setText(converter.getMessage(MessageKeyConstant.SECTION_AFTER_SNAP_ACTION, "After capture"));
		sectionNotification.setText(converter.getMessage(MessageKeyConstant.SECTION_NOTIFICATION, "Notification"));
		sectionSoundTitle.setText(converter.getMessage(MessageKeyConstant.SECTION_SOUND, "Sound"));

		// チェックボックス
		checkSaveImage.setText(converter.getMessage(MessageKeyConstant.CHECK_SAVE_IMAGE, "Save image"));
		checkClipboard.setText(converter.getMessage(MessageKeyConstant.CHECK_COPY_TO_CLIPBOARD, "Copy to clipboard"));
		checkNotification
				.setText(converter.getMessage(MessageKeyConstant.CHECK_SHOW_NOTIFICATION, "Show notification"));
		checkSnapSound
				.setText(converter.getMessage(MessageKeyConstant.CHECK_ENABLE_SNAP_SOUND, "Enable shutter sound"));
		checkNotificationSound.setText(
				converter.getMessage(MessageKeyConstant.CHECK_ENABLE_NOTIFICATION_SOUND, "Enable notification sound"));

		// アコーディオン
		accordionSaveDestination.setText(
				converter.getMessage(MessageKeyConstant.ACCORDION_SAVE_DESTINATION, "Save destination settings"));

		// ボタン
		buttonReset.setText(converter.getMessage(MessageKeyConstant.BUTTON_RESET_TO_DEFAULTS, "Reset to defaults"));
		buttonApply.setText(converter.getMessage(MessageKeyConstant.BUTTON_APPLY_CONFIG, "Apply settings"));
		buttonSelectFile.setText(converter.getMessage(MessageKeyConstant.BUTTON_SELECT_FILE, "Select...."));

		// プロンプト(テキストフィールドの初期説明文)
		textInputKey.setPromptText(converter.getMessage(MessageKeyConstant.PROMPT_CUSTOM_KEY, "Press a shortcut key"));
	}

}