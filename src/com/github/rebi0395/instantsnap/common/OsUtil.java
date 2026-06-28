package com.github.rebi0395.instantsnap.common;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

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
	 * OS名を取得する
	 * @return OS名
	 */
	public static String getOsName() {
		if (isWindows()) {
			return OS_NAME_WINDOWS;
		}

		if (isMac()) {
			return OS_NAME_MAC;
		}

		if (isLinux()) {
			return OS_NAME_LINUX;
		}

		return OS_NAME_OTHER;
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
