package com.github.rebi0395.instantsnap.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLogger {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(AppLogger.class);

	public static void info(String message, Object... args) {
		log.info(message, args);
	}

	public static void warn(String message, Object... args) {
		log.warn(message, args);
	}

	public static void error(String message, Object... args) {
		log.error(message, args);
	}
}
