package com.github.rebi0395.instantsnap.util;

import java.io.File;
import java.time.format.DateTimeFormatter;

public class LogWriter {

	private static final String LOG_PATH = "instantSnap.log";
	private static final DateTimeFormatter dtf = 
			DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS");
	
	/**
	 * 情報ログを出力する
	 * @param message
	 */
	public void writeInfo(String message) {
		write("Info", message, null);
	}
	
	/**
	 * 通知ログを出力する
	 * @param message
	 */
	public void writeWarn(String message) {
		
	}
	
	/**
	 * 例外時、エラーログを出力する
	 * @param message
	 * @param e
	 */
	public void writeError(String message,Throwable e) {
		
	}
	
	private static synchronized void write (String level, String message,Throwable e) {
		File file = new File(LOG_PATH);
		
		
		
		
	}

}
