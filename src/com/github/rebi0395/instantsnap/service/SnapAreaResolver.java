package com.github.rebi0395.instantsnap.service;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.OsUtil;
import com.github.rebi0395.instantsnap.model.enums.SnapAreaType;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HMONITOR;
import com.sun.jna.platform.win32.WinUser.MONITORINFOEX;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * スクリーンショットの撮影範囲を特定するクラス
 */
public class SnapAreaResolver {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SnapAreaResolver.class);

	/**
	 * 列挙型に対応する撮影範囲の座標を返却する
	 * @param snapAreaType 撮影範囲の種別
	 * @return 撮影範囲の座標
	 */
	public Rectangle resolveArea(SnapAreaType snapAreaType) {
		switch (snapAreaType) {
		case ACTIVE_WINDOW:
			return getActiveWindowArea();

		case ACTIVE_SCREEN:
			return getActiveScreenArea();

		case MAIN_SCREEN:
			return getMainScreenArea();

		case FULL_SCREEN:
			return getFullScreenArea();

		default:
			return getDefaultScreenArea();
		}
	}

	/**
	 * 影を除外したアクティブウィンドウの撮影範囲を取得する
	 * (OSがwindowsのみ対応)
	 * @return Rectangle 影を除外したアクティブウィンドウの範囲
	 */
	public Rectangle getActiveWindowArea() {
		// Windows以外はデフォルト挙動を行う
		if (!OsUtil.isWindows()) {
			log.info("osがwindowsではありません");
			return getDefaultScreenArea();
		}

		try {
			return WindowsActiveWindowResolver.resolve(this);

		} catch (Throwable t) {
			log.warn("アクティブウィンドウの撮影範囲取得に失敗しました。デフォルト撮影範囲へフォールバックします", t);
			return getDefaultScreenArea();
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
	 * 全モニターの範囲を取得する
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

	/**
	 * 座標の中心点から、該当するモニター（GraphicsDevice）を特定する
	 *
	 * @param rectangle ウィンドウのX,Y,W,H
	 * @return GraphicsDevice ウィンドウの中心が属しているモニター
	 */
	private GraphicsDevice findMonitorOfActiveWindow(Rectangle rectangle) {
		int centerX = rectangle.x + (rectangle.width / 2);
		int centerY = rectangle.y + (rectangle.height / 2);

		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			for (GraphicsDevice device : ge.getScreenDevices()) {
				if (device.getDefaultConfiguration().getBounds().contains(centerX, centerY)) {
					return device;
				}
			}
			// ウインドウの属しているモニターが見つからない場合、メインモニターを返却
			if (ge.getScreenDevices().length > 0) {
				return ge.getScreenDevices()[0];
			}
		} catch (Exception e) {
			log.warn("アクティブウィンドウの属するモニターの特定に失敗しました", e);
		}
		return null;
	}

	/**
	 * Windows専用のアクティブウィンドウ撮影範囲解決クラス
	 * <p>
	 * User32 や DWM API は Windows 専用のため、この内側クラスに閉じ込める。
	 * 外側クラスの初期化時には触らず、Windows 判定後にだけ参照することで、
	 * Windows 以外の環境で JNA の Windows API ロードが走ることを避ける。
	 * </p>
	 */
	private static class WindowsActiveWindowResolver {

		/** WindowsのUser32 APIにアクセスするためのJNA定数 */
		private static final User32 USER32 = User32.INSTANCE;

		/** WindowsのDWMから影なしウィンドウ範囲を取得するための属性 */
		private static final int DWMWA_EXTENDED_FRAME_BOUNDS = 9;

		/** Windows APIの正常終了コード */
		private static final int S_OK = 0;

		/**
		 * Windowsのdwmapi.dllにアクセスするためのJNAインターフェース
		 */
		private interface DwmApi extends StdCallLibrary {

			/** dwmapi.dllのインスタンス */
			DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class, W32APIOptions.DEFAULT_OPTIONS);

			/**
			 * ウィンドウに適用されているDWM属性を取得する
			 * @param hwnd ウィンドウの識別番号
			 * @param dwAttribute 取得する属性
			 * @param pvAttribute 取得結果の格納先
			 * @param cbAttribute 格納先のサイズ
			 * @return 成功時は0
			 */
			int DwmGetWindowAttribute(HWND hwnd, int dwAttribute, Pointer pvAttribute, int cbAttribute);
		}

		/**
		 * アクティブウィンドウの撮影範囲を解決する
		 * @param parent 外側の撮影範囲解決クラス
		 * @return アクティブウィンドウの撮影範囲
		 */
		private static Rectangle resolve(SnapAreaResolver parent) {
			// アクティブウィンドウの識別番号を取得する
			HWND hwnd = USER32.GetForegroundWindow();
			if (hwnd == null) {
				log.warn("アクティブウィンドウの識別番号の取得に失敗しました");
				return parent.getDefaultScreenArea();
			}

			// Windowsから影を除外した物理座標を取得する
			Rectangle physRect = getPhysicalWindowRect(hwnd);
			if (physRect == null) {
				log.warn("アクティブウィンドウの物理座標取得に失敗しました。デフォルト撮影範囲へフォールバックします");
				return parent.getDefaultScreenArea();
			}

			// ウィンドウが存在するモニター（GraphicsDevice）を特定する
			GraphicsDevice activeDevice = parent.findMonitorOfActiveWindow(physRect);
			if (activeDevice == null) {
				return physRect;
			}

			// モニターの解像度情報に合わせてJavaの仮想座標に変換して取得する
			return convertToJavaVirtualBounds(hwnd, physRect, activeDevice);
		}

		/**
		 * Windows APIを叩き、影を除外したウィンドウの物理座標を取得する
		 * @param hwnd ウィンドウの個別識別情報
		 * @return ウィンドウの影なし座標情報
		 */
		private static Rectangle getPhysicalWindowRect(HWND hwnd) {

			// DWMから影なし座標を取得する
			Rectangle dwmRect = getDwmWindowRect(hwnd);

			if (dwmRect != null) {
				return dwmRect;
			}
			log.warn("DWMによるウィンドウ座標取得に失敗しました。推測方式にフォールバックします");

			// DWMで取得できなかった場合は、推測方式で取得する
			Rectangle estimatedRect = getEstimatedWindowRect(hwnd);

			if (estimatedRect != null) {
				return estimatedRect;
			}

			log.warn("推測方式によるウィンドウ座標取得に失敗しました。");
			return null;
		}

		/**
		 * DWM APIから影を除外したウィンドウ座標を取得する
		 * @param hwnd ウィンドウの個別識別情報
		 * @return 影なしウィンドウ座標。取得できない場合はnull
		 */
		private static Rectangle getDwmWindowRect(HWND hwnd) {
			try {
				// DWMから「影を除外した見た目上のウィンドウ範囲」を取得する
				RECT dwmRect = new RECT();
				int result = DwmApi.INSTANCE.DwmGetWindowAttribute(
						hwnd,
						DWMWA_EXTENDED_FRAME_BOUNDS,
						dwmRect.getPointer(),
						dwmRect.size());

				// ネイティブ側で書き込まれたRECTの内容をJava側へ反映する
				dwmRect.read();

				if (result == S_OK && isValidRect(dwmRect)) {
					return toRectangle(dwmRect);
				}

				log.warn("DWMによるウィンドウ座標取得に失敗しました。result={}", result);

			} catch (Throwable t) {
				log.warn("DWMによるウィンドウ座標取得中に例外が発生しました", t);
			}

			return null;
		}

		/**
		 * Windows APIを叩き、DPIスケーリングを考慮した「影なし物理座標」を計算する
		 * @param hwnd ウィンドウの個別識別情報
		 * @return ウィンドウの影なし座標情報 (X,Y,W,H)
		 */
		private static Rectangle getEstimatedWindowRect(HWND hwnd) {

			RECT windowRect = new RECT();
			RECT clientRect = new RECT();

			boolean gotWindowRect = USER32.GetWindowRect(hwnd, windowRect); // 影付きのRect「X,Y,W,H」を取得する
			boolean gotClientRect = USER32.GetClientRect(hwnd, clientRect); // 実サイズのRect「W,H」を取得する

			if (!gotWindowRect || !gotClientRect) {
				log.warn("Windows APIによるウィンドウ座標取得に失敗しました");
				return null;
			}

			int totalW = windowRect.right - windowRect.left;
			int totalH = windowRect.bottom - windowRect.top;

			if (totalW <= 0 || totalH <= 0 || clientRect.right <= 0 || clientRect.bottom <= 0) {
				log.warn("Windows APIから不正なウィンドウ座標を取得しました");
				return null;
			}

			// 幅(影付き)から幅(影なし)を引き、2で割ることで片側の影を算出する
			int shadowWidth = (totalW - clientRect.right) / 2;

			// ウィンドウ左上を起点としたX,Y,W(幅),H(高さ)を取得する
			int winX = windowRect.left + shadowWidth;
			int winY = windowRect.top;
			int winW = totalW - (shadowWidth * 2);
			int winH = totalH - shadowWidth; // 上部は影なしのため引かない

			if (winW <= 0 || winH <= 0) {
				log.warn("推測方式で不正なウィンドウ座標になりました");
				return null;
			}

			return new Rectangle(winX, winY, winW, winH);
		}

		/**
		 * RECTが正常な範囲を持っているか判定する
		 * @param rect Windows APIから取得したRECT
		 * @return 正常な範囲の場合true
		 */
		private static boolean isValidRect(RECT rect) {
			// 右端 > 左側 && 下端 > 上端 であれば正常な範囲とする
			return rect.right > rect.left && rect.bottom > rect.top;
		}

		/**
		 * WindowsのRECTをJavaのRectangleへ変換する
		 * @param rect Windows APIから取得したRECT
		 * @return JavaのRectangle
		 */
		private static Rectangle toRectangle(RECT rect) {
			int x = rect.left;
			int y = rect.top;
			int w = rect.right - rect.left; // 右端 - 左端で幅取得
			int h = rect.bottom - rect.top; // 下端 - 上端で高さ取得

			return new Rectangle(x, y, w, h);
		}

		/**
		 * Windowsの物理座標を、Javaの仮想画面座標系へスケーリング変換し、画面外をカットする
		 * @param hwnd ウィンドウの識別番号
		 * @param physRect 影なしのウィンドウの座標情報(X,Y,W,H)
		 * @param device 取得したアクティブモニター
		 * @return Javaが管理する等倍の仮想モニターの座標に変換した、アクティブウィンドウの座標
		 */
		private static Rectangle convertToJavaVirtualBounds(HWND hwnd, Rectangle physRect, GraphicsDevice device) {
			Rectangle deviceBounds = device.getDefaultConfiguration().getBounds(); // モニター自体の座標取得

			try {
				// ウィンドウが最も被っているモニターの識別子を取得する
				HMONITOR hMonitor = USER32.MonitorFromWindow(hwnd, WinUser.MONITOR_DEFAULTTONEAREST);

				// osからモニターの配置、解像度情報を取得する
				MONITORINFOEX info = new MONITORINFOEX();
				BOOL gotMonitorInfo = USER32.GetMonitorInfo(hMonitor, info);

				if (gotMonitorInfo == null || !gotMonitorInfo.booleanValue()) {
					log.warn("モニター情報の取得に失敗しました");
					return physRect;
				}

				// OSが認識する幅と高さを算出する
				int osMonitorW = info.rcMonitor.right - info.rcMonitor.left;
				int osMonitorH = info.rcMonitor.bottom - info.rcMonitor.top;

				if (osMonitorW <= 0 || osMonitorH <= 0) {
					log.warn("不正なモニター情報を取得しました");
					return physRect;
				}

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
				Rectangle clipped = finalResult.intersection(deviceBounds); // 画面外の黒領域をカットする

				if (clipped.width <= 0 || clipped.height <= 0) {
					log.warn("変換後の撮影範囲が画面外になりました");
					return physRect;
				}

				return clipped;

			} catch (Throwable t) {
				log.warn("Windowsの物理座標を、Javaの仮想画面座標系へスケーリング変換処理が失敗しました", t);
				return physRect; // 失敗時は物理座標のまま返却する
			}
		}
	}
}