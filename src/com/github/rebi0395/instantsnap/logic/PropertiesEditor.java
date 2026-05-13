package com.github.rebi0395.instantsnap.logic;

import static com.github.rebi0395.instantsnap.util.AppConstant.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import com.github.rebi0395.instantsnap.core.Option;

public class PropertiesEditor {
 	
	/**
	 * コンストラクタ
	 */
	public PropertiesEditor(Option option) {
		propertiesRead(option);
	}
	
	/**
	 * プロパティファイルの読み取りメソッド
	 * @param option
	 */
	public void propertiesRead(Option option) {
		Properties properties = new Properties();
		File file = new File(PROPERTIES_PASS);
		// 文字化け防止のため、UTF-8を指定し、プロパティをセット
		try (InputStream inputstream = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(inputstream, StandardCharsets.UTF_8)){
			properties.load(reader);
			
			// プロパティから読み込み
			option.applySavePath(properties.getProperty(SAVE_PATH));
			option.applyFormat(properties.getProperty(FORMAT));
			option.applyTrigger(properties.getProperty(TRIGGER));
			option.applyOpenMode(properties.getProperty(OPENMODE));

		} catch (IOException ie) {
			System.out.println("プロパティファイルの読み込みに失敗");
		}
	}
	
	/**
	 * プロパティファイルの更新メソッド
	 */
	public void propertiesSave() {
	
	}

}
