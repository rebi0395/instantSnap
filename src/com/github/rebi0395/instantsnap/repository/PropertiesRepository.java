package com.github.rebi0395.instantsnap.repository;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
	public void readProperties(Option option) throws ServiceException {
		Properties properties = new Properties();
		File file = new File(PROPERTIES_PASS);
		// プロパティファイルの実在チェックを行い、ない場合はデフォルト値で適応
		if (!file.exists()) {
			System.out.println("プロパティファイルが存在しません。デフォルト設定を適応します");
			return;
		}
		// 文字化け防止のため、UTF-8を指定し、プロパティをセット
		try (InputStream inputstream = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(inputstream, StandardCharsets.UTF_8)){
			properties.load(reader);
			
			// プロパティから読み込み
			boolean isChangeDefault;
			
			option.setSavePath(properties.getProperty(SAVE_PATH));
			isChangeDefault = option.applySnapMode(properties.getProperty(SNAP_MODE));
			if (isChangeDefault){
				System.out.println("プロパティファイルの記載が不正なため、撮影モードをデフォルトに設定しました");
			}
			isChangeDefault = option.applyFormat(properties.getProperty(FORMAT));
			if (isChangeDefault){
				System.out.println("プロパティファイルの記載が不正なため、拡張子をデフォルトに設定しました");
			}
			option.setTrigger(properties.getProperty(TRIGGER));

		} catch (IOException ie) {
			throw new ServiceException("プロパティファイルの読み込みに失敗しました", ie);
		}
	}
	
	/**
	 * プロパティファイルの書き込みを行います
	 */
	public void saveProperties(Option option) {
	}
}
