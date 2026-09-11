package com.github.rebi0395.instantsnap.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.KeyCombinationBean;
import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.model.enums.AfterSnapActionType;
import com.github.rebi0395.instantsnap.model.enums.FileNameDateFormatType;
import com.github.rebi0395.instantsnap.model.enums.SnapAreaType;
import com.github.rebi0395.instantsnap.model.enums.ThemeType;

import static com.github.rebi0395.instantsnap.common.NativeKeyCodeConverter.*;
import static com.github.rebi0395.instantsnap.common.AppConstant.*;
import static com.github.rebi0395.instantsnap.common.StrCheckUtil.*;

/**
 * プロパティファイルの読み取り、書き込みを行い、
 * ユーザー設定の永続化を担保するクラスです
 */
public class PropertiesRepository {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(PropertiesRepository.class);

	/**
	 * プロパティファイルの読み取りを行います
	 * @param config 設定保持クラス
	 * @throws ServiceException 
	 */
	public void read(ConfigBean config) throws ServiceException {
		Properties properties = new Properties();

		checkAndResetProperties(); // プロパティファイルの実在チェック

		// UTF-8を明示的に指定し、プロパティをセット
		try (BufferedReader reader = Files.newBufferedReader(PROPERTIES_PATH, StandardCharsets.UTF_8)) {
			properties.load(reader);

			// -- プロパティから読み取った情報をセット -- 
			// 言語設定
			config.setLanguageType(LanguageType.parseOrDefault(
					properties.getProperty(PROPERTY_LANGUAGE)));

			// テーマ設定
			config.setThemeType(ThemeType.parseOrDefault(
					properties.getProperty(PROPERTY_THEME)));

			// 撮影設定
			config.setSnapAreaType(SnapAreaType.parseOrDefault(
					properties.getProperty(PROPERTY_SNAP_AREA)));
			config.setIsIncludeCursor(readBooleanOrDefault(
					properties.getProperty(PROPERTY_INCLUDE_CURSOR_ENABLED), DEFAULT_INCLUDE_CURSOR_ENABLED));
			config.setIsIncludeSettingWindow(readBooleanOrDefault(
					properties.getProperty(PROPERTY_INCLUDE_SETTING_WINDOW_ENABLED), DEFAULT_INCLUDE_SETTING_WINDOW_ENABLED));

			// 出力設定
			config.setIsSaveImage(readBooleanOrDefault(
					properties.getProperty(PROPERTY_SAVE_IMAGE_ENABLED), DEFAULT_SAVE_IMAGE_ENABLED));
			config.setIsCopyToClipboard(readBooleanOrDefault(
					properties.getProperty(PROPERTY_CLIPBOARD_ENABLED), DEFAULT_CLIPBOARD_ENABLED));
			config.setSavePath(
					properties.getProperty(PROPERTY_SAVE_PATH));
			config.setFileNamePrefix(
					properties.getProperty(PROPERTY_FILE_NAME_PREFIX));
			config.setFileNameDateFormatType(FileNameDateFormatType.parseOrDefault(
					properties.getProperty(PROPERTY_FILE_NAME_DATE_FORMAT)));
			config.setFormatType(FormatType.parseOrDefault(
					properties.getProperty(PROPERTY_FORMAT)));

			// 撮影後設定
			config.setAfterSnapActionType(AfterSnapActionType.parseOrDefault(
					properties.getProperty(PROPERTY_AFTER_SNAP_ACTION)));
			config.setIsShowNotification(readBooleanOrDefault(
					properties.getProperty(PROPERTY_NOTIFICATION_ENABLED), DEFAULT_NOTIFICATION_ENABLED));

			// サウンド設定
			config.setIsSnapSoundEnabled(readBooleanOrDefault(
					properties.getProperty(PROPERTY_SOUND_SNAP_ENABLED), DEFAULT_SOUND_SNAP_ENABLED));
			config.setIsNotificationSoundEnabled(readBooleanOrDefault(
					properties.getProperty(PROPERTY_SOUND_NOTIFICATION_ENABLED), DEFAULT_SOUND_NOTIFICATION_ENABLED));

			// 撮影キー
			KeyCombinationBean snapKeys = new KeyCombinationBean();
			snapKeys.setTriggerKey(toNativeKeyCode(
					properties.getProperty(PROPERTY_SNAP_KEY)));
			snapKeys.setUseAlt(readBooleanOrDefault(
					properties.getProperty(PROPERTY_SNAP_USE_ALT), DEFAULT_SNAP_USE_ALT));
			snapKeys.setUseCtrl(readBooleanOrDefault(
					properties.getProperty(PROPERTY_SNAP_USE_CTRL), DEFAULT_SNAP_USE_CTRL));
			snapKeys.setUseShift(readBooleanOrDefault(
					properties.getProperty(PROPERTY_SNAP_USE_SHIFT), DEFAULT_SNAP_USE_SHIFT));
			config.setSnapKeyBean(snapKeys);

			// 範囲選択キー
			KeyCombinationBean selectionKeys = new KeyCombinationBean();
			selectionKeys.setTriggerKey(toNativeKeyCode(
					properties.getProperty(PROPERTY_SELECTION_KEY)));
			selectionKeys.setUseAlt(readBooleanOrDefault(
					properties.getProperty(PROPERTY_SELECTION_USE_ALT),
					DEFAULT_SELECTION_USE_ALT));
			selectionKeys.setUseCtrl(readBooleanOrDefault(
					properties.getProperty(PROPERTY_SELECTION_USE_CTRL),
					DEFAULT_SELECTION_USE_CTRL));
			selectionKeys.setUseShift(readBooleanOrDefault(
					properties.getProperty(PROPERTY_SELECTION_USE_SHIFT),
					DEFAULT_SELECTION_USE_SHIFT));
			config.setSelectionKeyBean(selectionKeys);
			config.setIsAutoSnapAfterRangeSelectionEnabled(readBooleanOrDefault(
					properties.getProperty(PROPERTY_AUTO_SNAP_AFTER_RANGE_SELECTION_ENABLED),
					DEFAULT_AUTO_SNAP_AFTER_RANGE_SELECTION_ENABLED));
			config.setIsCloseRangeSelectionAfterSnapEnabled(readBooleanOrDefault(
					properties.getProperty(PROPERTY_CLOSE_RANGE_SELECTION_AFTER_SNAP_ENABLED),
					DEFAULT_CLOSE_RANGE_SELECTION_AFTER_SNAP_ENABLED));

		} catch (IOException ie) {
			throw new ServiceException("プロパティファイルの読み込みに失敗しました", ie);
		}
	}

