package com.github.rebi0395.instantsnap.exception;

/**
 * 独自例外クラスです。
 * チェック済例外に対してログの出力などを行います。
 */
public class ServiceException extends Exception {

	public ServiceException(String message){
		super(message);
	}
	
	public ServiceException(String message, Throwable e){
		super(message, e);
	}
}
