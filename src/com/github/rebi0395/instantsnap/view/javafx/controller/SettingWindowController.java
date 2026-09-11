package com.github.rebi0395.instantsnap.view.javafx.controller;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

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
import com.github.rebi0395.instantsnap.service.SettingsService;
import com.github.rebi0395.instantsnap.view.javafx.util.StyleSheetUtil;

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
 * JavaFX設定画面のコントローラクラス
 */
public class SettingWindowController {

	/** 設定値の読込・保存を担当するクラス */
	private final SettingsService settingsService;

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

	/** プロパティ再読込による画面反映中かを判定するフラグ */
	private boolean applyingConfigValues = false;

	/** 画面全体の親レイアウト */
	@FXML
	private BorderPane rootPane;

	// -- タブ --
	/** 基本設定タブ */
	@FXML
	private Tab tabBasicSettings;
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
	
	/** InstantSnapのウィンドウを画像に含める設定の有効/無効を表示するラベル */
	@FXML
	private Label labelIncludeSettingWindow;

	/** 範囲確定後の自動撮影設定の有効/無効を表示するラベル */
	@FXML
	private Label labelAutoSnapAfterRangeSelection;

	/** 撮影後の範囲撮影モード自動終了設定の有効/無効を表示するラベル */
	@FXML
	private Label labelCloseRangeSelectionAfterSnap;

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
	/** 範囲確定後に自動撮影するかを選択するチェックボックス */
	@FXML
	private CheckBox checkAutoSnapAfterRangeSelection;
	/** 撮影後に範囲撮影モードを自動で終了するかを選択するチェックボックス */
	@FXML
	private CheckBox checkCloseRangeSelectionAfterSnap;
	/** マウスカーソルを画像に含めるかを設定するチェックボックス */
	@FXML
	private CheckBox checkIncludeCursor;
	/** InstantSnapのウィンドウを画像に含めるかを設定するチェックボックス */
	@FXML
	private CheckBox checkIncludeSettingWindow;
	
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

	/**
	 * 設定の読込・保存に使用するサービスを受け取ります。
	 * 入力部品の初期化はFXMLの読込後にinitializeで行います。
	 *
	 * @param settingsService この設定画面で共有する設定サービス
	 */
	public SettingWindowController(SettingsService settingsService) {
		this.settingsService = settingsService;
	}

	/**
	 * 初期化処理。FXMLファイルの要素を取り込んだ後に呼び出す
	 */
	@FXML
	public void initialize() {
		ConfigBean config = settingsService.getConfigBean(); // 設定値保持クラス
		this.converter = settingsService.getMessageKeyConverter(); // メッセージキー変換クラス
		this.filePathService = settingsService.getFilePathService(); // ファイル名生成、ディレクトリ解決を行うクラス

		// コンボボックスの初期設定
		setupComboBoxes();

		// ラジオボタンの初期設定
		snapAfterRadioMap = createAfterSnapActionRadioMap(); // 紐付けマップ作成
		setupAfterRadioButtons(snapAfterRadioMap); // ラジオボタンの初期設定

		// configBeanから初期値読み込み
		applyConfigValues(config);

		// リスナー登録
		setupListeners();

		// 適用設定表示ラベルの反映
		updateSettingLabels(config);

		// 固定文言の反映
		updateFixedTexts();

		// 不正値項目のスタイル反映
		updateInvalidStyle(config);

		// 不正パターンを防ぐ活性／非活性制御を実施
		updateSnapOutputControlState();

		// ステータス値を撮影可能にする
		updateStatus(StatusType.READY);
	}

	/**
	 * 再読込済みの設定値を、JavaFXスレッド上で入力部品と文言へ反映します。
	 * 画面内の未保存の入力値も、ファイルから読み込んだ値で置き換えます。
	 */
	public void reloadSettings() {
		ConfigBean config = settingsService.getConfigBean();
		applyConfigValues(config);
		updateLanguageTexts(config);
		StyleSheetUtil.changeTheme(rootPane, config.getThemeType());
		updateInvalidStyle(config);
	}

	/**
	 * 即時保存リスナーを抑止しながら、設定値を入力部品へ反映します。
	 *
	 * @param config 入力部品へ反映する設定値
	 */
	private void applyConfigValues(ConfigBean config) {
		// 言語・テーマの再読込で保存が発生し、変更通知が循環することを防ぐ。
		applyingConfigValues = true;
		try {
			setConfigValuesToControls(config);
		} finally {
			applyingConfigValues = false;
		}
	}

