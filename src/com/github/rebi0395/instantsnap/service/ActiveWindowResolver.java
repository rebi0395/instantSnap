package com.github.rebi0395.instantsnap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.OsUtil;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * Windowsでアクティブウィンドウ撮影の対象を解決するクラスです。
 * 常時監視は行わず、撮影要求を受けた時点のフォアグラウンドウィンドウと
 * WindowsのZオーダーから、実際に撮影するウィンドウを特定します。
 * Zオーダーとは画面上のウィンドウの前後関係で、先頭ほど手前に表示されています。
 */
public final class ActiveWindowResolver {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(ActiveWindowResolver.class);

	/** ウィンドウが存在しないことを示す値 */
	private static final long NO_WINDOW_HANDLE = 0L;

	/** 常駐プロセス自身のプロセスID */
	private final int residentProcessId;

	/** JavaFX設定画面を実行中の子プロセスID。未起動時は0 */
	private volatile int settingsProcessId;

	/**
	 * 現在のJavaプロセスをInstantSnap常駐プロセスとして解決クラスを生成します。
	 */
	public ActiveWindowResolver() {
		this.residentProcessId = (int) ProcessHandle.current().pid();
	}

	/**
	 * JavaFX設定画面のプロセスをInstantSnapの内部ウィンドウとして登録します。
	 *
	 * @param processId 起動した設定プロセスのID
	 */
	public void registerSettingsProcess(long processId) {
		this.settingsProcessId = (int) processId;
	}

	/**
	 * 終了したJavaFX設定画面のプロセスIDを内部ウィンドウの一覧から解除します。
	 * 新しい設定プロセスへ既に切り替わっている場合は、そのIDを維持します。
	 *
	 * @param processId 終了した設定プロセスのID
	 */
	public void unregisterSettingsProcess(long processId) {
		int finishedProcessId = (int) processId;
		if (this.settingsProcessId == finishedProcessId) {
			this.settingsProcessId = 0;
		}
	}

	/**
	 * 撮影対象のアクティブウィンドウのネイティブハンドルを返します。
	 * 見つからない場合は、Zオーダー上で直後にあるウィンドウが取得できなくなるまで、
	 * 撮影対象になるウィンドウを探します。
	 *
	 * @param isIncludeInstantSnapWindow このアプリのウィンドウも撮影対象に含める場合はtrue
	 * @param isFromMainWindow 撮影元がメインウィンドウのパネルからであればtrueを指定
	 * @return 撮影ウィンドウのネイティブハンドル。見つからない場合は0
	 */
	public long getForegroundWindowHandle(boolean isIncludeInstantSnapWindow, boolean isFromMainWindow) {
		if (!OsUtil.isWindows()) {
			return NO_WINDOW_HANDLE;
		}

		try {
			// 前面のウィンドウを取得
			HWND foregroundWindow = WindowsApi.USER32.GetForegroundWindow();
			// ウィンドウが存在しない場合は、ループを抜けて0を返す
			while (foregroundWindow != null) {
				// アプリのウィンドウを含める場合
				if (isIncludeInstantSnapWindow) {
					// メインウィンドウからの撮影要求では、メインウィンドウは常に除外
					if (isFromMainWindow && isUsableCaptureWindow(foregroundWindow, residentProcessId)) {
						return toWindowHandleValue(foregroundWindow);
					}
					// ショートカット撮影の場合は、除外対象なし
					if (!isFromMainWindow && isUsableCaptureWindow(foregroundWindow)) {
						return toWindowHandleValue(foregroundWindow);
					}
				} else {
					// InstantSnapのウィンドウを除外する
					if (isUsableCaptureWindow(foregroundWindow, residentProcessId, settingsProcessId)) {
						return toWindowHandleValue(foregroundWindow);
					}
				}

				/*
				 * 次のウィンドウに上書きし、次ループに移行する
				 * GW_HWNDNEXTは、現在のウィンドウよりZオーダーが一つ後ろのHWNDを返す。
				 */
				foregroundWindow = User32Ex.INSTANCE.GetWindow(foregroundWindow, User32Ex.GW_HWNDNEXT);
			}
			return NO_WINDOW_HANDLE; // 見つからなかった場合

		} catch (RuntimeException | LinkageError e) {
			log.warn("アクティブウィンドウを取得できませんでした。", e);
			return NO_WINDOW_HANDLE;
		}
	}

