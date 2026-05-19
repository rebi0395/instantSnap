package com.github.rebi0395.instantsnap.repository;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.Option;

/**
 * プロパティファイルの読み取り、書き込みを行い、
 * ユーザー設定の永続化を担保するクラスです
 */
public class PropertiesRepository {
 	
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
		Path propetiesPath = Paths.get(PROPERTIES_PASS);
		
		//File file = new File(PROPERTIES_PASS);
		
		// プロパティファイルの実在チェックを行い、ない場合は生成
		if (!Files.exists(propetiesPath)) {
			System.out.println("プロパティファイルが存在しません。"
					+ "初期化したプロパティファイルを生成しました");
		}

		// UTF-8を明示的に指定し、プロパティをセット
		try (BufferedReader reader = Files.newBufferedReader(propetiesPath, StandardCharsets.UTF_8)){
			properties.load(reader);
			
			// プロパティから読み取った情報をセット
			option.setSavePath(properties.getProperty(SAVE_PATH));
			option.setSnapMode(properties.getProperty(SNAP_MODE));
			option.setFormat(properties.getProperty(FORMAT));
			option.setTrigger(properties.getProperty(TRIGGER));

		} catch (IOException ie) {
			throw new ServiceException("プロパティファイルの読み込みに失敗しました", ie);
		}
	}
	
	/**
	 * プロパティファイルの書き込みを行います
	 */
	public void save(Option option) {
	}
	
	
	
	
	
}
