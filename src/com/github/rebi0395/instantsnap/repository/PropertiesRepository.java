package com.github.rebi0395.instantsnap.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
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
import com.github.rebi0395.instantsnap.model.enums.SnapAfter;
import com.github.rebi0395.instantsnap.model.enums.SnapArea;
import com.github.rebi0395.instantsnap.model.enums.TriggerType;

import static com.github.rebi0395.instantsnap.common.NativeKeyCodeConverter.*;
import static com.github.rebi0395.instantsnap.common.AppConstant.*;
import static com.github.rebi0395.instantsnap.common.StrCheckUtil.*;

/**
 * プロパティファイルの読み取り、書き込みを行い、
 * ユーザー設定の永続化を担保するクラスです
 */
public class PropertiesRepository {

	/** プロパティのファイルパス */
	private static final Path PROPERTIES_PATH = Path.of(OPTION_FILE_NAME);

	/** プロパティファイルの一時保存パス */
	private static final Path TEMP_PATH = PROPERTIES_PATH.resolveSibling(PROPERTIES_PATH.getFileName() + ".tmp");

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(PropertiesRepository.class);

	/**
	 * コンストラクタ
	 */
	public PropertiesRepository() {
	}

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
			config.setLanguageType(LanguageType.parseOrDefault(properties.getProperty(PROPERTY_LANGUAGE)));

			// 撮影設定
			config.setTriggerType(TriggerType.parseOrDefault(properties.getProperty(PROPERTY_TRIGGER)));
			config.setSnapArea(SnapArea.parseOrDefault(properties.getProperty(PROPERTY_SNAP_AREA)));

			// 出力設定
			config.setIsSaveImage(isStrTrue(properties.getProperty(PROPERTY_SAVE_IMAGE_ENABLED)));
			config.setIsCopyToClipboard(isStrTrue(properties.getProperty(PROPERTY_CLIPBOARD_ENABLED)));
			config.setSavePath(properties.getProperty(PROPERTY_SAVE_PATH));
			config.setFormatType(FormatType.parseOrDefault(properties.getProperty(PROPERTY_FORMAT)));

			// 撮影後設定
			config.setSnapAfter(SnapAfter.parseOrDefault(properties.getProperty(PROPERTY_SNAP_AFTER)));
			config.setIsShowNotification(isStrTrue(properties.getProperty(PROPERTY_NOTIFICATION_ENABLED)));
			config.setIsSoundEnabled(isStrTrue(properties.getProperty(PROPERTY_SOUND_ENABLED)));

			// カスタムキー
			KeyCombinationBean customKeys = config.getCustomKeyBean();
			customKeys.setTriggerKey(toNativeKeyCode(properties.getProperty(PROPERTY_CUSTOM_KEY)));
			customKeys.setUseAlt(isStrTrue(properties.getProperty(PROPERTY_USE_ALT)));
			customKeys.setUseCtrl(isStrTrue(properties.getProperty(PROPERTY_USE_CTRL)));
			customKeys.setUseShift(isStrTrue(properties.getProperty(PROPERTY_USE_SHIFT)));

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
		checkAndResetProperties(); // プロパティファイルの実在チェック

		// 更新したいキーと値のペアをMapに格納する
		Map<String, String> updateMap = new HashMap<>();
		updateMap.put(PROPERTY_LANGUAGE, config.getLanguageType().getValue());
		// 撮影設定
		updateMap.put(PROPERTY_TRIGGER, config.getTriggerType().getValue());
		updateMap.put(PROPERTY_SNAP_AREA, config.getSnapArea().getValue());

		// 出力設定
		updateMap.put(PROPERTY_SAVE_IMAGE_ENABLED, String.valueOf(config.getIsSaveImage()));
		updateMap.put(PROPERTY_CLIPBOARD_ENABLED, String.valueOf(config.getIsCopyToClipboard()));
		updateMap.put(PROPERTY_SAVE_PATH, config.getSavePath());
		updateMap.put(PROPERTY_FORMAT, config.getFormatType().getValue());

		// 撮影後設定, 通知設定
		updateMap.put(PROPERTY_SNAP_AFTER, config.getSnapAfter().getValue());
		updateMap.put(PROPERTY_NOTIFICATION_ENABLED, String.valueOf(config.getIsShowNotification()));
		updateMap.put(PROPERTY_SOUND_ENABLED, String.valueOf(config.getIsSoundEnabled()));

		// -- カスタムキーの設定 --
		KeyCombinationBean customKeys = config.getCustomKeyBean();
		updateMap.put(PROPERTY_CUSTOM_KEY, String.valueOf(customKeys.getTriggerKey()));
		updateMap.put(PROPERTY_USE_ALT, String.valueOf(customKeys.isUseAlt()));
		updateMap.put(PROPERTY_USE_CTRL, String.valueOf(customKeys.isUseCtrl()));
		updateMap.put(PROPERTY_USE_SHIFT, String.valueOf(customKeys.isUseShift()));

		try (BufferedWriter writer = Files.newBufferedWriter(TEMP_PATH, StandardCharsets.UTF_8); // 一時ファイル自動作成
				BufferedReader reader = Files.newBufferedReader(PROPERTIES_PATH, StandardCharsets.UTF_8)) {

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
				String value = updateMap.get(key);
				// 常にindex > 0
				if (value != null) {
					writer.write(key + "=" + value); // keyがupdateMapに存在したら更新
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
	 * プロパティファイルの初期化を行います
	 * @throws ServiceException 初期化失敗時
	 */
	public synchronized void reset() throws ServiceException {
		try {
			// プロパティファイルがなければ作成、あれば文章を上書きする (追記しない)
			Files.writeString(PROPERTIES_PATH, DEFAULT_CONTENT, StandardCharsets.UTF_8);
			log.info("プロパティファイルを初期化しました");

		} catch (IOException ie) {
			throw new ServiceException("プロパティファイルの初期化に失敗しました", ie);
		}
	}

	// 初期化時の文章をソース側で保持 改行コードを統一
	private static final String DEFAULT_CONTENT = """
			#InstantSnap Settings

			#言語を指定してください
			language=ja

			#～～ 撮影設定 ～～#
			#スクショ発動キーの選択
			#1 -> shift + printscreen(デフォルト), 2 -> printscreen
			#3 -> F10, 4 -> カスタムキー
			trigger=1

			#カスタムメインキー設定 -- triggerを4にした上でキーコードを記載してください
			#未使用な場合は、0
			customKey=21

			#カスタムキーのコンビネーションキー設定 -- 使用する場合はtrue メインキー必須
			useAlt=true
			useCtrl=true
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
			savePath=C:/Instantsnap画像保存先

			#拡張子の選択 png、jpg、bmp、gifのいずれかを記載してください(デフォルト：png)
			format=jpg

			#～～ 撮影後、通知設定 ～～#
			#撮影後に行う処理を選択
			#1 -> 通知を表示する, 2 -> 画像ファイルを開く, 0, 未記入等 -> 何もしない(デフォルト)
			snapAfter=0

			#撮影時、通知を表示する（有効にする場合は"true"と記載してください）
			notificationEnabled=false

			#撮影時、音を鳴らして撮影する（有効にする場合は"true"と記載してください）
			soundEnabled=false

						""".replace("\r\n", "\n").replace("\n", System.lineSeparator());
}
