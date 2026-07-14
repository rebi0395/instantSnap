package com.github.rebi0395.instantsnap.view.controller;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntConsumer;

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
import com.github.rebi0395.instantsnap.model.enums.FileNameDateFormatType;
import com.github.rebi0395.instantsnap.model.enums.SnapAreaType;
import com.github.rebi0395.instantsnap.model.enums.StatusType;
import com.github.rebi0395.instantsnap.model.enums.ThemeType;
import com.github.rebi0395.instantsnap.service.FilePathService;
import com.github.rebi0395.instantsnap.view.StyleSheetUtil;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;
import static com.github.rebi0395.instantsnap.common.StrCheckUtil.*;
import static com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

/**
 * メイン画面のコントローラクラス
 */
public class MainWindowController {

	/** サービスクラスの司令塔クラス */
	private final InstantSnapCoordinator coordinator;

	/** メッセージの多言語変換クラス */
	private MessageKeyConverter converter;

	/** ファイル名生成、ディレクトリ解決を行うクラス */
	private FilePathService filePathService;

	/** 撮影キーのnativeキーコードを保持する変数 */
	private int snapNativeCode;

	/** 範囲選択キーのnativeキーコードを保持する変数 */
	private int selectionNativeCode;

	/** 画面ステータス */
	private StatusType status = StatusType.STARTING;

	/** 画面ステータスのリセット時計 */
	private PauseTransition statusResetTimer;

	/** ComboBoxの表示更新中かを判定するフラグ */
	private boolean updatingComboBoxConverters = false;

	/** 画面全体の親レイアウト */
	@FXML
	private BorderPane rootPane;

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
	/** テーマ設定の項目名を表示するセクション */
	@FXML
	private Label sectionTheme;

	/** ショートカットキー選択セクションのラベル */
	@FXML
	private Label sectionSnapShortcutKey;

	/** 範囲選択ショートカットキーセクションのラベル */
	@FXML
	private Label sectionSelectionShortcutKey;

	/** 撮影範囲セクションのラベル */
	@FXML
	private Label sectionSnapArea;

	/** 撮影オプションのラベル */
	@FXML
	private Label sectionSnapOption;

	/** 保存方法セクションのラベル */
	@FXML
	private Label sectionSaveMethod;

	/** 保存先選択セクションのラベル */
	@FXML
	private Label sectionSelectSaveDirectory;

	/** ファイル名セクションのラベル */
	@FXML
	private Label sectionFileName;

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
	private Label labelSnapKeyCombination;

	/** 適用中の範囲選択キー設定を表示するラベル */
	@FXML
	private Label labelSelectionKeyCombination;

	/** 適応中の保存先ファイルパスを表示するラベル */
	@FXML
	private Label labelFilePath;

	/** 適応中の保存ファイル名を表示するラベル */
	@FXML
	private Label labelFileName;

	/** 適応中の撮影範囲を表示するラベル */
	@FXML
	private Label labelSnapArea;

	/** 適応中の撮影後設定を表示するラベル */
	@FXML
	private Label labelSnapAfter;

	/** マウスカーソルを画像に含める設定の有効/無効を表示するラベル */
	@FXML
	private Label labelIncludeCursor;

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

	// -- テーマ設定 --
	/** 画面テーマを選択するコンボボックス */
	@FXML
	private ComboBox<ThemeType> comboThemeType;

	// -- 撮影設定 --
	/** 撮影範囲を選択するコンボボックス */
	@FXML
	private ComboBox<SnapAreaType> comboSnapArea;

	/** 撮影キーの入力を受け付けるテキストフィールド */
	@FXML
	private TextField textSnapInputKey;
	/** 撮影キーのコンビネーションキーとしてAltを使用するかを選択するチェックボックス */
	@FXML
	private CheckBox checkSnapAlt;
	/** 撮影キーのコンビネーションキーとしてCtrlを使用するかを選択するチェックボックス */
	@FXML
	private CheckBox checkSnapCtrl;
	/** 撮影キーのコンビネーションキーとしてShiftを使用するかを選択するチェックボックス */
	@FXML
	private CheckBox checkSnapShift;
	/** 範囲選択キーの入力を受け付けるテキストフィールド */
	@FXML
	private TextField textSelectionInputKey;
	/** 範囲選択キーでAltを使用するかを選択するチェックボックス */
	@FXML
	private CheckBox checkSelectionAlt;
	/** 範囲選択キーでCtrlを使用するかを選択するチェックボックス */
	@FXML
	private CheckBox checkSelectionCtrl;
	/** 範囲選択キーでShiftを使用するかを選択するチェックボックス */
	@FXML
	private CheckBox checkSelectionShift;
	/** マウスカーソルを画像に含めるかを設定するチェックボックス */
	@FXML
	private CheckBox checkIncludeCursor;

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