	/**
	 * 指定ウィンドウが、撮影対象として利用できるか判定します。
	 *
	 * @param windowHandle 判定するウィンドウハンドル
	 * @param excludedProcessIds 0個以上指定できる、撮影対象の除外プロセスID
	 * @return 撮影可能なウィンドウの場合true
	 */
	private boolean isUsableCaptureWindow(HWND windowHandle, int... excludedProcessIds) {
		// Zオーダーには撮影できないシステム用ウィンドウも含まれるため、順番に除外する。
		if (windowHandle == null || windowHandle.getPointer() == null) {
			return false;
		}
		// 最小化している場合は除外する
		if (!WindowsApi.USER32.IsWindow(windowHandle)
				|| !WindowsApi.USER32.IsWindowVisible(windowHandle)
				|| isMinimized(windowHandle)) {
			return false;
		}
		if (WindowsApi.USER32.GetWindowTextLength(windowHandle) <= 0) {
			return false;
		}
		int processId = getProcessId(windowHandle);

		// プロセスIDが0の場合は、ウィンドウが存在しないか、所有者不明のため除外する
		if (processId == 0) {
			return false;
		}
		// 引数に除外対象のプロセスIDが指定されている場合は、そのプロセスIDを持つウィンドウを除外する
		for (int excludedProcessId : excludedProcessIds) {
			if (processId == excludedProcessId) {
				return false;
			}
		}
		// 撮影対象として利用可能とする
		return true;
	}

	/**
	 * 指定ウィンドウを所有するプロセスIDを取得します。
	 *
	 * @param windowHandle 所有者を確認するウィンドウハンドル
	 * @return ウィンドウを所有するプロセスID。取得できない場合は0
	 */
	private int getProcessId(HWND windowHandle) {
		IntByReference processId = new IntByReference();
		WindowsApi.USER32.GetWindowThreadProcessId(windowHandle, processId);
		return processId.getValue();
	}

	/**
	 * JNAのHWNDを、非同期撮影処理へ安全に渡せる数値へ変換します。
	 *
	 * @param windowHandle 変換するウィンドウハンドル
	 * @return 数値化したウィンドウハンドル
	 */
	private long toWindowHandleValue(HWND windowHandle) {
		return Pointer.nativeValue(windowHandle.getPointer());
	}

	/**
	 * 指定ウィンドウが最小化されているかWindows APIから取得します。
	 *
	 * @param windowHandle 判定するウィンドウハンドル
	 * @return 最小化されている場合true
	 */
	private boolean isMinimized(HWND windowHandle) {
		BOOL result = User32Ex.INSTANCE.IsIconic(windowHandle);
		return result != null && result.booleanValue();
	}

	/**
	 * Windows以外でUser32のロードが発生しないよう、Windows APIを遅延初期化します。
	 */
	private static class WindowsApi {

		/** WindowsのUser32 API */
		private static final User32 USER32 = User32.INSTANCE;
	}

	/**
	 * JNA標準のUser32に定義されていないWindows APIを呼び出すインターフェースです。
	 */
	private interface User32Ex extends StdCallLibrary {
		/** Zオーダーを画面の手前から奥へ一つ進めるための値 */
		int GW_HWNDNEXT = 2;

		/** User32 APIを呼び出すためのインスタンス */
		User32Ex INSTANCE = Native.load(
				"user32",
				User32Ex.class,
				W32APIOptions.DEFAULT_OPTIONS);

		/**
		 * 指定したウィンドウが最小化されているか判定します。
		 *
		 * @param window 判定するウィンドウハンドル
		 * @return 最小化されている場合はTRUE
		 */
		BOOL IsIconic(HWND window);

		/**
		 * Zオーダー上で指定ウィンドウと関係するウィンドウを取得します。
		 *
		 * @param window 探索の基準となるウィンドウ
		 * @param command 取得する関係を示す値
		 * @return 条件に該当するウィンドウ。存在しない場合はnull
		 */
		HWND GetWindow(HWND window, int command);
	}
}