	/**
	 * 設定値を各入力部品へ設定します。リスナー抑止中に呼び出します。
	 *
	 * @param config 各部品へ表示する設定値
	 */
	private void setConfigValuesToControls(ConfigBean config) {
		comboLanguageType.setValue(config.getLanguageType()); // 言語設定の読み込み
		comboThemeType.setValue(config.getThemeType()); // テーマの読み込み

		// 撮影キーを設定
		KeyCombinationBean snapKeys = config.getSnapKeyBean();
		snapNativeCode = snapKeys.getTriggerKey(); // nativeキーコードを保持
		if (snapNativeCode != VC_UNDEFINED) {
			// 未定義でない場合、テキストフィールドにキーの名前をセットする
			textSnapInputKey.setText(NativeKeyCodeConverter.toFXKeyName(snapNativeCode));
		} else {
			textSnapInputKey.clear();
		}
		// 修飾キーを設定
		checkSnapCtrl.setSelected(snapKeys.isUseCtrl());
		checkSnapAlt.setSelected(snapKeys.isUseAlt());
		checkSnapShift.setSelected(snapKeys.isUseShift());

		// 範囲選択キーを設定
		KeyCombinationBean selectionKeys = config.getSelectionKeyBean();
		selectionNativeCode = selectionKeys.getTriggerKey();
		if (selectionNativeCode != VC_UNDEFINED) {
			// 未定義でない場合、テキストフィールドにキーの名前をセットする
			textSelectionInputKey.setText(NativeKeyCodeConverter.toFXKeyName(selectionNativeCode));
		} else {
			textSelectionInputKey.clear();
		}
		// 修飾キーを設定
		checkSelectionCtrl.setSelected(selectionKeys.isUseCtrl());
		checkSelectionAlt.setSelected(selectionKeys.isUseAlt());
		checkSelectionShift.setSelected(selectionKeys.isUseShift());
		checkAutoSnapAfterRangeSelection.setSelected(config.getIsAutoSnapAfterRangeSelectionEnabled());
		checkCloseRangeSelectionAfterSnap.setSelected(config.getIsCloseRangeSelectionAfterSnapEnabled());

		comboSnapArea.setValue(config.getSnapAreaType()); // 撮影範囲
		checkIncludeCursor.setSelected(config.getIsIncludeCursor()); // マウスカーソル設定
		checkIncludeSettingWindow.setSelected(config.getIsIncludeSettingWindow()); // 設定画面を画像に含めるか設定

		checkSaveImage.setSelected(config.getIsSaveImage()); // 画像保存の有無の値を設定
		checkClipboard.setSelected(config.getIsCopyToClipboard()); // クリップボードに画像をするかの値をセット
		textFilePath.setText(config.getSavePath()); // 保存先のフォルダ名をセット
		textFileNamePrefix.setText(config.getFileNamePrefix()); // ファイル名の先頭文字をセット
		comboFileNameDateFormatType.setValue(config.getFileNameDateFormatType()); // 日付フォーマット
		comboFormatType.setValue(config.getFormatType()); // 拡張子

		radioSnapAfterGroup.selectToggle(snapAfterRadioMap.get(config.getAfterSnapActionType())); // 撮影後動作
		checkNotification.setSelected(config.getIsShowNotification()); // 通知表示設定
		checkNotificationSound.setSelected(config.getIsNotificationSoundEnabled()); // 通知音
		checkSnapSound.setSelected(config.getIsSnapSoundEnabled()); // 撮影音
	}

	/**
	 * コンボボックスの選択肢と、表示変換コンバーターを設定する
	 */
	private void setupComboBoxes() {
		// 言語設定のリスト、コンバーター(変換ルール)、初期設定値をセット
		comboLanguageType.getItems().setAll(LanguageType.values());
		comboLanguageType.setConverter(LanguageType.toConverter());

		// テーマ設定のリスト、コンバーター(変換ルール)、初期設定値をセット
		comboThemeType.getItems().setAll(ThemeType.values());
		comboThemeType.setConverter(ThemeType.toConverter(converter));

		// 撮影範囲のリスト、コンバーター(変換ルール)、初期設定値をセット
		comboSnapArea.getItems().setAll(SnapAreaType.values());
		comboSnapArea.setConverter(SnapAreaType.toConverter(converter));

		// 日時フォーマットのリスト、コンバーター(変換ルール)、初期設定値をセット
		comboFileNameDateFormatType.getItems().setAll(FileNameDateFormatType.values());
		comboFileNameDateFormatType.setConverter(FileNameDateFormatType.toConverter());

		// 拡張子のリスト、コンバーター(変換ルール)、初期設定値をセット
		comboFormatType.getItems().setAll(FormatType.values());
		comboFormatType.setConverter(FormatType.toConverter());
	}