	/** ファイルの先頭文字を指定するテキストフィールド */
	@FXML
	private TextField textFileNamePrefix;

	/** ファイル名の日時フォーマットを選択するコンボボックス */
	@FXML
	private ComboBox<FileNameDateFormatType> comboFileNameDateFormatType;

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

	/** 不正値用スタイルクラス */
	private static final String STYLE_INVALID_VALUE = "invalid-value";

	/** コンストラクタ */
	public MainWindowController(InstantSnapCoordinator coordinator) {
		this.coordinator = coordinator;
	}

	/**
	 * 初期化処理。FXMLファイルの要素を取り込んだ後に呼び出す
	 */
	@FXML
	public void initialize() {
		ConfigBean config = coordinator.getConfigBean(); // 設定値保持クラス
		this.converter = coordinator.getMessageKeyConverter(); // メッセージキー変換クラス
		this.filePathService = coordinator.getFilePathService(); // ファイル名生成、ディレクトリ解決を行うクラス

		// ショートカットキーのテキストフィールドの挙動を設定
		inputShortcutKey(textSnapInputKey, keyCode -> snapNativeCode = keyCode);
		inputShortcutKey(textSelectionInputKey, keyCode -> selectionNativeCode = keyCode);
		// ファイル名の先頭文字の置換処理
		registerTextFieldFocusOutListener();

		// ～～ ユーザー設定値のセット ～～
		// 言語設定のリスト、コンバーター(変換ルール)、初期設定値をセット
		comboLanguageType.getItems().addAll(LanguageType.values());
		comboLanguageType.setConverter(LanguageType.toConverter());
		comboLanguageType.setValue(config.getLanguageType());
		registerLanguageChangeListener(); // 言語切り替え時の処理を設定
		// テーマ設定のリスト、コンバーター(変換ルール)、初期設定値をセット
		comboThemeType.getItems().addAll(ThemeType.values());
		comboThemeType.setConverter(ThemeType.toConverter(converter));
		comboThemeType.setValue(config.getThemeType());
		registerThemeChangeListener(); // テーマ切り替え時の処理を設定

		// -- 撮影設定 --

		// 撮影キーの初期値を設定
		KeyCombinationBean snapKeys = config.getSnapKeyBean();
		snapNativeCode = snapKeys.getTriggerKey(); // nativeキーコードを保持
		if (!(snapNativeCode == VC_UNDEFINED)) {
			// 未定義でない場合、テキストフィールドにキーの名前をセットする
			textSnapInputKey.setText(NativeKeyCodeConverter.toFXKeyName(snapNativeCode));
		}
		// 修飾キーの初期値を設定
		checkSnapCtrl.setSelected(snapKeys.isUseCtrl());
		checkSnapAlt.setSelected(snapKeys.isUseAlt());
		checkSnapShift.setSelected(snapKeys.isUseShift());

		// 範囲選択キーの初期値を設定
		KeyCombinationBean selectionKeys = config.getSelectionKeyBean();
		selectionNativeCode = selectionKeys.getTriggerKey();
		if (!(selectionNativeCode == VC_UNDEFINED)) {
			// 未定義でない場合、テキストフィールドにキーの名前をセットする
			textSelectionInputKey.setText(NativeKeyCodeConverter.toFXKeyName(selectionNativeCode));
		}
		// 修飾キーの初期値を設定
		checkSelectionCtrl.setSelected(selectionKeys.isUseCtrl());
		checkSelectionAlt.setSelected(selectionKeys.isUseAlt());
		checkSelectionShift.setSelected(selectionKeys.isUseShift());
		// 撮影範囲のリスト、コンバーター(変換ルール)、初期設定値をセット
		comboSnapArea.getItems().addAll(SnapAreaType.values());
		comboSnapArea.setConverter(SnapAreaType.toConverter(converter)); // コンバーターにより	Enumsをdisplaynameで変換が可能になる
		comboSnapArea.setValue(config.getSnapAreaType());
		// マウスカーソルを画像に含めるかのチェックボックスをセット
		checkIncludeCursor.setSelected(config.getIsIncludeCursor());

		// -- 出力設定 --

		// 画像保存の有無を設定するチェックボックスをセット
		checkSaveImage.setSelected(config.getIsSaveImage());
		// クリップボードに画像をするかのチェックボックスをセット
		checkClipboard.setSelected(config.getIsCopyToClipboard());
		// 保存先のフォルダを取得し、テキストフィールドの初期値にセット
		textFilePath.setText(config.getSavePath());
		// ファイル名の先頭文字を取得し、テキストフィールドの初期値にセット
		textFileNamePrefix.setText(config.getFileNamePrefix());
		// 日時フォーマットのリスト、コンバーター(変換ルール)、初期設定値をセット
		comboFileNameDateFormatType.getItems().addAll(FileNameDateFormatType.values());
		comboFileNameDateFormatType.setConverter(FileNameDateFormatType.toConverter());
		comboFileNameDateFormatType.setValue(config.getFileNameDateFormatType());
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
		updateSettingLabels(config);
		// 固定文言の反映
		updateFixedTexts();
		// 不正値項目のスタイル反映
		updateInvalidStyle(config);
		// ステータス値を撮影可能にする
		updateStatus(StatusType.READY);

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
	 * テーマ設定が変更されたときのリスナーを登録する
	 * テーマ設定は見た目に直接関わるため、設定反映ボタンを待たずに
	 * プロパティファイルの更新とCSSの切り替えを即時に行う。
	 */
	private void registerThemeChangeListener() {
		comboThemeType.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (updatingComboBoxConverters || newVal == null || newVal == oldVal) {
				return;
			}

			ConfigBean config = coordinator.getConfigBean();
			config.setThemeType(newVal); // 最新のテーマ設定をセット
			coordinator.save(); // 内部では全項目更新

			StyleSheetUtil.changeTheme(rootPane, config.getThemeType()); // テーマCSSを即時反映
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
			// パス文字を置換してテキストフィールドにセットする
			textFilePath.setText(folder.getAbsolutePath().replace("\\", "/"));
			// パスをチェックし、スタイルを変化させる
			boolean invalid = filePathService.isInvalidSaveDirectoryPath(textFilePath.getText());
			applyInvalidStyle(textFilePath, invalid);
		}
	}

