package com.github.rebi0395.instantsnap.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.model.enums.MessageEnum;

import static com.github.rebi0395.instantsnap.common.MessageKeyConstant.*;

/**
 * ユーザー設定の言語で、メッセージキーをメッセージに変換するクラス
 */
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
	 * 
	 * @param messageKey メッセージキー
	 * @return メッセージで言語を変換したメッセージ
	 */
	public String getMessage(String messageKey) {
		try {
			return bundle.getString(messageKey);
		} catch (MissingResourceException e) {
			log.warn("メッセージのリソースが見つかりません" ,e);
			return messageKey;
		}
	}
	
	/**
	 * メッセージキーから、言語別のメッセージを取得する
	 * 
	 * @param messageKey メッセージキー
	 * @param defaultMessage メッセージキーが取得できないときに返す文章
	 * @return メッセージで言語を変換したメッセージ
	 */
	public String getMessage(String messageKey, String defaultMessage) {
		try {
			return bundle.getString(messageKey);
		} catch (MissingResourceException e) {
			log.warn("メッセージのリソースが見つかりません" ,e);
			return defaultMessage;
		}
	}
	
	/**
	 * メッセージキーから、言語別のメッセージを取得する
	 * @param messageKey メッセージキーを持つEnum（ポリモーフィズム）
	 * @return メッセージで言語を変換したメッセージ
	 */
	public String getMessage(MessageEnum messageEnum) {
		try {
			return bundle.getString(messageEnum.getMessageKey());
		} catch (MissingResourceException e) {
			log.warn("メッセージのリソースが見つかりません" ,e);
			return messageEnum.getBaseDisplayName();
		}
	}

	/**
	 * メッセージキーリストから、言語翻訳したメッセージリストを取得する
	 * @param messageKeyList メッセージキーリスト
	 * @return 翻訳済のメッセージリスト
	 */
	public List<String> getMessageList(List<String> messageKeyList) {
		List <String> messageList = new ArrayList<String>();
		// メッセージキーリストの件数分、ループで翻訳する
		for (String messagekey : messageKeyList) {
			String message = getMessage(messagekey);
			messageList.add(message);
		}
		return messageKeyList;
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
