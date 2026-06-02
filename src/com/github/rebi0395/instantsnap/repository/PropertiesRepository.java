package com.github.rebi0395.instantsnap.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.SnapAfter;
import com.github.rebi0395.instantsnap.model.enums.SnapArea;
import static com.github.rebi0395.instantsnap.common.AppConstant.*;
import static com.github.rebi0395.instantsnap.common.StrCheckUtil.*;

/**
 * プロパティファイルの読み取り、書き込みを行い、
 * ユーザー設定の永続化を担保するクラスです
 */
public class PropertiesRepository {

	/** プロパティのファイルパス */
	private static final Path PROPERTIES_PATH = Paths.get("config.properties");
	
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

			// プロパティから読み取った情報をセット
			config.setSavePath(properties.getProperty(SAVE_PATH));
			config.setSnapArea(SnapArea.parseOrDefault(properties.getProperty(SNAP_AREA)));
			config.setFormatType(FormatType.parseOrDefault(properties.getProperty(FORMAT)));
			config.setTrigger(properties.getProperty(TRIGGER));
			config.setSnapAfter(SnapAfter.parseOrDefault(properties.getProperty(SNAP_AFTER)));
			config.setIsCopyToClipboard(isStrTrue(properties.getProperty(COPY_TO_CLIPBOARD)));

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
		updateMap.put(SAVE_PATH, config.getSavePath());
		updateMap.put(SNAP_AREA, config.getSnapArea().getValue());
		updateMap.put(FORMAT, config.getFormatType().getValue());
		updateMap.put(SNAP_AFTER, config.getSnapAfter().getValue());
		updateMap.put(COPY_TO_CLIPBOARD, String.valueOf(config.getIsCopyToClipboard()));
		
		try (BufferedWriter writer = Files.newBufferedWriter(TEMP_PATH, StandardCharsets.UTF_8); // 一時ファイル自動作成
			BufferedReader reader = Files.newBufferedReader(PROPERTIES_PATH, StandardCharsets.UTF_8)) {

			String line;
			while (true) {
				line = reader.readLine();
				if (line == null) {
					// ファイルの最終行でループを抜ける
					break;
				}
				line = stripSafe(line); // 余分なスペース除去
				boolean isMatched = false;

				// マップのキーと値のペアのリストをentrySetで取得し、一つずつentryで受け取る
				for (Map.Entry<String, String> entry : updateMap.entrySet()) {
					String prefix = entry.getKey() + "="; // 先頭文字
					if (line.startsWith(prefix)) {
						// 一致すればその行を書き込み
						writer.write(prefix + entry.getValue());
						isMatched = true;
						break; // この行の置換は完了したため、Mapのループを抜ける
					}
				}

				// どのキーにもマッチしなかった行（コメントや空行）はそのままコピー
				if (!isMatched) {
					writer.write(line);
				}

				writer.newLine(); // 文末改行
			}
			
			// 一時ファイルを元ファイルに置換する
			Files.move(TEMP_PATH, PROPERTIES_PATH, StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			throw new ServiceException("プロパティファイルの書き込みに失敗しました", e);
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
		// osごとの最適な改行コードで統一する
		String content = DEFAULT_CONTENT.replace("\r\n", "\n").replace("\n", System.lineSeparator());
		try {
			// プロパティファイルがなければ作成、あれば文章を上書きする (追記しない)
			Files.writeString(PROPERTIES_PATH, content, StandardCharsets.UTF_8);
			log.info("プロパティファイルを初期化しました");

		} catch (IOException ie) {
			throw new ServiceException("プロパティファイルの初期化に失敗しました", ie);
		}
	}

	// 初期化時の文章をソース側で保持 改行コードを統一
	private static final String DEFAULT_CONTENT = """
			#Setting
			#保存先パスを絶対パスで指定してください
			savePath=C:/Instantsnap画像保存先/

			#撮影範囲の選択
			#1 -> アクティブウィンドウ(デフォルト), 2 -> アクティブモニター
			#3 -> メインモニター, 4 -> 全てのモニター
			snapArea=1

			#拡張子の選択 png、jpg、bmp、gifのいずれかを記載してください
			format=png

			#スクショ発動キーの選択
			#1 -> shift + printscreen(推奨), 2 -> printscreen
			trigger=1

			#撮影後、フォルダ、画像を自動で開く設定です
			#1 -> フォルダ, 2 -> 画像, それ以外 -> 開かない
			openMode=0

			""";
}
