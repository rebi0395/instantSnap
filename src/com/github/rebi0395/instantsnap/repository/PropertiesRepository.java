package com.github.rebi0395.instantsnap.repository;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.Option;

/**
 * プロパティファイルの読み取り、書き込みを行い、
 * ユーザー設定の永続化を担保するクラスです
 */
public class PropertiesRepository {

	/** プロパティのファイルパス */
	private static final Path PROPERTIES_PATH = Paths.get("config.properties");

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(PropertiesRepository.class);

	/**
	 * コンストラクタ
	 */
	public PropertiesRepository() {
	}

	/**
	 * プロパティファイルの読み取りを行います
	 * @param option 設定保持クラス
	 * @throws ServiceException 
	 */
	public void read(Option option) throws ServiceException {
		Properties properties = new Properties();

		// プロパティファイルの実在チェックを行い、ない場合はプロパティファイルを生成
		if (!Files.exists(PROPERTIES_PATH)) {
			log.warn("プロパティファイルが見つかりません。再生成を行います");
			reset();
			log.warn("プロパティファイルを再生成しました");
		}

		// UTF-8を明示的に指定し、プロパティをセット
		try (BufferedReader reader = Files.newBufferedReader(PROPERTIES_PATH, StandardCharsets.UTF_8)) {
			properties.load(reader);

			// プロパティから読み取った情報をセット
			option.setSavePath(properties.getProperty(SAVE_PATH));
			option.setSnapMode(properties.getProperty(SNAP_AREA));
			option.setFormat(properties.getProperty(FORMAT));
			option.setTrigger(properties.getProperty(TRIGGER));
			option.setSnapAfter(properties.getProperty(SNAP_AFTER));

		} catch (IOException ie) {
			throw new ServiceException("プロパティファイルの読み込みに失敗しました", ie);
		}
	}

	/**
	 * プロパティファイルの書き込みを行います
	 */
	public void save(Option option) {
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
