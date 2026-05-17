package com.github.rebi0395.instantsnap.service;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.io.IOException;

import com.github.rebi0395.instantsnap.model.SnapMode;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

/**
 * スクリーンショットの撮影範囲を特定するクラスに
 */
public class SnapAreaResolver {
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

	private Rectangle getActiveWindowArea() {
		String os = System.getProperty("os.name").toLowerCase();
		try {
			if (os.contains("win")) {
				return getActiveWindowAreaWindows();
			} else if (os.contains("mac")) {
				return getActiveWindowAreaMac();
			} else {
				// Linuxやその他のOSの場合は、アクティブスクリーンを返す
				return getActiveScreenArea();
			}
		} catch (IOException e) {
			// 例外が発生した場合は、アクティブスクリーンを返す
			return getActiveScreenArea();
		}
	}

	/**
	 * 
	 * @return Rectangle アクティブウィンドウの座標
	 */
	private Rectangle getActiveWindowAreaWindows() {
		HWND hwnd = User32.INSTANCE.GetForegroundWindow();
		RECT rect = new RECT();
		User32.INSTANCE.GetWindowRect(hwnd, rect);
		return new Rectangle(
			rect.left,
			rect.top,
			rect.right - rect.left,
			rect.bottom - rect.top
		);
	}

	private Rectangle getActiveWindowAreaMac() throws IOException {
		String script = "tell application \"System Events\"\n" +
				"    tell (first application process whose frontmost is true)\n" +
				"        set pos to position of window 1\n" +
				"        set sz to size of window 1\n" +
				"        return (item 1 of pos) & \",\" & (item 2 of pos) & \",\" & (item 1 of sz) & \",\" & (item 2 of sz)\n"
				+
				"    end tell\n" +
				"end tell";

		Process p = new ProcessBuilder("osascript", "-e", script).start();
		String result = new String(p.getInputStream().readAllBytes()).trim();

		String[] parts = result.split(",");
		int x = Integer.parseInt(parts[0]);
		int y = Integer.parseInt(parts[1]);
		int w = Integer.parseInt(parts[2]);
		int h = Integer.parseInt(parts[3]);

		return new Rectangle(x, y, w, h);
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
