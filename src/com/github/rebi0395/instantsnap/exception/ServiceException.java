package com.github.rebi0395.instantsnap.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 独自例外クラスです。
 * チェック済例外に対してログの出力などを行います。
 */
public class ServiceException extends Exception {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(ServiceException.class);
	
	/**
	 * コンストラクタ
	 * @param message エラーメッセージ
	 */
	public ServiceException(String message){
		super(message);
		log.error(message);
	}

	/**
	 * コンストラクタ
	 * @param message エラーメッセージ
	 * @param e スタックトレース
	 */
	public ServiceException(String message, Throwable e){
		super(message, e);
		log.error(message, e);
	}
}