	/**
	 * プロパティファイルの書き込みを行います
	 * @param config ユーザーの設定値を保持するクラス
	 * @throws ServiceException
	 */
	public void save(ConfigBean config) throws ServiceException {

		// 言語に応じたプロパティテンプレート取得
		String template = resolvePropertyTemplate(config.getLanguageType());
		// 更新するプロパティキーと値をセットしたマップを作成
		Map<String, String> updateMap = createUpdateMap(config);

		try (BufferedWriter writer = Files.newBufferedWriter(TEMP_PATH, StandardCharsets.UTF_8); // 一時ファイル自動作成
				BufferedReader reader = new BufferedReader(new StringReader(template))) {

			String line;
			while ((line = reader.readLine()) != null) {
				// 余分なスペース除去（空白は書き込まない）
				line = stripSafe(line);
				// 何文字目に "=" があるかを取得する (一文字目が0とする)
				int index = line.indexOf("=");
				// コメント行は先に処理
				if (line.startsWith("#") || index <= 0) {
					writer.write(line);
					writer.newLine();
					continue;
				}

				String key = line.substring(0, index); // "="の左側の文字を取得

				if (updateMap.containsKey(key)) {
					// keyがupdateMapに存在したら更新
					String value = updateMap.get(key);
					writer.write(key + "=" + escapePropertyValue(value)); // エスケープ処理を行う
				} else {
					writer.write(line); // 取得した行のまま書き写し
				}
				writer.newLine(); // 文末改行
			}
		} catch (IOException e) {
			throw new ServiceException("プロパティファイルの書き込みに失敗しました", e);
		}

		// readerとwriterは閉じてから処理する
		try {
			// 一時ファイルを元ファイルに置換する
			Files.move(TEMP_PATH, PROPERTIES_PATH, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new ServiceException("プロパティファイルの生成に失敗しました", e);
		}
	}

	/**
	 * プロパティファイルの初期化を行います
	 * @throws ServiceException 初期化失敗時
	 */
	public synchronized void reset() throws ServiceException {
		try {
			// プロパティファイルがなければ作成、あれば文章を上書きする (追記しない)
			Files.writeString(PROPERTIES_PATH, PROPERTY_TEMPLATE_EN, StandardCharsets.UTF_8);
			log.info("プロパティファイルを初期化しました");

		} catch (IOException ie) {
			throw new ServiceException("プロパティファイルの初期化に失敗しました", ie);
		}
	}

	/**
	 * 初期起動時に二重起動防止メッセージを出力するため、言語設定のみを取得する
	 * プロパティファイルの実在チェック、再生成はしない。
	 * 
	 * @return 設定している言語設定  取得できない場合は、英語として返す
	 */
	public static LanguageType readLanguageOnly() {
		Properties properties = new Properties();
		try (BufferedReader reader = Files.newBufferedReader(PROPERTIES_PATH, StandardCharsets.UTF_8)) {
			properties.load(reader);
			LanguageType language = LanguageType.parseOrDefault(properties.getProperty(PROPERTY_LANGUAGE));
			return language;

		} catch (IOException e) {
			log.warn("初期起動時、言語設定の読み取りに失敗しました");
			return LanguageType.ENGLISH;
		}
	}

	/**
	 * プロパティファイルの実在チェックを行い、存在しなければ再生成する
	 * @throws ServiceException Resetが失敗した場合にthrowする
	 */
	private void checkAndResetProperties() throws ServiceException {
		// ファイルの実在チェック
		if (!Files.exists(PROPERTIES_PATH)) {
			log.warn("プロパティファイルが見つかりません。再生成を行います");
			reset(); // 再生成
			log.warn("プロパティファイルを再生成しました");

		}
	}

	/**
	 * プロパティのキーと更新する値をセットにしたマップを作成する
	 * @param config
	 * @return プロパティファイルのキーと設定クラスの値を格納したマップ
	 */
	private Map<String, String> createUpdateMap(ConfigBean config) {
		// 更新したいキーと値のペアをMapに格納する
		Map<String, String> updateMap = new HashMap<>();

		// 言語設定
		updateMap.put(PROPERTY_LANGUAGE, config.getLanguageType().getValue());

		// テーマ設定
		updateMap.put(PROPERTY_THEME, config.getThemeType().getValue());

		// 撮影設定
		updateMap.put(PROPERTY_SNAP_AREA, config.getSnapAreaType().getValue());
		updateMap.put(PROPERTY_INCLUDE_CURSOR_ENABLED, String.valueOf(config.getIsIncludeCursor()));
		updateMap.put(PROPERTY_INCLUDE_SETTING_WINDOW_ENABLED, String.valueOf(config.getIsIncludeSettingWindow()));

		// 出力設定
		updateMap.put(PROPERTY_SAVE_IMAGE_ENABLED, String.valueOf(config.getIsSaveImage()));
		updateMap.put(PROPERTY_CLIPBOARD_ENABLED, String.valueOf(config.getIsCopyToClipboard()));
		updateMap.put(PROPERTY_SAVE_PATH, config.getSavePath());
		updateMap.put(PROPERTY_FILE_NAME_PREFIX, config.getFileNamePrefix());
		updateMap.put(PROPERTY_FILE_NAME_DATE_FORMAT, config.getFileNameDateFormatType().getValue());
		updateMap.put(PROPERTY_FORMAT, config.getFormatType().getValue());

		// 撮影後設定, 通知設定
		updateMap.put(PROPERTY_AFTER_SNAP_ACTION, config.getAfterSnapActionType().getValue());
		updateMap.put(PROPERTY_NOTIFICATION_ENABLED, String.valueOf(config.getIsShowNotification()));
		updateMap.put(PROPERTY_SOUND_SNAP_ENABLED, String.valueOf(config.getIsSnapSoundEnabled()));
		updateMap.put(PROPERTY_SOUND_NOTIFICATION_ENABLED, String.valueOf(config.getIsNotificationSoundEnabled()));

		// -- ショートカットキーの設定 --
		KeyCombinationBean snapKeys = config.getSnapKeyBean();
		updateMap.put(PROPERTY_SNAP_KEY, String.valueOf(snapKeys.getTriggerKey()));
		updateMap.put(PROPERTY_SNAP_USE_ALT, String.valueOf(snapKeys.isUseAlt()));
		updateMap.put(PROPERTY_SNAP_USE_CTRL, String.valueOf(snapKeys.isUseCtrl()));
		updateMap.put(PROPERTY_SNAP_USE_SHIFT, String.valueOf(snapKeys.isUseShift()));

		KeyCombinationBean selectionKeys = config.getSelectionKeyBean();
		updateMap.put(PROPERTY_SELECTION_KEY, String.valueOf(selectionKeys.getTriggerKey()));
		updateMap.put(PROPERTY_SELECTION_USE_ALT, String.valueOf(selectionKeys.isUseAlt()));
		updateMap.put(PROPERTY_SELECTION_USE_CTRL, String.valueOf(selectionKeys.isUseCtrl()));
		updateMap.put(PROPERTY_SELECTION_USE_SHIFT, String.valueOf(selectionKeys.isUseShift()));
		updateMap.put(
				PROPERTY_AUTO_SNAP_AFTER_RANGE_SELECTION_ENABLED,
				String.valueOf(config.getIsAutoSnapAfterRangeSelectionEnabled()));
		updateMap.put(
				PROPERTY_CLOSE_RANGE_SELECTION_AFTER_SNAP_ENABLED,
				String.valueOf(config.getIsCloseRangeSelectionAfterSnapEnabled()));

		return updateMap;
	}

	/**
	 * 言語設定に対応したプロパティテンプレートを解決する
	 * @param languageType 言語設定(デフォルトは英語)
	 * @return プロパティファイルのテンプレート
	 */
	private String resolvePropertyTemplate(LanguageType languageType) {
		switch (languageType) {
		case JAPANESE:
			return PROPERTY_TEMPLATE_JA;
		case SIMPLIFIED_CHINESE:
			return PROPERTY_TEMPLATE_ZH_CN;
		case KOREAN:
			return PROPERTY_TEMPLATE_KO;
		case FRENCH:
			return PROPERTY_TEMPLATE_FR;
		case SPANISH:
			return PROPERTY_TEMPLATE_ES;
		case RUSSIAN:
			return PROPERTY_TEMPLATE_RU;
		case GERMAN:
			return PROPERTY_TEMPLATE_DE;
		case BRAZILIAN_PORTUGUESE:
			return PROPERTY_TEMPLATE_PT_BR;

		case ENGLISH:
		default:
			return PROPERTY_TEMPLATE_EN;
		}
	}

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（日本語） */
	private static final String PROPERTY_TEMPLATE_JA = """
			# InstantSnap Settings

			# 言語を指定してください
			language=ja

			# テーマを指定してください
			# 0 -> デフォルト, 1 -> ダーク, 2 -> 絵本
			theme=0

			# ～～ 撮影設定 ～～
			# 撮影ショートカットキー
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# 範囲選択ショートカットキー
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# 範囲確定後に自動で撮影する
			autoSnapAfterRangeSelectionEnabled=true

			# 撮影後に範囲撮影モードを自動で終了する
			closeRangeSelectionAfterSnapEnabled=true

			# 撮影範囲の選択 デフォルトは 1
			# 1 -> 使用しているウィンドウを撮影, 2 -> カーソルのあるモニターを撮影
			# 3 -> メインモニターを撮影, 4 -> 全てのモニターを撮影
			snapArea=1

			# マウスカーソルを画像に含める
			includeCursorEnabled=false

			# InstantSnapのウィンドウを画像に含める
			includeSettingWindowEnabled=false

			# ～～ 出力設定 ～～
			# 画像を保存する（有効にする場合は"true"と記載してください）
			saveImageEnabled=true

			# クリップボードにコピーする（有効にする場合は"true"と記載してください）
			clipboardEnabled=true

			# 保存先パスを指定してください
			savePath=./screenshots/

			# ファイル名の先頭文字
			fileNamePrefix=

			# ファイル名につける日時フォーマット デフォルトは1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# ファイルの拡張子の選択 png、jpg、bmp、gifのいずれかを記載してください(デフォルト：png)
			format=png

			# ～～ 撮影後設定 ～～
			# 撮影後に行う処理を選択
			# 1 -> 画像ファイルを表示する, 2 -> ペイントを開く,
			# 3 -> 一時保存ファイルに保存する, 0, 未記入等 -> 何もしない(デフォルト)
			afterSnapAction=0

			# 撮影時、通知を表示する（有効にする場合は"true"と記載してください）
			notificationEnabled=true

			# ～～ サウンド設定 ～～#
			# 通知音を有効にする（有効にする場合は"true"と記載してください）
			soundNotificationEnabled=true

			# 撮影音を鳴らして撮影する（有効にする場合は"true"と記載してください）
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（英語） */
	private static final String PROPERTY_TEMPLATE_EN = """
			# InstantSnap Settings

			# Please specify the language
			language=en

			# Please specify the theme
			# 0 -> Default, 1 -> Dark, 2 -> Storybook
			theme=0

			# ~~ Capture Settings ~~
			# Capture shortcut key
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# Range selection shortcut key
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# Capture automatically after selecting a range
			autoSnapAfterRangeSelectionEnabled=true

			# Automatically exit range capture mode after capturing
			closeRangeSelectionAfterSnapEnabled=true

			# Select the capture area
			# 1 -> Active window (default), 2 -> Active monitor
			# 3 -> Main monitor, 4 -> All monitors
			snapArea=1

			# Include the mouse cursor in the image
			includeCursorEnabled=false

			# Include the setting window in the image
			includeSettingWindowEnabled=false

			# ~~ Output Settings ~~
			# Save the image (set to "true" to enable)
			saveImageEnabled=true

			# Copy to clipboard (set to "true" to enable)
			clipboardEnabled=true

			# Please specify the save path
			savePath=./screenshots/

			# Prefix added to the beginning of the file name
			fileNamePrefix=

			# Date/time format added to the file name. Default is 1.
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# Select the file extension. Specify one of png, jpg, bmp, or gif. (default: png)
			format=png

			# ~~ Post-Capture Settings ~~
			# Select the action to perform after capturing
			# 1 -> Open the image file, 2 -> Open Paint,
			# 3 -> Save to a temporary file, 0, blank, etc. -> Do nothing (default)
			afterSnapAction=0

			# Show a notification when capturing (set to "true" to enable)
			notificationEnabled=true

			# ~~ Sound Settings ~~
			# Enable notification sound (set to "true" to enable)
			soundNotificationEnabled=true

			# Play a shutter sound when capturing (set to "true" to enable)
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（中国語・簡体字） */
	private static final String PROPERTY_TEMPLATE_ZH_CN = """
			# InstantSnap 设置

			# 指定语言
			language=zh-CN

			# 指定主题
			# 0 -> 默认, 1 -> 深色, 2 -> 绘本
			theme=0

			# ~~ 截图设置 ~~
			# 截图快捷键
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# 区域选择快捷键
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# 选定区域后自动截图
			autoSnapAfterRangeSelectionEnabled=true

			# 截图后自动退出区域模式
			closeRangeSelectionAfterSnapEnabled=true

			# 选择截图范围
			# 1 -> 当前窗口（默认）, 2 -> 指针所在屏幕
			# 3 -> 主屏幕, 4 -> 所有屏幕
			snapArea=1

			# 在图片中包含鼠标指针
			includeCursorEnabled=false

			# 在图片中包含 InstantSnap 窗口
			includeSettingWindowEnabled=false

			# ~~ 输出设置 ~~
			# 保存图片（设为 "true" 即启用）
			saveImageEnabled=true

			# 复制到剪贴板（设为 "true" 即启用）
			clipboardEnabled=true

			# 指定保存路径
			savePath=./screenshots/

			# 文件名前缀
			fileNamePrefix=

			# 文件名的日期时间格式，默认为 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# 文件扩展名：png、jpg、bmp 或 gif（默认：png）
			format=png

			# ~~ 截图后设置 ~~
			# 选择截图后的操作
			# 1 -> 打开图片, 2 -> 用画图打开,
			# 3 -> 保存到临时文件夹, 0 或留空 -> 无操作（默认）
			afterSnapAction=0

			# 截图时显示通知（设为 "true" 即启用）
			notificationEnabled=true

			# ~~ 声音设置 ~~
			# 启用通知音（设为 "true" 即启用）
			soundNotificationEnabled=true

			# 截图时播放快门声（设为 "true" 即启用）
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（韓国語） */
	private static final String PROPERTY_TEMPLATE_KO = """
			# InstantSnap 설정

			# 언어 지정
			language=ko

			# 테마 지정
			# 0 -> 기본, 1 -> 다크, 2 -> 동화책
			theme=0

			# ~~ 캡처 설정 ~~
			# 캡처 단축키
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# 영역 선택 단축키
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# 영역 선택 후 자동 캡처
			autoSnapAfterRangeSelectionEnabled=true

			# 캡처 후 영역 모드 자동 종료
			closeRangeSelectionAfterSnapEnabled=true

			# 캡처 영역 선택
			# 1 -> 현재 창(기본), 2 -> 포인터가 있는 화면
			# 3 -> 주 화면, 4 -> 모든 화면
			snapArea=1

			# 이미지에 마우스 포인터 포함
			includeCursorEnabled=false

			# 이미지에 InstantSnap 창 포함
			includeSettingWindowEnabled=false

			# ~~ 출력 설정 ~~
			# 이미지 저장 (사용하려면 "true")
			saveImageEnabled=true

			# 클립보드에 복사 (사용하려면 "true")
			clipboardEnabled=true

			# 저장 경로 지정
			savePath=./screenshots/

			# 파일 이름 접두사
			fileNamePrefix=

			# 파일 이름의 날짜/시간 형식. 기본값은 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# 파일 확장자: png, jpg, bmp, gif 중 하나 (기본값: png)
			format=png

			# ~~ 캡처 후 설정 ~~
			# 캡처 후 동작 선택
			# 1 -> 이미지 열기, 2 -> 그림판에서 열기,
			# 3 -> 임시 폴더에 저장, 0 또는 빈 값 -> 없음(기본)
			afterSnapAction=0

			# 캡처 시 알림 표시 (사용하려면 "true")
			notificationEnabled=true

			# ~~ 소리 설정 ~~
			# 알림음 사용 (사용하려면 "true")
			soundNotificationEnabled=true

			# 캡처 시 셔터음 재생 (사용하려면 "true")
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（フランス語） */
	private static final String PROPERTY_TEMPLATE_FR = """
			# Paramètres InstantSnap

			# Langue
			language=fr

			# Thème
			# 0 -> Défaut, 1 -> Sombre, 2 -> Conte
			theme=0

			# ~~ Réglages de capture ~~
			# Raccourci de capture
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# Raccourci de sélection de zone
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# Capturer automatiquement la zone sélectionnée
			autoSnapAfterRangeSelectionEnabled=true

			# Quitter automatiquement le mode zone après capture
			closeRangeSelectionAfterSnapEnabled=true

			# Zone de capture
			# 1 -> Fenêtre active (défaut), 2 -> Écran du pointeur
			# 3 -> Écran principal, 4 -> Tous les écrans
			snapArea=1

			# Inclure le pointeur dans l’image
			includeCursorEnabled=false

			# Inclure les fenêtres InstantSnap dans l’image
			includeSettingWindowEnabled=false

			# ~~ Réglages de sortie ~~
			# Enregistrer l’image ("true" pour activer)
			saveImageEnabled=true

			# Copier dans le presse-papiers ("true" pour activer)
			clipboardEnabled=true

			# Dossier d’enregistrement
			savePath=./screenshots/

			# Préfixe du nom de fichier
			fileNamePrefix=

			# Format de date du nom de fichier. Valeur par défaut : 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# Extension : png, jpg, bmp ou gif (défaut : png)
			format=png

			# ~~ Après la capture ~~
			# Action après la capture
			# 1 -> Ouvrir l’image, 2 -> Ouvrir dans Paint,
			# 3 -> Dossier temporaire, 0 ou vide -> Aucune (défaut)
			afterSnapAction=0

			# Afficher une notification ("true" pour activer)
			notificationEnabled=true

			# ~~ Réglages du son ~~
			# Activer le son de notification ("true" pour activer)
			soundNotificationEnabled=true

			# Jouer le son de capture ("true" pour activer)
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（スペイン語） */
	private static final String PROPERTY_TEMPLATE_ES = """
			# Ajustes de InstantSnap

			# Idioma
			language=es

			# Tema
			# 0 -> Predeterminado, 1 -> Oscuro, 2 -> Cuento
			theme=0

			# ~~ Ajustes de captura ~~
			# Atajo de captura
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# Atajo de selección de área
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# Capturar automáticamente el área seleccionada
			autoSnapAfterRangeSelectionEnabled=true

			# Salir automáticamente del modo área tras capturar
			closeRangeSelectionAfterSnapEnabled=true

			# Área de captura
			# 1 -> Ventana activa (predeterminado), 2 -> Pantalla del puntero
			# 3 -> Pantalla principal, 4 -> Todas las pantallas
			snapArea=1

			# Incluir el puntero en la imagen
			includeCursorEnabled=false

			# Incluir las ventanas de InstantSnap en la imagen
			includeSettingWindowEnabled=false

			# ~~ Ajustes de salida ~~
			# Guardar la imagen ("true" para activar)
			saveImageEnabled=true

			# Copiar al portapapeles ("true" para activar)
			clipboardEnabled=true

			# Carpeta de destino
			savePath=./screenshots/

			# Prefijo del nombre de archivo
			fileNamePrefix=

			# Formato de fecha del nombre. Valor predeterminado: 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# Extensión: png, jpg, bmp o gif (predeterminado: png)
			format=png

			# ~~ Después de capturar ~~
			# Acción después de capturar
			# 1 -> Abrir la imagen, 2 -> Abrir en Paint,
			# 3 -> Carpeta temporal, 0 o vacío -> Ninguna (predeterminado)
			afterSnapAction=0

			# Mostrar una notificación ("true" para activar)
			notificationEnabled=true

			# ~~ Ajustes de sonido ~~
			# Activar sonido de notificación ("true" para activar)
			soundNotificationEnabled=true

			# Reproducir sonido de captura ("true" para activar)
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（ロシア語） */
	private static final String PROPERTY_TEMPLATE_RU = """
			# Настройки InstantSnap

			# Язык
			language=ru

			# Тема
			# 0 -> Обычная, 1 -> Тёмная, 2 -> Сказка
			theme=0

			# ~~ Настройки снимка ~~
			# Горячая клавиша снимка
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# Горячая клавиша выбора области
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# Автоматически снимать выбранную область
			autoSnapAfterRangeSelectionEnabled=true

			# Автоматически закрывать режим области после снимка
			closeRangeSelectionAfterSnapEnabled=true

			# Область снимка
			# 1 -> Активное окно (по умолчанию), 2 -> Экран с указателем
			# 3 -> Основной экран, 4 -> Все экраны
			snapArea=1

			# Включить указатель мыши в изображение
			includeCursorEnabled=false

			# Включить окна InstantSnap в изображение
			includeSettingWindowEnabled=false

			# ~~ Настройки вывода ~~
			# Сохранять изображение ("true" — включено)
			saveImageEnabled=true

			# Копировать в буфер обмена ("true" — включено)
			clipboardEnabled=true

			# Папка сохранения
			savePath=./screenshots/

			# Префикс имени файла
			fileNamePrefix=

			# Формат даты в имени файла. По умолчанию: 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# Расширение: png, jpg, bmp или gif (по умолчанию: png)
			format=png

			# ~~ После снимка ~~
			# Действие после снимка
			# 1 -> Открыть изображение, 2 -> Открыть в Paint,
			# 3 -> Во временную папку, 0 или пусто -> Нет (по умолчанию)
			afterSnapAction=0

			# Показывать уведомление ("true" — включено)
			notificationEnabled=true

			# ~~ Настройки звука ~~
			# Включить звук уведомления ("true" — включено)
			soundNotificationEnabled=true

			# Воспроизводить звук снимка ("true" — включено)
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（ドイツ語） */
	private static final String PROPERTY_TEMPLATE_DE = """
			# InstantSnap-Einstellungen

			# Sprache
			language=de

			# Design
			# 0 -> Standard, 1 -> Dunkel, 2 -> Bilderbuch
			theme=0

			# ~~ Aufnahme-Einstellungen ~~
			# Tastenkürzel für Aufnahme
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# Tastenkürzel für Bereichsauswahl
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# Gewählten Bereich automatisch aufnehmen
			autoSnapAfterRangeSelectionEnabled=true

			# Bereichsmodus nach der Aufnahme automatisch schließen
			closeRangeSelectionAfterSnapEnabled=true

			# Aufnahmebereich
			# 1 -> Aktives Fenster (Standard), 2 -> Bildschirm am Mauszeiger
			# 3 -> Hauptbildschirm, 4 -> Alle Bildschirme
			snapArea=1

			# Mauszeiger in das Bild aufnehmen
			includeCursorEnabled=false

			# InstantSnap-Fenster in das Bild aufnehmen
			includeSettingWindowEnabled=false

			# ~~ Ausgabe-Einstellungen ~~
			# Bild speichern (zum Aktivieren "true")
			saveImageEnabled=true

			# In Zwischenablage kopieren (zum Aktivieren "true")
			clipboardEnabled=true

			# Speicherpfad
			savePath=./screenshots/

			# Präfix des Dateinamens
			fileNamePrefix=

			# Datumsformat im Dateinamen. Standard: 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# Dateiendung: png, jpg, bmp oder gif (Standard: png)
			format=png

			# ~~ Nach der Aufnahme ~~
			# Aktion nach der Aufnahme
			# 1 -> Bild öffnen, 2 -> In Paint öffnen,
			# 3 -> Im Temp-Ordner speichern, 0 oder leer -> Keine (Standard)
			afterSnapAction=0

			# Benachrichtigung anzeigen (zum Aktivieren "true")
			notificationEnabled=true

			# ~~ Ton-Einstellungen ~~
			# Hinweiston aktivieren (zum Aktivieren "true")
			soundNotificationEnabled=true

			# Aufnahmeton abspielen (zum Aktivieren "true")
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（ポルトガル語・ブラジル） */
	private static final String PROPERTY_TEMPLATE_PT_BR = """
			# Configurações do InstantSnap

			# Idioma
			language=pt-BR

			# Tema
			# 0 -> Padrão, 1 -> Escuro, 2 -> Livro infantil
			theme=0

			# ~~ Ajustes de captura ~~
			# Atalho de captura
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# Atalho de seleção de área
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# Capturar automaticamente a área selecionada
			autoSnapAfterRangeSelectionEnabled=true

			# Sair automaticamente do modo de área após capturar
			closeRangeSelectionAfterSnapEnabled=true

			# Área de captura
			# 1 -> Janela ativa (padrão), 2 -> Tela do ponteiro
			# 3 -> Tela principal, 4 -> Todas as telas
			snapArea=1

			# Incluir o ponteiro na imagem
			includeCursorEnabled=false

			# Incluir as janelas do InstantSnap na imagem
			includeSettingWindowEnabled=false

			# ~~ Ajustes de saída ~~
			# Salvar a imagem (use "true" para ativar)
			saveImageEnabled=true

			# Copiar para a área de transferência (use "true" para ativar)
			clipboardEnabled=true

			# Pasta de destino
			savePath=./screenshots/

			# Prefixo do nome do arquivo
			fileNamePrefix=

			# Formato de data no nome. Padrão: 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# Extensão: png, jpg, bmp ou gif (padrão: png)
			format=png

			# ~~ Após capturar ~~
			# Ação após a captura
			# 1 -> Abrir imagem, 2 -> Abrir no Paint,
			# 3 -> Pasta temporária, 0 ou vazio -> Nenhuma (padrão)
			afterSnapAction=0

			# Mostrar notificação (use "true" para ativar)
			notificationEnabled=true

			# ~~ Ajustes de som ~~
			# Ativar som de notificação (use "true" para ativar)
			soundNotificationEnabled=true

			# Reproduzir som de captura (use "true" para ativar)
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

}
