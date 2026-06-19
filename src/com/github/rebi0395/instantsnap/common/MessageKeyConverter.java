package com.github.rebi0395.instantsnap.common;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import static com.github.rebi0395.instantsnap.common.MessageKeyConstant.*;

public class MessageKeyConverter {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(MessageKeyConverter.class);
		
	/** リソースバンドル */
	private ResourceBundle bundle;
	
	/** コンストラクタ */
	public MessageKeyConverter(LanguageType languageType) {
		changeLanguage(languageType);
	}
	
	/**
	 * ユーザーの言語設定から、リソースバンドルを取得する
	 * @param languageType ユーザーの言語設定
	 */
	public void changeLanguage(LanguageType languageType) {
		Locale locale = languageType.getLocale();
		bundle = ResourceBundle.getBundle(BASE_NAME, locale);
	}
	
	/**
	 * メッセージキーから、言語別のメッセージを取得する
	 * @param messageKey メッセージキー
	 * @return メッセージで言語を変換したメッセージ
	 */
	public String getMessage(String messageKey) {
		try {
			return bundle.getString(messageKey);	
		} catch (MissingResourceException e) {
			return messageKey;
		}
	}

	/**
	 * 設定値の有効、無効のメッセージを多言語対応して返す
	 * @param enabled 設定が有効ならtrue / 無効でfalse
	 * @return 翻訳されたラベルメッセージ true: 有効 / false: 無効 
	 */
	public String getEnabledMessage(boolean enabled) {
		if (enabled) {
			return getMessage(LABEL_ENABLED);
			
		} else {
			return getMessage(LABEL_DISABLED);
		}
	}
}