	/**
	 * ショートカットキーの入力処理を行うリスナーを登録する
	 * @param textField テキストフィールド
	 * @param nativeCodeSetter 入力されたnativeキーコードの保持処理
	 */
	private void inputShortcutKey(TextField textField, IntConsumer nativeCodeSetter) {
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
			int nativeCode = NativeKeyCodeConverter.toNativeKeyCode(keyCode);
			if (nativeCode == VC_UNDEFINED) {
				return; // マップにないキーは入力処理、イベント消費をしない
			}
			nativeCodeSetter.accept(nativeCode); // 対象のショートカットキーとして保持
			// 通常のキー押下イベントを消費させる
			event.consume();
			// テキストフィールドに表示する処理
			textField.setText(NativeKeyCodeConverter.toFXKeyName(nativeCode)); // JavaFXのキーコード使用
			textField.end(); // カーソルを右に移動
		});
	}

	/**
	 * テキストフィールドがフォーカスアウトされたときの処理を登録する。
	 */
	private void registerTextFieldFocusOutListener() {
		// ファイル名に使用できない文字を置換処理する
		// newValがfalseに変わるタイミングがフォーカスアウト
		textFileNamePrefix.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal) {
				return;
			}
			String inputText = textFileNamePrefix.getText();
			String sanitizedText = sanitizeFileNamePrefix(inputText);
			if (sanitizedText.equals(inputText)) {
				// 置換処理していない場合はリターン
				return;
			}
			textFileNamePrefix.setText(sanitizedText);
		});

		// 入力時、不正なパスであれば入力欄を赤くする
		textFilePath.focusedProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal) {
				return;
			}
			String inputText = textFilePath.getText();
			if (isBlankSafe(inputText)) {
				// 空文字の場合は、デフォルトパスを注入する
				textFilePath.setText(DEFAULT_SAVE_DIR);
				applyInvalidStyle(textFilePath, false);
				return;
			}

			boolean invalid = filePathService.isInvalidSaveDirectoryPath(inputText);
			applyInvalidStyle(textFilePath, invalid);
		});
	}

	/**
	 * SnapAfterと各種ラジオボタンを紐付けたマップを作成する
	 * @return SnapAfterと各種ラジオボタンを紐付けたマップ
	 */
	private Map<AfterSnapActionType, RadioButton> createAfterSnapActionRadioMap() {
		Map<AfterSnapActionType, RadioButton> afterSnapActionRadioMap = new LinkedHashMap<AfterSnapActionType, RadioButton>();
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

		// 画面ステータスを保存中に変更
		updateStatus(StatusType.SAVING);

		// 入力部品からフォーカスを外す
		rootPane.requestFocus();

		ConfigBean config = coordinator.getConfigBean();
		config.setLanguageType(comboLanguageType.getValue()); // 言語設定
		config.setThemeType(comboThemeType.getValue()); // テーマ設定

		// -- 撮影設定 --
		config.setSnapAreaType(comboSnapArea.getValue()); // 撮影範囲
		// 撮影キー
		KeyCombinationBean snapKeys = config.getSnapKeyBean();
		snapKeys.setTriggerKey(snapNativeCode); // 保持しているキーコードをいれる
		snapKeys.setUseCtrl(checkSnapCtrl.isSelected());
		snapKeys.setUseAlt(checkSnapAlt.isSelected());
		snapKeys.setUseShift(checkSnapShift.isSelected());
		// 範囲選択キー
		KeyCombinationBean selectionKeys = config.getSelectionKeyBean();
		selectionKeys.setTriggerKey(selectionNativeCode);
		selectionKeys.setUseCtrl(checkSelectionCtrl.isSelected());
		selectionKeys.setUseAlt(checkSelectionAlt.isSelected());
		selectionKeys.setUseShift(checkSelectionShift.isSelected());
		config.setIsIncludeCursor(checkIncludeCursor.isSelected()); // マウスカーソルの有無

		// -- 出力設定 --
		config.setIsSaveImage(checkSaveImage.isSelected()); // 画像保存の有無
		config.setIsCopyToClipboard(checkClipboard.isSelected()); // クリップボード保存の有無
		config.setSavePath(textFilePath.getText()); // 保存パス
		config.setFileNamePrefix(textFileNamePrefix.getText()); // ファイル名の先頭文字
		config.setFileNameDateFormatType(comboFileNameDateFormatType.getValue()); // 日時フォーマット
		config.setFormatType(comboFormatType.getValue()); // 拡張子

		// -- 撮影後設定 --
		config.setAfterSnapActionType((AfterSnapActionType) radioSnapAfterGroup.getSelectedToggle().getUserData()); // 撮影後設定
		config.setIsShowNotification(checkNotification.isSelected()); // 通知表示の有無
		config.setIsNotificationSoundEnabled(checkNotificationSound.isSelected()); // 通知音を鳴らす有無
		config.setIsSnapSoundEnabled(checkSnapSound.isSelected()); // 撮影音を鳴らす有無

		// 文字列を更新する
		updateLanguageTexts(config);

		// 不正値の場合、スタイルを反映
		updateInvalidStyle(config);

		// 更新されたconfigを用いて、司令塔クラスにプロパティ更新を依頼する
		coordinator.save();

		// 画面ステータスを保存完了とする
		updateStatus(StatusType.SAVED);
	}

	/**
	 * 「設定を初期化」ボタンが押されたときの処理
	 */
	@FXML
	public void onResetConfig() {
		// -- 撮影設定 --
		comboSnapArea.getSelectionModel().selectFirst();
		checkIncludeCursor.setSelected(DEFAULT_INCLUDE_CURSOR_ENABLED); // マウスカーソルの有無
		// ショートカットキーの設定をリセットする
		KeyCombinationBean defaultSnapKeys = ConfigBean.createDefaultSnapKey();
		snapNativeCode = defaultSnapKeys.getTriggerKey();
		textSnapInputKey.setText(NativeKeyCodeConverter.toFXKeyName(snapNativeCode));
		checkSnapCtrl.setSelected(defaultSnapKeys.isUseCtrl());
		checkSnapAlt.setSelected(defaultSnapKeys.isUseAlt());
		checkSnapShift.setSelected(defaultSnapKeys.isUseShift());

		KeyCombinationBean defaultSelectionKeys = ConfigBean.createDefaultSelectionKey();
		selectionNativeCode = defaultSelectionKeys.getTriggerKey();
		textSelectionInputKey.setText(NativeKeyCodeConverter.toFXKeyName(selectionNativeCode));
		checkSelectionCtrl.setSelected(defaultSelectionKeys.isUseCtrl());
		checkSelectionAlt.setSelected(defaultSelectionKeys.isUseAlt());
		checkSelectionShift.setSelected(defaultSelectionKeys.isUseShift());

		// -- 出力設定 --
		checkSaveImage.setSelected(DEFAULT_SAVE_IMAGE_ENABLED); // チェックを入れる
		checkClipboard.setSelected(DEFAULT_CLIPBOARD_ENABLED); // チェックを入れる
		textFilePath.setText(DEFAULT_SAVE_DIR); // フォルダパスをデフォルトに設定
		textFileNamePrefix.setText(DEFAULT_FILE_NAME_PREFIX); // ファイル名の先頭文字をデフォルトに設定
		comboFileNameDateFormatType.getSelectionModel().selectFirst(); // 日時フォーマット
		comboFormatType.getSelectionModel().selectFirst(); // 拡張子

		// -- 撮影後設定 --
		radioSnapAfterGroup.selectToggle(radioSnapAfterNothing);
		checkNotification.setSelected(DEFAULT_NOTIFICATION_ENABLED);
		checkSnapSound.setSelected(DEFAULT_SOUND_SNAP_ENABLED);
		checkNotificationSound.setSelected(DEFAULT_SOUND_NOTIFICATION_ENABLED);

		// -- ステータス更新 --
		updateStatus(StatusType.RESET_INPUT);

		// -- 不正値のスタイル除去 --
		applyInvalidStyle(textFilePath, false);
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
		updateSettingLabels(config);
		// 固定文言を更新
		updateFixedTexts();
		// ステータス文言の言語切り替え(タイマーはリセットしない)
		labelStatus.setText(converter.getMessage(status));
	}

	/**
	 * ステータス値の更新と、ステータスメッセージを更新する。
	 * @param statusType
	 */
	private void updateStatus(StatusType statusType) {
		// ステータスタイマーを停止し、nullにする
		if (statusResetTimer != null) {
			statusResetTimer.stop();
			statusResetTimer = null;
		}
		// ステータス更新
		status = statusType;
		labelStatus.setText(converter.getMessage(status));
		// 自動切り替えをしない場合は、早期リターン
		if (status.getChangeSeconds() == 0) {
			return;
		}
		// タイマーを作成し、タイマー終了イベントでステータスを更新する
		statusResetTimer = new PauseTransition(Duration.seconds(status.getChangeSeconds()));
		statusResetTimer.setOnFinished(event -> {
			status = StatusType.READY;
			labelStatus.setText(converter.getMessage(status));
		});
		statusResetTimer.play();
	}

	/**
	 * ComboBoxのコンバーターを再設定し、表示を更新する
	 *
	 */
	private void updateComboBoxConverters() {
		updatingComboBoxConverters = true;
		try {
			// 今の選択状態を退避
			SnapAreaType snapAreaType = comboSnapArea.getValue();
			ThemeType themeType = comboThemeType.getValue();

			// 選択値を入れ直して、ComboBoxの表示を再描画させる
			comboSnapArea.getItems().clear();
			comboSnapArea.getItems().addAll(SnapAreaType.values());
			comboSnapArea.setConverter(SnapAreaType.toConverter(converter)); // コンバーターによりEnumsをdisplayNameで変換できるようにする
			comboSnapArea.setValue(snapAreaType);

			comboThemeType.getItems().clear();
			comboThemeType.getItems().addAll(ThemeType.values());
			comboThemeType.setConverter(ThemeType.toConverter(converter)); // コンバーターによりEnumsをdisplayNameで変換できるようにする
			comboThemeType.setValue(themeType);
		} finally {
			updatingComboBoxConverters = false;
		}
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
	 */
	public void updateSettingLabels(ConfigBean config) {

		// -- 撮影設定
		labelSnapKeyCombination.setText(config.getSnapKeyBean().resolveKeyCombinationName());
		labelSelectionKeyCombination.setText(config.getSelectionKeyBean().resolveKeyCombinationName());
		labelSnapArea.setText(config.getSnapAreaType().getMessage(converter));
		labelIncludeCursor.setText(converter.getEnabledMessage(config.getIsIncludeCursor()));

		// -- 出力設定
		labelSaveImage.setText(converter.getEnabledMessage(config.getIsSaveImage()));
		labelClipboard.setText(converter.getEnabledMessage(config.getIsCopyToClipboard()));

		// フォルダ名のラベル設定
		String savePath = filePathService.resolveSaveDirectoryPreview(config.getSavePath());
		labelFilePath.setText(compressMiddle(savePath, 36)); // 長い場合、真ん中を省略表記
		labelFilePath.setTooltip(new Tooltip(savePath)); // ツールチップで全体を表示可能

		// ファイル名のラベル設定
		String fileName = filePathService.createFileNamePreview(config);
		labelFileName.setText(compressMiddle(fileName, 36)); // 長い場合、真ん中を省略表記
		labelFileName.setTooltip(new Tooltip(fileName)); // ツールチップで全体を表示可能

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

		// セクション
		sectionTheme.setText(converter.getMessage(MessageKeyConstant.SECTION_THEME, "Theme"));
		sectionSnapShortcutKey.setText(
				converter.getMessage(MessageKeyConstant.SECTION_SNAP_SHORTCUT_KEY, "Capture shortcut key"));
		sectionSelectionShortcutKey.setText(
				converter.getMessage(
						MessageKeyConstant.SECTION_SELECTION_SHORTCUT_KEY,
						"Range selection shortcut key"));
		sectionSnapArea.setText(converter.getMessage(MessageKeyConstant.SECTION_SNAP_AREA, "Capture area"));
		sectionSnapOption.setText(converter.getMessage(MessageKeyConstant.SECTION_SNAP_OPTION, "Capture option"));
		sectionSaveMethod.setText(converter.getMessage(MessageKeyConstant.SECTION_SAVE_METHOD, "Save options"));
		sectionSelectSaveDirectory.setText(
				converter.getMessage(MessageKeyConstant.SECTION_SELECT_SAVE_DIRECTORY, "Save destination folder"));
		sectionFileName.setText(converter.getMessage(MessageKeyConstant.SECTION_FILE_NAME, "File name"));
		sectionAfterSnapAction
				.setText(converter.getMessage(MessageKeyConstant.SECTION_AFTER_SNAP_ACTION, "After capture"));
		sectionNotification.setText(converter.getMessage(MessageKeyConstant.SECTION_NOTIFICATION, "Notification"));
		sectionSoundTitle.setText(converter.getMessage(MessageKeyConstant.SECTION_SOUND, "Sound"));

		// チェックボックス
		checkIncludeCursor.setText(
				converter.getMessage(MessageKeyConstant.CHECK_INCLUDE_CURSOR, "Include cursor"));
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
		String shortcutKeyPrompt =
				converter.getMessage(MessageKeyConstant.PROMPT_SHORTCUT_KEY, "Press a shortcut key");
		textSnapInputKey.setPromptText(shortcutKeyPrompt);
		textSelectionInputKey.setPromptText(shortcutKeyPrompt);
	}

	/**
	 * ユーザー設定値が不正値であれば、スタイルを反映させる。
	 * 不正でない項目はスタイルを除去する
	 * @param config ユーザー設定値
	 */
	private void updateInvalidStyle(ConfigBean config) {
		applyInvalidStyle(textFilePath,
				filePathService.isInvalidSaveDirectoryPath(config.getSavePath()));
	}

	/**
	 * 不正値用のスタイルを反映する
	 *
	 * @param node 反映対象
	 * @param invalid 不正値の場合true
	 */
	private void applyInvalidStyle(Node node, boolean invalid) {
		node.getStyleClass().remove(STYLE_INVALID_VALUE);

		if (invalid) {
			node.getStyleClass().add(STYLE_INVALID_VALUE);
		}
	}
}
