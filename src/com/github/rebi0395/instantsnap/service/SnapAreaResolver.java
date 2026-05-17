package com.github.rebi0395.instantsnap.service;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;

import com.github.rebi0395.instantsnap.model.SnapMode;
import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HMONITOR;
import com.sun.jna.platform.win32.WinUser.MONITORINFOEX;

/**
 * スクリーンショットの撮影範囲を特定するクラス
 */
public class SnapAreaResolver {
	// フィールド（定数）として最初に一発定義しておく
    private static final User32 USER32 = User32.INSTANCE;
	
	public Rectangle resolve(SnapMode mode) {	   
		switch (mode) {
			case ACTIVE_WINDOW:
				return getActiveWindowArea();
			case ACTIVE_SCREEN:
				return getActiveScreenArea();
			case FULL_SCREEN:
				return getFullScreenArea();
			default:
				return getActiveScreenArea();
		}
    }

	/**
	 * アクティブウィンドウの撮影範囲を取得するメソッド
	 * 現在はwindowsのみ対応
	 * @return Rectangle 影を除外したウィンドウの範囲
	 */
	private Rectangle getActiveWindowArea() {
		String os = System.getProperty("os.name").toLowerCase();
		// Windowsのみ対応
		if (!os.contains("win")) {
			return getActiveScreenArea();
		}
		HWND hwnd = USER32.GetForegroundWindow();
		if (hwnd == null) {
			return getActiveScreenArea();
		}

		// 1. Windowsから影を除外した物理座標を取得
		Rectangle physRect = getPhysicalWindowRectWithShadowRaw(hwnd);

		// 2. ウィンドウが存在するモニター（GraphicsDevice）を特定
		int centerX = physRect.x + (physRect.width / 2);
		int centerY = physRect.y + (physRect.height / 2);
		GraphicsDevice currentDevice = findGraphicsDevice(centerX, centerY);
		if (currentDevice == null) {
			return physRect;
		}

		// 3. モニターの解像度情報に合わせてJavaの仮想座標に変換して返す
		return convertToJavaVirtualBounds(hwnd, physRect, currentDevice);
	}

	/**
	 * Windows APIを叩き、DPIスケーリングを考慮した「影なし物理座標」を計算する
	 */
	private Rectangle getPhysicalWindowRectWithShadowRaw(HWND hwnd) {
		try {
			// Windows 10以降で有効な「モニター個別DPI認識モード（V2）」を表すマジックナンバー
			long PER_MONITOR_AWARE_V2 = -4L;
			Pointer pointer = new Pointer(PER_MONITOR_AWARE_V2);
			Function.getFunction("user32", "SetProcessDpiAwarenessContext")
			                    .invoke(new Object[]{ pointer });
		} catch (Throwable t) {
			// エラー時は無視して進行
		}

		RECT windowRect = new RECT();
		RECT clientRect = new RECT();
		USER32.GetWindowRect(hwnd, windowRect);
		USER32.GetClientRect(hwnd, clientRect);

		int totalW = windowRect.right - windowRect.left;
		int shadowWidth = (totalW - clientRect.right) / 2;

		int winX = windowRect.left + shadowWidth;
		int winY = windowRect.top;
		int winW = totalW - (shadowWidth * 2);
		int winH = (windowRect.bottom - windowRect.top) - shadowWidth;

		return new Rectangle(winX, winY, winW, winH);
	}

	/**
	 * 座標の中心点から、該当するモニター（GraphicsDevice）を探索する
	 */
	private GraphicsDevice findGraphicsDevice(int centerX, int centerY) {
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			for (GraphicsDevice gd : ge.getScreenDevices()) {
				if (gd.getDefaultConfiguration().getBounds().contains(centerX, centerY)) {
					return gd;
				}
			}
			if (ge.getScreenDevices().length > 0) {
				return ge.getScreenDevices()[0];
			}
		} catch (Exception e) {
			// 取得失敗
		}
		return null;
	}

	/**
	 * Windowsの物理座標を、Javaの仮想画面座標系へスケーリング変換し、画面外をカットする
	 */
	private Rectangle convertToJavaVirtualBounds(HWND hwnd, Rectangle physRect, GraphicsDevice device) {
		Rectangle deviceBounds = device.getDefaultConfiguration().getBounds();
		try {
			HMONITOR hMonitor = USER32.MonitorFromWindow(hwnd, WinUser.MONITOR_DEFAULTTONEAREST);
			
			MONITORINFOEX info = new MONITORINFOEX();
			USER32.GetMonitorInfo(hMonitor, info);
			
			int osMonitorW = info.rcMonitor.right - info.rcMonitor.left;
			int osMonitorH = info.rcMonitor.bottom - info.rcMonitor.top;
			
			double scaleX = (double) deviceBounds.width / osMonitorW;
			double scaleY = (double) deviceBounds.height / osMonitorH;
			
			int relativeX = physRect.x - info.rcMonitor.left;
			int relativeY = physRect.y - info.rcMonitor.top;
			
			int x = deviceBounds.x + (int) Math.round(relativeX * scaleX);
			int y = deviceBounds.y + (int) Math.round(relativeY * scaleY);
			int w = (int) Math.round(physRect.width * scaleX);
			int h = (int) Math.round(physRect.height * scaleY);
			
			Rectangle finalResult = new Rectangle(x, y, w, h);
			return finalResult.intersection(deviceBounds); // 画面外の黒領域カット
			
		} catch (Throwable t) {
			// 失敗時は物理座標のままフォールバック
		}
		return physRect;
	}

	private Rectangle getActiveScreenArea() {
		PointerInfo pi = MouseInfo.getPointerInfo();
		GraphicsDevice gd = pi.getDevice();
		return gd.getDefaultConfiguration().getBounds();
	}

	private Rectangle getFullScreenArea() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screens = ge.getScreenDevices();

		Rectangle all = new Rectangle();
		for (GraphicsDevice screen : screens) {
			all = all.union(screen.getDefaultConfiguration().getBounds());
		}
		return all;
	}
}
