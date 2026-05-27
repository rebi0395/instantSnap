package com.github.rebi0395.instantsnap.repository;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.enums.FormatType;
import com.github.rebi0395.instantsnap.model.enums.SnapAfter;
import com.github.rebi0395.instantsnap.model.enums.SnapArea;

/**
 * プロパティファイルの読み取り、書き込みを行い、
 * ユーザー設定の永続化を担保するクラスです
 */
public class PropertiesRepository {

	/** プロパティのファイルパス */
	private static final Path PROPERTIES_PATH = Paths.get("config.properties");

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(PropertiesRepository.class);
	
	/** プロパティを読み取るビルダー */
	private final FileBasedConfigurationBuilder<PropertiesConfiguration> builder; 
	
	/**
	 * コンストラクタ
	 */
	public PropertiesRepository() {
		this.builder = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class); 
		// ビルダーの設定を渡すパラメータクラス
		Parameters param = new Parameters();
		// メソッドチェーンでファイルパスと文字コードを設定
		builder.configure(param.properties().setFile(PROPERTIES_PATH.toFile()).setEncoding("UTF-8"));
	}

	/**
	 * プロパティファイルの読み取りを行います
	 * @param config 設定保持クラス
	 * @throws ServiceException 
	 */
	public void read(ConfigBean config) throws ServiceException {
		// プロパティファイルの実在チェックを行い、ない場合はプロパティファイルを生成
		if (!Files.exists(PROPERTIES_PATH)) {
			log.warn("プロパティファイルが見つかりません。再生成を行います");
			reset();
			log.warn("プロパティファイルを再生成しました");
		}

		try {
			// プロパティファイルをビルダーで読み取る(オブジェクトのためtry不要)
			PropertiesConfiguration properties = builder.getConfiguration();
			// プロパティから読み取った情報をセット
			config.setSavePath(properties.getString(SAVE_PATH));
			config.setSnapArea(SnapArea.parseOrDefault(properties.getString(SNAP_AREA)));
			config.setFormatType(FormatType.parseOrDefault(properties.getString(FORMAT)));
			config.setTrigger(properties.getString(TRIGGER));
			config.setSnapAfter(SnapAfter.parseOrDefault(properties.getString(SNAP_AFTER)));
			config.setIsCopyToClipboard(properties.getBoolean(COPY_TO_CLIPBOARD, false)); // null時false

		} catch (ConfigurationException ie) {
			throw new ServiceException("プロパティファイルの読み込みに失敗しました", ie);
		}
	}

	/**
	 * プロパティファイルの書き込みを行います
	 */
	public void save(ConfigBean config) throws ServiceException {
		Properties properties = new Properties();

		// プロパティファイルの実在チェックを行い、ない場合はプロパティファイルを生成
		if (!Files.exists(PROPERTIES_PATH)) {
			log.warn("プロパティファイルが見つかりません。再生成を行います");
			reset();
			log.warn("プロパティファイルを再生成しました");
		}
		
		// Config → Properties へ変換
		properties.setProperty(SAVE_PATH, config.getSavePath());
		properties.setProperty(SNAP_AREA, config.getSnapArea().getValue());
		properties.setProperty(FORMAT, config.getFormatType().getValue());
		properties.setProperty(SNAP_AFTER, config.getSnapAfter().getValue());
		properties.setProperty(COPY_TO_CLIPBOARD, String.valueOf(config.getIsCopyToClipboard()));

		//TODO startWithを使って置換
		
        try (Writer writer = Files.newBufferedWriter(PROPERTIES_PATH, StandardCharsets.UTF_8)) {
        	properties.store(writer, "InstantSnap Settings");
        } catch (IOException e) {
            throw new ServiceException("プロパティファイルの書き込みに失敗しました", e);
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
