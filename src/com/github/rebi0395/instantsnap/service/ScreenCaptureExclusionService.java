package com.github.rebi0395.instantsnap.service;

import java.awt.Window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.OsUtil;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * InstantSnapが所有するウィンドウの画面キャプチャ可否を切り替えるサービスクラス
 * Windows以外ではOSの標準動作を維持するため、何も行わない
 */
public final class ScreenCaptureExclusionService {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(ScreenCaptureExclusionService.class);

	/** 通常どおり画面キャプチャへ表示する設定値 */
	private static final int WDA_NONE = 0x00000000;

	/** 画面キャプチャからウィンドウを除外する設定値 */
	private static final int WDA_EXCLUDEFROMCAPTURE = 0x00000011;

	/** Windows APIから取得するウィンドウタイトルの最大文字数 */
	private static final int WINDOW_TITLE_BUFFER_SIZE = 512;

	/** 現在のJavaプロセスID */
	private final int currentProcessId;

	/**
	 * 現在のJavaプロセスを対象にするサービスを生成します。
	 */
	public ScreenCaptureExclusionService() {
		this.currentProcessId = (int) ProcessHandle.current().pid();
	}

	/**
	 * SwingまたはAWTのウィンドウについて、画面キャプチャからの除外状態を設定します。
	 * ネイティブウィンドウが未生成の場合は適用せず、画面表示後の再呼び出しに委ねます。
	 *
	 * @param window 設定対象のウィンドウ
	 * @param excluded 撮影対象から除外する場合true、通常どおり含める場合false
	 */
	public void setExcluded(Window window, boolean excluded) {
		if (!OsUtil.isWindows() || window == null || !window.isDisplayable()) {
			return;
		}

		try {
			Pointer windowPointer = Native.getComponentPointer(window);
			HWND windowHandle = new HWND(windowPointer);
			setExcluded(windowHandle, excluded, window.getName());
		} catch (RuntimeException | LinkageError e) {
			log.warn("ウィンドウの撮影除外設定を変更できませんでした。", e);
		}
	}

	/**
	 * 現在のプロセスが所有する指定タイトルのネイティブウィンドウについて、
	 * 画面キャプチャからの除外状態を設定します。
	 * JavaFXのStageはAWTコンポーネントではないため、プロセスIDとタイトルから特定します。
	 *
	 * @param windowTitle 設定対象のウィンドウタイトル
	 * @param excluded 撮影対象から除外する場合true、通常どおり含める場合false
	 */
	public void setCurrentProcessWindowExcluded(String windowTitle, boolean excluded) {
		if (!OsUtil.isWindows() || windowTitle == null || windowTitle.isBlank()) {
			return;
		}

		try {
			HWND windowHandle = findCurrentProcessWindow(windowTitle);
			if (windowHandle == null) {
				log.warn("撮影除外設定の対象ウィンドウが見つかりませんでした。title={}", windowTitle);
				return;
			}
			setExcluded(windowHandle, excluded, windowTitle);
		} catch (RuntimeException | LinkageError e) {
			log.warn("ウィンドウの撮影除外設定を変更できませんでした。title={}", windowTitle, e);
		}
	}

	/**
	 * 現在のプロセスが所有する、指定タイトルのトップレベルウィンドウを検索します。
	 *
	 * @param windowTitle 検索するウィンドウタイトル
	 * @return 見つかったウィンドウハンドル。見つからない場合はnull
	 */
	private HWND findCurrentProcessWindow(String windowTitle) {
		HWND[] foundWindow = new HWND[1];
		WindowsApi.USER32.EnumWindows(new WinUser.WNDENUMPROC() {
			@Override
			public boolean callback(HWND windowHandle, Pointer data) {
				if (!belongsToCurrentProcess(windowHandle)) {
					return true;
				}

				char[] titleBuffer = new char[WINDOW_TITLE_BUFFER_SIZE];
				WindowsApi.USER32.GetWindowText(windowHandle, titleBuffer, titleBuffer.length);
				String currentTitle = Native.toString(titleBuffer);
				if (!windowTitle.equals(currentTitle)) {
					return true;
				}

				foundWindow[0] = windowHandle;
				return false;
			}
		}, null);
		return foundWindow[0];
	}

	/**
	 * 指定ウィンドウが現在のJavaプロセスに属しているか判定します。
	 *
	 * @param windowHandle 判定対象のウィンドウハンドル
	 * @return 現在のプロセスに属する場合true
	 */
	private boolean belongsToCurrentProcess(HWND windowHandle) {
		IntByReference processId = new IntByReference();
		WindowsApi.USER32.GetWindowThreadProcessId(windowHandle, processId);
		return processId.getValue() == currentProcessId;
	}

	/**
	 * Windowsのウィンドウハンドルへ画面キャプチャの表示方法を設定します。
	 *
	 * @param windowHandle 設定対象のウィンドウハンドル
	 * @param excluded 撮影対象から除外する場合true
	 * @param windowName ログに出力するウィンドウ名
	 */
	private void setExcluded(HWND windowHandle, boolean excluded, String windowName) {
		int affinity = WDA_NONE;
		if (excluded) {
			affinity = WDA_EXCLUDEFROMCAPTURE;
		}

		BOOL result = User32Ex.INSTANCE.SetWindowDisplayAffinity(windowHandle, affinity);
		if (result == null || !result.booleanValue()) {
			log.warn(
					"ウィンドウの撮影除外設定を変更できませんでした。window={}, excluded={}, errorCode={}",
					windowName,
					excluded,
					Native.getLastError());
		}
	}

	/**
	 * Windows以外でUser32のロードが発生しないよう、Windows APIを遅延初期化します。
	 */
	private static class WindowsApi {

		/** WindowsのUser32 API */
		private static final User32 USER32 = User32.INSTANCE;
	}

	/**
	 * JNAを介して呼び出すWindowsのUser32 APIです。
	 */
	private interface User32Ex extends StdCallLibrary {

		/** User32 APIを呼び出すためのインスタンス */
		User32Ex INSTANCE = Native.load(
				"user32",
				User32Ex.class,
				W32APIOptions.DEFAULT_OPTIONS);

		/**
		 * 指定したウィンドウの画面キャプチャに関する表示方法を設定します。
		 *
		 * @param window 設定対象のウィンドウハンドル
		 * @param affinity 画面キャプチャ時の表示方法
		 * @return 設定に成功した場合はTRUE、失敗した場合はFALSE
		 */
		BOOL SetWindowDisplayAffinity(HWND window, int affinity);
	}
}
