package com.github.rebi0395.instantsnap.service;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	/** WindowsのUser32 APIにアクセスするためのJNA定数 */
	private static final User32 USER32 = User32.INSTANCE;

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SnapAreaResolver.class);

	/**
	 * 影を除外したアクティブウィンドウの撮影範囲を取得する
	 * (OSがwindowsのみ対応)
	 * @return Rectangle 影を除外したアクティブウィンドウの範囲
	 */
	public Rectangle getActiveWindowArea() {
		String os = System.getProperty("os.name").toLowerCase();
		// Windows以外はデフォルト挙動を行う
		if (!os.contains("win")) {
			log.info("osがwindowsではありません");
			return getDefaultScreenArea();
		}
		// アクティブウィンドウの識別番号を取得する
		HWND hwnd = USER32.GetForegroundWindow();
		if (hwnd == null) {
			log.warn("アクティブウィンドウの識別番号の取得に失敗しました");
			return getDefaultScreenArea();
		}

		// Windowsから影を除外した物理座標を取得する
		Rectangle physRect = getPhysicalWindowRect(hwnd);

		// ウィンドウが存在するモニター（GraphicsDevice）を特定する
		GraphicsDevice activeDevice = findMonitorOfActiveWindow(physRect);
		if (activeDevice == null) {
			return physRect;
		}

		// モニターの解像度情報に合わせてJavaの仮想座標に変換して取得する
		return convertToJavaVirtualBounds(hwnd, physRect, activeDevice);
	}

	/**
	 * Windows APIを叩き、DPIスケーリングを考慮した「影なし物理座標」を計算する
	 * @param hwnd ウィンドウの個別識別情報
	 * @return ウィンドウの影なし座標情報 (X,Y,W,H)
	 */
	public Rectangle getPhysicalWindowRect(HWND hwnd) {

		RECT windowRect = new RECT();
		RECT clientRect = new RECT();
		USER32.GetWindowRect(hwnd, windowRect); // 影付きのRect「X,Y,W,H」を取得する
		USER32.GetClientRect(hwnd, clientRect); // 実サイズのRect「W,H」を取得する

		int totalW = windowRect.right - windowRect.left;
		// 幅(影付き)から幅(影なし)を引き、2で割ることで片側の影を算出する
		int shadowWidth = (totalW - clientRect.right) / 2;

		// ウィンドウ左上を起点としたX,Y,W(幅),H(高さ)を取得する
		int winX = windowRect.left + shadowWidth;
		int winY = windowRect.top;
		int winW = totalW - (shadowWidth * 2);
		int winH = (windowRect.bottom - windowRect.top) - shadowWidth; // 上部は影なしのため引かない

		return new Rectangle(winX, winY, winW, winH);
	}

	/**
	 * 座標の中心点から、該当するモニター（GraphicsDevice）を特定する
	 *
	 * @param rectangle ウィンドウのX,Y,W,H
	 * @return GraphicsDevice ウィンドウの中心が属しているモニター
	 */
	public GraphicsDevice findMonitorOfActiveWindow(Rectangle rectangle) {
		int centerX = rectangle.x + (rectangle.width / 2);
		int centerY = rectangle.y + (rectangle.height / 2);

		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			for (GraphicsDevice device : ge.getScreenDevices()) {
				if (device.getDefaultConfiguration().getBounds().contains(centerX, centerY)) {
					return device;
				}
			}
			if (ge.getScreenDevices().length > 0) {
				return ge.getScreenDevices()[0];
			}
		} catch (Exception e) {
			log.warn("アクティブウィンドウの属するモニターの特定に失敗しました", e);
		}
		return null;
	}

	/**
	 * Windowsの物理座標を、Javaの仮想画面座標系へスケーリング変換し、画面外をカットする
	 * @param hwnd ウィンドウの識別番号
	 * @param physRect 影なしのウィンドウの座標情報(X,Y,W,H)
	 * @param device 取得したアクティブモニター
	 * @return Javaが管理する等倍の仮想モニターの座標に変換した、アクティブウィンドウの座標
	 */
	public Rectangle convertToJavaVirtualBounds(HWND hwnd, Rectangle physRect, GraphicsDevice device) {
		Rectangle deviceBounds = device.getDefaultConfiguration().getBounds(); // モニター自体の座標取得 
		try {
			// ウィンドウが最も被っているモニターの識別子を取得する
			HMONITOR hMonitor = USER32.MonitorFromWindow(hwnd, WinUser.MONITOR_DEFAULTTONEAREST);

			// osからモニターの配置、解像度情報を取得する
			MONITORINFOEX info = new MONITORINFOEX();
			USER32.GetMonitorInfo(hMonitor, info);

			// OSが認識する幅と高さを算出する
			int osMonitorW = info.rcMonitor.right - info.rcMonitor.left;
			int osMonitorH = info.rcMonitor.bottom - info.rcMonitor.top;

			// Java側の論理幅 / OS側の物理幅 を行い、スケーリング比率(拡大率)を算出する
			double scaleX = (double) deviceBounds.width / osMonitorW;
			double scaleY = (double) deviceBounds.height / osMonitorH;

			// ウィンドウの物理座標から、モニター内部の物理的な端を引くことで、
			// モニター左上を(0,0)とした時のX,Yを取得
			int relativeX = physRect.x - info.rcMonitor.left;
			int relativeY = physRect.y - info.rcMonitor.top;

			// 以下計算式を用いて、Javaが管理する等倍の仮想モニターの座標に変換する
			// モニター自体の座標 + (四捨五入) 座標 * スケーリング
			int x = deviceBounds.x + (int) Math.round(relativeX * scaleX);
			int y = deviceBounds.y + (int) Math.round(relativeY * scaleY);
			int w = (int) Math.round(physRect.width * scaleX);
			int h = (int) Math.round(physRect.height * scaleY);

			Rectangle finalResult = new Rectangle(x, y, w, h);
			return finalResult.intersection(deviceBounds); // 画面外の黒領域をカットする

		} catch (Throwable t) {
			log.warn("Windowsの物理座標を、Javaの仮想画面座標系へスケーリング変換処理が失敗しました", t);
			return physRect; // 失敗時は物理座標のまま返却する
		}
	}

	/**
	 * ポインターの位置からアクティブモニターを特定し、撮影範囲を取得する
	 * @return アクティブモニターの撮影範囲
	 */
	public Rectangle getActiveScreenArea() {
		PointerInfo pointer = MouseInfo.getPointerInfo();
		if (pointer == null || pointer.getDevice() == null) {
			log.warn("マウスポインターの位置からモニターを特定できませんでした");
			return getMainScreenArea();
		}
		GraphicsDevice activeDevice = pointer.getDevice();
		return activeDevice.getDefaultConfiguration().getBounds();
	}

	/**
	 * メインに設定しているモニターの範囲を取得する
	 * @return メインモニターの撮影範囲
	 */
	public Rectangle getMainScreenArea() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice mainDevice = ge.getDefaultScreenDevice();
		return mainDevice.getDefaultConfiguration().getBounds();

	}

	/**
	 * 
	 * @return マルチモニター込みの撮影範囲
	 */
	public Rectangle getFullScreenArea() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screens = ge.getScreenDevices();

		Rectangle allRectangle = screens[0].getDefaultConfiguration().getBounds();
		for (GraphicsDevice screen : screens) {
			allRectangle = allRectangle.union(screen.getDefaultConfiguration().getBounds());
		}
		return allRectangle;
	}

	/**
	 * プロパティファイルの設定値が不正のときのデフォルト処理
	 * @return アクティブモニターの撮影範囲
	 */
	public Rectangle getDefaultScreenArea() {
		log.info("撮影範囲をデフォルト値で設定しました");
		return getActiveScreenArea();
	}
}
