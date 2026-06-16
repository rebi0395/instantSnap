package com.github.rebi0395.instantsnap.common;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.model.enums.LanguageType;

public class MessageManager {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(MessageManager.class);
	
	/** 言語別プロパティファイルの格納先 */
	private static final String BASE_NAME = "i18n.messages";
	
	/** リソースバンドル */
	private ResourceBundle bundle;
	
	/** コンストラクタ */
	public MessageManager(LanguageType languageType) {
		changeLanguage(languageType);
	}
	
	// -- メッセージキー定数 -- 
 	/** ラベルの有効を表示するメッセージキー */
	public static final String LABEL_ENABLED = "label.enabled";
	/** ラベルで無効を表示するメッセージキー */
	public static final String LABEL_DISABLED = "label.disabled";
	
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
