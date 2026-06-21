package com.github.rebi0395.instantsnap.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
import com.github.rebi0395.instantsnap.model.enums.SnapAreaType;
import com.github.rebi0395.instantsnap.model.enums.TriggerType;

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

	/** プロパティのファイルパス */
	private static final Path PROPERTIES_PATH = Path.of(OPTION_FILE_NAME);

	/** プロパティファイルの一時保存パス */
	private static final Path TEMP_PATH = PROPERTIES_PATH.resolveSibling(PROPERTIES_PATH.getFileName() + ".tmp");

	/**
	 * コンストラクタ
	 */
	public PropertiesRepository() {
	}

	//TODO 不足キー追加処理

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

			// 撮影設定
			config.setTriggerType(TriggerType.parseOrDefault(
					properties.getProperty(PROPERTY_TRIGGER)));
			config.setSnapAreaType(SnapAreaType.parseOrDefault(
					properties.getProperty(PROPERTY_SNAP_AREA)));

			// 出力設定
			config.setIsSaveImage(readBooleanOrDefault(
					properties.getProperty(PROPERTY_SAVE_IMAGE_ENABLED), DEFAULT_SAVE_IMAGE_ENABLED));
			config.setIsCopyToClipboard(readBooleanOrDefault(
					properties.getProperty(PROPERTY_CLIPBOARD_ENABLED), DEFAULT_CLIPBOARD_ENABLED));
			config.setSavePath(
					properties.getProperty(PROPERTY_SAVE_PATH));
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

			// カスタムキー
			KeyCombinationBean customKeys = config.getCustomKeyBean();
			customKeys.setTriggerKey(toNativeKeyCode(
					properties.getProperty(PROPERTY_CUSTOM_KEY)));
			customKeys.setUseAlt(readBooleanOrDefault(
					properties.getProperty(PROPERTY_USE_ALT), DEFAULT_USE_ALT));
			customKeys.setUseCtrl(readBooleanOrDefault(
					properties.getProperty(PROPERTY_USE_CTRL), DEFAULT_USE_CTRL));
			customKeys.setUseShift(readBooleanOrDefault(
					properties.getProperty(PROPERTY_USE_SHIFT), DEFAULT_USE_SHIFT));

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

		// 撮影設定
		updateMap.put(PROPERTY_TRIGGER, config.getTriggerType().getValue());
		updateMap.put(PROPERTY_SNAP_AREA, config.getSnapAreaType().getValue());

		// 出力設定
		updateMap.put(PROPERTY_SAVE_IMAGE_ENABLED, String.valueOf(config.getIsSaveImage()));
		updateMap.put(PROPERTY_CLIPBOARD_ENABLED, String.valueOf(config.getIsCopyToClipboard()));
		updateMap.put(PROPERTY_SAVE_PATH, config.getSavePath());
		updateMap.put(PROPERTY_FORMAT, config.getFormatType().getValue());

		// 撮影後設定, 通知設定
		updateMap.put(PROPERTY_AFTER_SNAP_ACTION, config.getAfterSnapActionType().getValue());
		updateMap.put(PROPERTY_NOTIFICATION_ENABLED, String.valueOf(config.getIsShowNotification()));
		updateMap.put(PROPERTY_SOUND_SNAP_ENABLED, String.valueOf(config.getIsSnapSoundEnabled()));
		updateMap.put(PROPERTY_SOUND_NOTIFICATION_ENABLED, String.valueOf(config.getIsNotificationSoundEnabled()));

		// -- カスタムキーの設定 --
		KeyCombinationBean customKeys = config.getCustomKeyBean();
		updateMap.put(PROPERTY_CUSTOM_KEY, String.valueOf(customKeys.getTriggerKey()));
		updateMap.put(PROPERTY_USE_ALT, String.valueOf(customKeys.isUseAlt()));
		updateMap.put(PROPERTY_USE_CTRL, String.valueOf(customKeys.isUseCtrl()));
		updateMap.put(PROPERTY_USE_SHIFT, String.valueOf(customKeys.isUseShift()));

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

		case ENGLISH:
		default:
			return PROPERTY_TEMPLATE_EN;
		}
	}

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（日本語） */
	private static final String PROPERTY_TEMPLATE_JA = """
			#InstantSnap Settings

			#言語を指定してください
			language=ja

			#～～ 撮影設定 ～～#
			#スクショ発動キーの選択
			#1 -> shift + printscreen(デフォルト), 2 -> Ctrl + Shift + S
			#3 -> F9, 4 -> カスタムキー
			trigger=1

			#カスタムメインキー設定 -- triggerを4にした上でキーコードを記載してください
			#未使用な場合は、0
			customKey=0

			#カスタムキーのコンビネーションキー設定 -- 使用する場合はtrue メインキー必須
			useAlt=false
			useCtrl=false
			useShift=false

			#撮影範囲の選択
			#1 -> アクティブウィンドウ(デフォルト), 2 -> アクティブモニター
			#3 -> メインモニター, 4 -> 全てのモニター
			snapArea=1

			#～～ 出力設定 ～～#
			#画像を保存する（有効にする場合は"true"と記載してください）
			saveImageEnabled=true

			#クリップボードにコピーする（有効にする場合は"true"と記載してください）
			clipboardEnabled=true

			#保存先パスを指定してください
			savePath=./screenshots/

			#拡張子の選択 png、jpg、bmp、gifのいずれかを記載してください(デフォルト：png)
			format=png

			#～～ 撮影後設定 ～～#
			#撮影後に行う処理を選択
			#1 -> 画像ファイルを表示する, 2 -> ペイントを開く,
			#3 -> 一時保存ファイルに保存する, 0, 未記入等 -> 何もしない(デフォルト)
			afterSnapAction=0

			#撮影時、通知を表示する（有効にする場合は"true"と記載してください）
			notificationEnabled=true

			#～～ サウンド設定 ～～#
			#通知音を有効にする（有効にする場合は"true"と記載してください）
			soundNotificationEnabled=true

			#撮影音を鳴らして撮影する（有効にする場合は"true"と記載してください）
			soundSnapEnabled=false

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（英語） */
	private static final String PROPERTY_TEMPLATE_EN = """
			# InstantSnap Settings

			# Please specify the language
			language=en

			# ~~ Capture Settings ~~
			# Select the screenshot trigger key
			# 1 -> Shift + PrintScreen (default), 2 -> Ctrl + Shift + S
			# 3 -> F9, 4 -> Custom key
			trigger=1

			# Custom main key setting -- Set trigger to 4 and specify the key code
			# If not used, set to 0
			customKey=0

			# Custom key modifier settings -- Set to true if used. A main key is required.
			useAlt=false
			useCtrl=false
			useShift=false

			# Select the capture area
			# 1 -> Active window (default), 2 -> Active monitor
			# 3 -> Main monitor, 4 -> All monitors
			snapArea=1

			# ~~ Output Settings ~~
			# Save the image (set to "true" to enable)
			saveImageEnabled=true

			# Copy to clipboard (set to "true" to enable)
			clipboardEnabled=true

			# Please specify the save path
			savePath=./screenshots/

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
			soundSnapEnabled=false

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

}