	/**
	 * 各入力項目の変更・操作リスナーを登録する
	 */
	private void setupListeners() {
		// 撮影トリガーキーのテキストフィールドの挙動を設定
		inputShortcutKey(textSnapInputKey);
		// 範囲トリガーキーのテキストフィールドの挙動を設定
		inputShortcutKey(textSelectionInputKey);
		// トリガーキーの入力状態に応じて修飾キーの活性、非活性を切り替える
		registerModifierKeyStateListener(textSnapInputKey, checkSnapCtrl, checkSnapAlt, checkSnapShift);
		registerModifierKeyStateListener(textSelectionInputKey, checkSelectionCtrl, checkSelectionAlt,
				checkSelectionShift);

		// テキストフィールドのフォーカスアウト処理
		registerTextFieldFocusOutListener();
		// 言語切り替え時の処理を設定
		registerLanguageChangeListener();
		// 画像保存off、クリップボードoff、撮影後何もしないの組み合わせを禁じる制御
		registerUpdateSnapOutputControlState();
		// テーマ切り替え時の処理を設定
		registerThemeChangeListener();
	}

	/**
	 * 言語設定が変更されたときのリスナーを登録する
	 * 言語設定のみプロパティファイルを更新し、uiの表示言語切り替える(未実装)
	 */
	private void registerLanguageChangeListener() {
		comboLanguageType.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (applyingConfigValues || newVal == null || newVal == oldVal) {
				return;
			}

			ConfigBean config = settingsService.getConfigBean();
			config.setLanguageType(newVal); // 最新の言語設定をセット
			settingsService.save(); // 内部では全項目更新

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
			if (applyingConfigValues || updatingComboBoxConverters || newVal == null || newVal == oldVal) {
				return;
			}

			ConfigBean config = settingsService.getConfigBean();
			config.setThemeType(newVal); // 最新のテーマ設定をセット
			settingsService.save(); // 内部では全項目更新

			StyleSheetUtil.changeTheme(rootPane, config.getThemeType()); // テーマCSSを即時反映
		});
	}

	/**
	 * 保存先フォルダ選択ボタン押下時、ディレクトリフォルダを選択させる
	 */
	@FXML
	public void onSelectFile() {
		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle(converter.getMessage(MessageKeyConstant.SECTION_SELECT_SAVE_DIRECTORY, "Save Location"));
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
	 */
	private void inputShortcutKey(TextField textField) {
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

			// Tabキーは入力対象にせず、標準のフォーカス移動へ任せる
			if (keyCode == KeyCode.TAB) {
				return;
			}
			// Nativeキーコードに変換し、フィールドで保持
			int nativeCode = NativeKeyCodeConverter.toNativeKeyCode(keyCode);

			// フィールドの値は先に更新する
			if (textField == textSnapInputKey) {
				snapNativeCode = nativeCode;
			} else if (textField == textSelectionInputKey) {
				selectionNativeCode = nativeCode;
			}

			if (nativeCode == VC_UNDEFINED) {
				textField.clear(); // マップにないキーはテキストフィールドをクリアする
				return; // マップにないキーは入力処理、イベント消費をしない
			}

			// 通常のキー押下イベントを消費させる
			event.consume();
			// テキストフィールドに表示する処理
			textField.setText(NativeKeyCodeConverter.toFXKeyName(nativeCode)); // JavaFXのキーコード使用
			textField.end(); // カーソルを右に移動
		});
	}

	/**
	 * トリガーキーの入力状態に応じて、修飾キーの活性、非活性を切り替える。
	 * トリガーキーは必須のため、未入力時は修飾キーを操作できないようにする。
	 *
	 * @param textField トリガーキーのテキストフィールド
	 * @param ctrlCheckBox Ctrlキーのチェックボックス
	 * @param altCheckBox Altキーのチェックボックス
	 * @param shiftCheckBox Shiftキーのチェックボックス
	 */
	private void registerModifierKeyStateListener(
			TextField textField,
			CheckBox ctrlCheckBox,
			CheckBox altCheckBox,
			CheckBox shiftCheckBox) {
		// 値が更新されたときに、修飾キーの活性・非活性を切り替える
		textField.textProperty().addListener((obs, oldValue, newValue) -> {
			updateModifierKeyState(textField, ctrlCheckBox, altCheckBox, shiftCheckBox);
		});
		updateModifierKeyState(textField, ctrlCheckBox, altCheckBox, shiftCheckBox); // 初期表示時
	}

	/**
	 * トリガーキーが入力されている場合のみ、修飾キーを活性にする。
	 *
	 * @param textField トリガーキーのテキストフィールド
	 * @param ctrlCheckBox Ctrlキーのチェックボックス
	 * @param altCheckBox Altキーのチェックボックス
	 * @param shiftCheckBox Shiftキーのチェックボックス
	 */
	private void updateModifierKeyState(
			TextField textField,
			CheckBox ctrlCheckBox,
			CheckBox altCheckBox,
			CheckBox shiftCheckBox) {
		boolean hasTriggerKey = isNotBlankSafe(textField.getText());
		ctrlCheckBox.setDisable(!hasTriggerKey);
		altCheckBox.setDisable(!hasTriggerKey);
		shiftCheckBox.setDisable(!hasTriggerKey);
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
	 * 画像保存、クリップボード保存、撮影後の動作「何もしない」が、
	 * すべて選択されないように完全に制御する処理を登録する
	 */
	private void registerUpdateSnapOutputControlState() {

		checkSaveImage.selectedProperty().addListener((obs, oldVal, newVal) -> {
			updateSnapOutputControlState();
		});

		checkClipboard.selectedProperty().addListener((obs, oldVal, newVal) -> {
			updateSnapOutputControlState();
		});

		radioSnapAfterNothing.selectedProperty().addListener((obs, oldVal, newVal) -> {
			updateSnapOutputControlState();
		});
	}

	/**
	 * 出力方法なし、保存先の組み合わせがUIから選択できないように、活性／非活性の制御を行う
	 */
	private void updateSnapOutputControlState() {
		boolean saveImage = checkSaveImage.isSelected();
		boolean copyToClipboard = checkClipboard.isSelected();
		boolean doNothing = radioSnapAfterNothing.isSelected();

		// 残っている唯一の出力方法は解除できないようにする。
		checkSaveImage.setDisable(!copyToClipboard && doNothing);
		checkClipboard.setDisable(!saveImage && doNothing);

		// 保存先が一つもなければ「何もしない」は選択できない。
		radioSnapAfterNothing.setDisable(!saveImage && !copyToClipboard);
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

		// 不正値スタイル除去
		clearAllInvalidStyle();
		// 画面ステータスを保存中に変更
		updateStatus(StatusType.SAVING);

		// 入力部品からフォーカスを外す
		rootPane.requestFocus();

		ConfigBean config = settingsService.getConfigBean();
		config.setLanguageType(comboLanguageType.getValue()); // 言語設定
		config.setThemeType(comboThemeType.getValue()); // テーマ設定

		// -- 撮影設定 --
		config.setSnapAreaType(comboSnapArea.getValue()); // 撮影範囲
		// 撮影キー
		KeyCombinationBean snapKeys = new KeyCombinationBean();
		snapKeys.setTriggerKey(snapNativeCode); // 保持しているキーコードをいれる
		snapKeys.setUseCtrl(checkSnapCtrl.isSelected());
		snapKeys.setUseAlt(checkSnapAlt.isSelected());
		snapKeys.setUseShift(checkSnapShift.isSelected());
		config.setSnapKeyBean(snapKeys);

		// 範囲選択キー
		KeyCombinationBean selectionKeys = new KeyCombinationBean();
		selectionKeys.setTriggerKey(selectionNativeCode);
		selectionKeys.setUseCtrl(checkSelectionCtrl.isSelected());
		selectionKeys.setUseAlt(checkSelectionAlt.isSelected());
		selectionKeys.setUseShift(checkSelectionShift.isSelected());

		if (selectionKeys.isSameKeyCombination(config.getSnapKeyBean())) {
			// 範囲キーが撮影キーと同じである場合、範囲キーを未定義にフォールバック
			selectionKeys = new KeyCombinationBean();
			textSelectionInputKey.clear();
			applyInvalidStyle(textSelectionInputKey, true); // 範囲キー項目を赤くする
		}
		config.setSelectionKeyBean(selectionKeys);
		config.setIsAutoSnapAfterRangeSelectionEnabled(checkAutoSnapAfterRangeSelection.isSelected());
		config.setIsCloseRangeSelectionAfterSnapEnabled(checkCloseRangeSelectionAfterSnap.isSelected());
		// マウスカーソルの有無
		config.setIsIncludeCursor(checkIncludeCursor.isSelected());
		// InstantSnapのウィンドウを含めるか
		config.setIsIncludeSettingWindow(checkIncludeSettingWindow.isSelected());

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

		// 設定サービスが保持する更新済みConfigBeanをプロパティファイルへ保存する。
		settingsService.save();

		// 設定値再読み込み(ショートカットキーのなどのフォールバック反映)
		applyConfigValues(config);

		// 画面ステータスを保存完了とする
		updateStatus(StatusType.SAVED);
	}

	/**
	 * 「設定を初期化」ボタンが押されたときの処理
	 */
	@FXML
	public void onResetConfig() {

		// 不正値スタイル除去
		clearAllInvalidStyle();

		// -- 撮影設定 --
		comboSnapArea.getSelectionModel().selectFirst();
		checkIncludeCursor.setSelected(DEFAULT_INCLUDE_CURSOR_ENABLED); // マウスカーソルの有無
		checkIncludeSettingWindow.setSelected(DEFAULT_INCLUDE_SETTING_WINDOW_ENABLED); // 設定ウィンドウを画像に含む
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
		checkAutoSnapAfterRangeSelection.setSelected(DEFAULT_AUTO_SNAP_AFTER_RANGE_SELECTION_ENABLED);
		checkCloseRangeSelectionAfterSnap.setSelected(DEFAULT_CLOSE_RANGE_SELECTION_AFTER_SNAP_ENABLED);

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
	 */
	private void updateComboBoxConverters() {
		updatingComboBoxConverters = true;
		try {
			// 今の選択状態を退避
			SnapAreaType snapAreaType = comboSnapArea.getValue();
			ThemeType themeType = comboThemeType.getValue();

			// 選択値を入れ直して、ComboBoxの表示を再描画させる
			comboSnapArea.getItems().setAll(SnapAreaType.values());
			comboSnapArea.setConverter(SnapAreaType.toConverter(converter)); // コンバーターによりEnumsをdisplayNameで変換できるようにする
			comboSnapArea.setValue(snapAreaType);

			comboThemeType.getItems().setAll(ThemeType.values());
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

		if (config.getSelectionKeyBean().isTriggerKeyDefined()) {
			labelSelectionKeyCombination.setText(config.getSelectionKeyBean().resolveKeyCombinationName());
		} else {
			// トリガーキーが未定義の場合、無効ラベルを表示する
			labelSelectionKeyCombination.setText(converter.getEnabledMessage(false));
		}

		labelSnapArea.setText(config.getSnapAreaType().getMessage(converter));
		labelIncludeCursor.setText(converter.getEnabledMessage(config.getIsIncludeCursor()));
		labelIncludeSettingWindow.setText(converter.getEnabledMessage(config.getIsIncludeSettingWindow()));
		labelAutoSnapAfterRangeSelection.setText(
				converter.getEnabledMessage(config.getIsAutoSnapAfterRangeSelectionEnabled()));
		labelCloseRangeSelectionAfterSnap.setText(
				converter.getEnabledMessage(config.getIsCloseRangeSelectionAfterSnapEnabled()));

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
		checkIncludeSettingWindow.setText(
				converter.getMessage(MessageKeyConstant.CHECK_INCLUDE_SETTING_WINDOW, "Include settings window"));
		checkAutoSnapAfterRangeSelection.setText(
				converter.getMessage(
						MessageKeyConstant.CHECK_AUTO_SNAP_AFTER_RANGE_SELECTION,
						"Auto-capture selected range"));
		checkCloseRangeSelectionAfterSnap.setText(
				converter.getMessage(
						MessageKeyConstant.CHECK_CLOSE_RANGE_SELECTION_AFTER_SNAP,
						"Exit range mode after capture"));
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
		String shortcutKeyPrompt = converter.getMessage(MessageKeyConstant.PROMPT_SHORTCUT_KEY, "Press a shortcut key");
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

	/**
	 * 不正値用のスタイルを完全に除去する
	 */
	private void clearAllInvalidStyle() {
		for (Node node : rootPane.lookupAll("." + STYLE_INVALID_VALUE)) {
			node.getStyleClass().removeAll(STYLE_INVALID_VALUE);
		}
	}
}
