package com.github.rebi0395.instantsnap.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLogger {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(AppLogger.class);

	/**
	 * infoレベルのログを出力します
	 * @param message メッセージ メッセージ内に{}で置換文字をとれる
	 * @param value {}の中に置換する変数、0個以上の引数をとる
	 */
	public static void info(String message, Object... values) {
		log.info(message, values);
	}

	/**
	 * warnレベルのログを出力します
	 * @param message メッセージ
	 * @param value {}の中に置換する変数、0個以上の引数をとる
	 */
	public static void warn(String message, Object... values) {
		log.warn(message, values);
	}

	/**
	 * errorレベルのログを出力します。スタックトレースは最後に指定すること。
	 * @param message メッセージ
	 * @param value {}の中に置換する変数、0個以上の引数をとる
	 */
	public static void error(String message, Object... values) {
		log.error(message, values);
	}

	/**
	 * errorレベルのログを出力します
	 * @param message メッセージ
	 * @param t スタックトレース
	 */
	public static void error(String message, Throwable t) {
		log.error(message, t);
	}
}
