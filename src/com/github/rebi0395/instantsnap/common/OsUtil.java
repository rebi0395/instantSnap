package com.github.rebi0395.instantsnap.common;

/**
 * OS判定を行うユーティリティクラス
 */
public final class OsUtil {

	/** システムのOS名 */
	private static final String SYSTEM_OS_NAME = System.getProperty("os.name").toLowerCase();

	/**
	 * インスタンス化防止
	 */
	private OsUtil() {
	}

	/**
	 * Windowsか判定する
	 * @return Windowsの場合true
	 */
	public static boolean isWindows() {
		return SYSTEM_OS_NAME.contains("win");
	}

	/**
	 * macOSか判定する
	 * @return macOSの場合true
	 */
	public static boolean isMac() {
		return SYSTEM_OS_NAME.contains("mac");
	}

	/**
	 * Linuxか判定する
	 * @return Linuxの場合true
	 */
	public static boolean isLinux() {
		return SYSTEM_OS_NAME.contains("linux");
	}
}
