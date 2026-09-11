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
import com.github.rebi0395.instantsnap.repository.template.PropertyFileTemplate;

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
		String template = PropertyFileTemplate.resolve(config.getLanguageType());
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
			Files.writeString(
					PROPERTIES_PATH,
					PropertyFileTemplate.resolve(LanguageType.ENGLISH),
					StandardCharsets.UTF_8);
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

}
