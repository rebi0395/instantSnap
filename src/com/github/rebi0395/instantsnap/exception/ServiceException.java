package com.github.rebi0395.instantsnap.exception;

/**
 * 独自例外クラスです。
 * 下位層で発生したチェック済例外をラップします
 */
public class ServiceException extends Exception {

	/**
	 * コンストラクタ
	 * @param message エラーメッセージ
	 */
	public ServiceException(String message){
		super(message);
	}
	
	/**
	 * コンストラクタ
	 * @param e スタックトレース
	 */
	public ServiceException(Throwable e){
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param message エラーメッセージ
	 * @param e スタックトレース
	 */
	public ServiceException(String message, Throwable e){
		super(message, e);
	}
}
