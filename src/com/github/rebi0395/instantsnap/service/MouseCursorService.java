package com.github.rebi0395.instantsnap.service;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.OsUtil;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HBRUSH;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * スクリーンショット画像に、Windowsで現在表示されている本物のマウスカーソルを描画するクラス
 * このクラスのみ全部AI任せ。なにもわからん
 */
public class MouseCursorService {

	/** カーソルが表示中であることを示すフラグ */
	private static final int CURSOR_SHOWING = 0x00000001;

	/** DrawIconExで通常描画を行うためのフラグ */
	private static final int DI_NORMAL = 0x0003;

	/** DIB作成時にRGBカラーを使用する指定 */
	private static final int DIB_RGB_COLORS = 0;

	/** 非圧縮RGB */
	private static final int BI_RGB = 0;

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(MouseCursorService.class);

	/**
	 * user32.dllを呼び出すためのJNAインターフェース
	 */
	private interface User32Ex extends StdCallLibrary {

		/** user32.dllのインスタンス */
		User32Ex INSTANCE = Native.load("user32", User32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

		/**
		 * 現在表示されているカーソル情報を取得する
		 * @param cursorInfo カーソル情報の格納先
		 * @return 成功した場合true
		 */
		boolean GetCursorInfo(CURSORINFO cursorInfo);

		/**
		 * カーソルまたはアイコンのハンドルをコピーする
		 * 元のカーソルハンドルを直接破棄しないために使用する
		 * @param hIcon コピー元のカーソルハンドル
		 * @return コピーされたカーソルハンドル
		 */
		HICON CopyIcon(HICON hIcon);

		/**
		 * カーソルまたはアイコンの詳細情報を取得する
		 * ここでカーソルのhotspotを取得する
		 * @param hIcon カーソルハンドル
		 * @param iconInfo カーソル情報の格納先
		 * @return 成功した場合true
		 */
		boolean GetIconInfo(HICON hIcon, ICONINFO iconInfo);

		/**
		 * カーソルまたはアイコンを指定したDCへ描画する
		 * @param hdc 描画先DC
		 * @param xLeft 描画先X座標
		 * @param yTop 描画先Y座標
		 * @param hIcon カーソルハンドル
		 * @param cxWidth 描画幅。0の場合はシステム標準サイズ
		 * @param cyHeight 描画高さ。0の場合はシステム標準サイズ
		 * @param istepIfAniCur アニメーションカーソルのフレーム指定
		 * @param hbrFlickerFreeDraw 背景ブラシ
		 * @param diFlags 描画フラグ
		 * @return 成功した場合true
		 */
		boolean DrawIconEx(
				HDC hdc,
				int xLeft,
				int yTop,
				HICON hIcon,
				int cxWidth,
				int cyHeight,
				int istepIfAniCur,
				HBRUSH hbrFlickerFreeDraw,
				int diFlags);

		/**
		 * CopyIconでコピーしたカーソルハンドルを破棄する
		 * @param hIcon 破棄対象のカーソルハンドル
		 * @return 成功した場合true
		 */
		boolean DestroyIcon(HICON hIcon);

		/**
		 * 画面全体のDCを取得する
		 * @param hwnd nullの場合は画面全体のDCを取得する
		 * @return 画面DC
		 */
		HDC GetDC(HWND hwnd);

		/**
		 * GetDCで取得したDCを解放する
		 * @param hwnd GetDCに渡したウィンドウハンドル
		 * @param hdc 解放対象DC
		 * @return 結果
		 */
		int ReleaseDC(HWND hwnd, HDC hdc);
	}

	/**
	 * gdi32.dllを呼び出すためのJNAインターフェース
	 */
	private interface Gdi32Ex extends StdCallLibrary {

		/** gdi32.dllのインスタンス */
		Gdi32Ex INSTANCE = Native.load("gdi32", Gdi32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

		/**
		 * メモリ上の描画先DCを作成する
		 * @param hdc 互換元DC
		 * @return 作成したメモリDC
		 */
		HDC CreateCompatibleDC(HDC hdc);

		/**
		 * Java側からピクセルを直接読み書きできるDIBを作成する
		 * @param hdc DC
		 * @param bitmapInfo ビットマップ情報
		 * @param usage 色指定方式
		 * @param bits ピクセル領域のポインター格納先
		 * @param section ファイルマッピング。今回はnull
		 * @param offset オフセット。今回は0
		 * @return 作成したビットマップ
		 */
		HBITMAP CreateDIBSection(
				HDC hdc,
				BITMAPINFO bitmapInfo,
				int usage,
				PointerByReference bits,
				Pointer section,
				int offset);

		/**
		 * DCに描画対象のGDIオブジェクトを選択する
		 * HBITMAPはHANDLEとして扱えるため、HGDIOBJは使わずHANDLEで受ける
		 * @param hdc DC
		 * @param object 選択するGDIオブジェクト
		 * @return 以前選択されていたGDIオブジェクト
		 */
		HANDLE SelectObject(HDC hdc, HANDLE object);

		/**
		 * GDIオブジェクトを削除する
		 * HBITMAPはHANDLEとして扱えるため、HGDIOBJは使わずHANDLEで受ける
		 * @param object 削除対象のGDIオブジェクト
		 * @return 成功した場合true
		 */
		boolean DeleteObject(HANDLE object);

		/**
		 * メモリDCを削除する
		 * @param hdc 削除対象DC
		 * @return 成功した場合true
		 */
		boolean DeleteDC(HDC hdc);
	}

	/**
	 * GetCursorInfoで使用する構造体
	 */
	@FieldOrder({ "cbSize", "flags", "hCursor", "ptScreenPos" })
	public static class CURSORINFO extends Structure {

		/** 構造体サイズ */
		public int cbSize;

		/** カーソル状態フラグ */
		public int flags;

		/** 現在のカーソルハンドル */
		public HICON hCursor;

		/** 画面全体でのカーソル座標。基本的にカーソルの先端位置 */
		public POINT ptScreenPos = new POINT();

		/** コンストラクタ */
		public CURSORINFO() {
			cbSize = size();
		}
	}

	/**
	 * GetIconInfoで使用する構造体
	 */
	@FieldOrder({ "fIcon", "xHotspot", "yHotspot", "hbmMask", "hbmColor" })
	public static class ICONINFO extends Structure {

		/** trueならアイコン、falseならカーソル */
		public boolean fIcon;

		/** カーソル先端位置のX。カーソル画像左上から見た相対位置 */
		public int xHotspot;

		/** カーソル先端位置のY。カーソル画像左上から見た相対位置 */
		public int yHotspot;

		/** マスクビットマップ。GetIconInfoが作成するため、後で削除が必要 */
		public HBITMAP hbmMask;

		/** カラービットマップ。GetIconInfoが作成するため、後で削除が必要 */
		public HBITMAP hbmColor;
	}

	/**
	 * BITMAPINFOHEADER構造体
	 */
	@FieldOrder({
			"biSize",
			"biWidth",
			"biHeight",
			"biPlanes",
			"biBitCount",
			"biCompression",
			"biSizeImage",
			"biXPelsPerMeter",
			"biYPelsPerMeter",
			"biClrUsed",
			"biClrImportant"
	})
	public static class BITMAPINFOHEADER extends Structure {

		/** 構造体サイズ */
		public int biSize;

		/** ビットマップ幅 */
		public int biWidth;

		/** ビットマップ高さ。負数にすると上から下へ並ぶトップダウンDIBになる */
		public int biHeight;

		/** プレーン数。通常1 */
		public short biPlanes;

		/** 1ピクセルあたりのビット数 */
		public short biBitCount;

		/** 圧縮方式 */
		public int biCompression;

		/** 画像サイズ */
		public int biSizeImage;

		/** 水平方向の解像度 */
		public int biXPelsPerMeter;

		/** 垂直方向の解像度 */
		public int biYPelsPerMeter;

		/** 使用色数 */
		public int biClrUsed;

		/** 重要色数 */
		public int biClrImportant;
	}

	/**
	 * RGBQUAD構造体
	 */
	@FieldOrder({ "rgbBlue", "rgbGreen", "rgbRed", "rgbReserved" })
	public static class RGBQUAD extends Structure {

		/** 青 */
		public byte rgbBlue;

		/** 緑 */
		public byte rgbGreen;

		/** 赤 */
		public byte rgbRed;

		/** 予約領域 */
		public byte rgbReserved;
	}

	/**
	 * BITMAPINFO構造体
	 */
	@FieldOrder({ "bmiHeader", "bmiColors" })
	public static class BITMAPINFO extends Structure {

		/** ビットマップヘッダー */
		public BITMAPINFOHEADER bmiHeader = new BITMAPINFOHEADER();

		/** カラーテーブル。32bit BI_RGBではほぼ使わないが構造体上必要 */
		public RGBQUAD[] bmiColors = (RGBQUAD[]) new RGBQUAD().toArray(1);
	}

	/**
	 * スクリーンショット画像に現在のマウスカーソルを描画する
	 * Windows以外、カーソル非表示、カーソル範囲外、描画失敗の場合は元画像をそのまま返す
	 * 
	 * @param image スクリーンショット画像
	 * @param snapArea 撮影範囲
	 * @return カーソル描画後の画像
	 */
	public BufferedImage drawCursor(BufferedImage image, Rectangle snapArea) {
		if (image == null || snapArea == null) {
			return image;
		}

		// この実装はWindows APIを使うため、Windows以外では何もしない
		if (!OsUtil.isWindows()) {
			return image;
		}

		// 現在のカーソル情報を取得する
		CURSORINFO cursorInfo = getCurrentCursorInfo();

		if (cursorInfo == null) {
			return image;
		}

		// カーソルが非表示なら描画しない
		if ((cursorInfo.flags & CURSOR_SHOWING) == 0) {
			return image;
		}

		// カーソルハンドルが取れていなければ描画できない
		if (cursorInfo.hCursor == null) {
			log.warn("現在のマウスカーソルハンドルを取得できませんでした");
			return image;
		}

		// カーソル先端の画面座標を取得する
		int screenCursorX = cursorInfo.ptScreenPos.x;
		int screenCursorY = cursorInfo.ptScreenPos.y;

		// カーソルの先端が撮影範囲外なら描画しない
		if (!snapArea.contains(screenCursorX, screenCursorY)) {
			return image;
		}

		// 現在のカーソルハンドルをコピーする
		// 元のカーソルハンドルを直接破棄してはいけないため、コピーしたものを使う
		HICON copiedCursor = User32Ex.INSTANCE.CopyIcon(cursorInfo.hCursor);

		if (copiedCursor == null) {
			log.warn("マウスカーソルハンドルのコピーに失敗しました");
			return image;
		}

		ICONINFO iconInfo = new ICONINFO();

		try {
			// カーソルのhotspotなどを取得する
			boolean isIconInfoSuccess = User32Ex.INSTANCE.GetIconInfo(copiedCursor, iconInfo);

			// ネイティブ側で書き込まれた情報をJava側へ反映する
			iconInfo.read();

			if (!isIconInfoSuccess) {
				log.warn("マウスカーソルのアイコン情報を取得できませんでした");
				return image;
			}

			// DIBにピクセルを書き込むため、TYPE_INT_RGBへ変換する
			// スクリーンショットは透過不要なので、Iビームなどのマスクカーソル対策としてRGBで扱う
			BufferedImage rgbImage = toRgbImage(image);

			// Windows GDIを使って、現在のカーソルを画像へ描画する
			drawCursorByGdi(
					rgbImage,
					snapArea,
					screenCursorX,
					screenCursorY,
					copiedCursor,
					iconInfo);

			return rgbImage;

		} catch (Throwable t) {
			log.warn("マウスカーソルの描画中に例外が発生しました", t);
			return image;

		} finally {
			// GetIconInfoが作成したビットマップを削除する
			deleteIconInfoBitmaps(iconInfo);

			// CopyIconでコピーしたカーソルハンドルを削除する
			User32Ex.INSTANCE.DestroyIcon(copiedCursor);
		}
	}

	/**
	 * 現在のカーソル情報を取得する
	 * @return カーソル情報。取得できない場合はnull
	 */
	private CURSORINFO getCurrentCursorInfo() {
		CURSORINFO cursorInfo = new CURSORINFO();

		// 構造体サイズなどをネイティブ側へ書き込む
		cursorInfo.write();

		boolean isSuccess = User32Ex.INSTANCE.GetCursorInfo(cursorInfo);

		// ネイティブ側で書き込まれた情報をJava側へ反映する
		cursorInfo.read();

		if (!isSuccess) {
			log.warn("現在のマウスカーソル情報を取得できませんでした");
			return null;
		}

		return cursorInfo;
	}

	/**
	 * Windows GDIを使って、BufferedImage上にカーソルを描画する
	 * 
	 * @param image 描画対象画像。TYPE_INT_RGBであること
	 * @param snapArea 撮影範囲
	 * @param screenCursorX 画面全体でのカーソルX座標
	 * @param screenCursorY 画面全体でのカーソルY座標
	 * @param cursorIcon コピー済みのカーソルハンドル
	 * @param iconInfo カーソル情報
	 */
	private void drawCursorByGdi(
			BufferedImage image,
			Rectangle snapArea,
			int screenCursorX,
			int screenCursorY,
			HICON cursorIcon,
			ICONINFO iconInfo) {

		int width = image.getWidth();
		int height = image.getHeight();

		HDC screenDc = null;
		HDC memoryDc = null;
		HBITMAP dibBitmap = null;
		HANDLE oldObject = null;

		try {
			// 画面全体のDCを取得する
			screenDc = User32Ex.INSTANCE.GetDC(null);

			if (screenDc == null) {
				log.warn("画面DCの取得に失敗しました");
				return;
			}

			// メモリ上に描画先DCを作成する
			memoryDc = Gdi32Ex.INSTANCE.CreateCompatibleDC(screenDc);

			if (memoryDc == null) {
				log.warn("マウスカーソル描画用のメモリDC作成に失敗しました");
				return;
			}

			// Windows側のDIBピクセル領域のポインターを受け取る
			PointerByReference bitsRef = new PointerByReference();

			// Java画像と同じ幅・高さの32bit DIBを作る
			BITMAPINFO bitmapInfo = createBitmapInfo(width, height);

			dibBitmap = Gdi32Ex.INSTANCE.CreateDIBSection(
					screenDc,
					bitmapInfo,
					DIB_RGB_COLORS,
					bitsRef,
					null,
					0);

			if (dibBitmap == null || bitsRef.getValue() == null) {
				log.warn("マウスカーソル描画用のDIB作成に失敗しました");
				return;
			}

			// 作成したDIBをメモリDCの描画先として選択する
			// HBITMAPはHANDLEとして扱えるため、そのままSelectObjectへ渡す
			oldObject = Gdi32Ex.INSTANCE.SelectObject(memoryDc, dibBitmap);

			// Java画像のピクセル配列を取得する
			int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

			// Java画像のピクセルをWindows側のDIBへ書き込む
			Pointer bits = bitsRef.getValue();
			bits.write(0, pixels, 0, pixels.length);

			// 画面全体のカーソル座標を、スクショ画像内の座標へ変換する
			int cursorXOnImage = screenCursorX - snapArea.x;
			int cursorYOnImage = screenCursorY - snapArea.y;

			// DrawIconExへ渡す座標は、カーソル画像の左上座標
			// GetCursorInfoの座標はカーソル先端なので、hotspotを引く
			int drawX = cursorXOnImage - iconInfo.xHotspot;
			int drawY = cursorYOnImage - iconInfo.yHotspot;

			// Windows APIで、現在の本物カーソルをDIB上へ描画する
			boolean isDrawSuccess = User32Ex.INSTANCE.DrawIconEx(
					memoryDc,
					drawX,
					drawY,
					cursorIcon,
					0,
					0,
					0,
					null,
					DI_NORMAL);

			if (!isDrawSuccess) {
				log.warn("マウスカーソルの描画に失敗しました");
				return;
			}

			// Windows側でカーソル描画済みになったDIBピクセルをJava画像へ戻す
			bits.read(0, pixels, 0, pixels.length);

			// 念のため、RGB画像として扱えるようにピクセル値を正規化する
			// Iビームなどのマスクカーソル描画で不要な上位ビットが混ざるのを防ぐ
			normalizeRgbPixels(pixels);

		} finally {
			// SelectObjectした場合は、元のオブジェクトへ戻す
			if (memoryDc != null && oldObject != null) {
				Gdi32Ex.INSTANCE.SelectObject(memoryDc, oldObject);
			}

			// DIBを削除する
			if (dibBitmap != null) {
				Gdi32Ex.INSTANCE.DeleteObject(dibBitmap);
			}

			// メモリDCを削除する
			if (memoryDc != null) {
				Gdi32Ex.INSTANCE.DeleteDC(memoryDc);
			}

			// 画面DCを解放する
			if (screenDc != null) {
				User32Ex.INSTANCE.ReleaseDC(null, screenDc);
			}
		}
	}

	/**
	 * BufferedImageをTYPE_INT_RGBへ変換する
	 * Robotで撮影した画像は基本的に不透明画像なので、カーソル描画でもRGBとして扱う
	 * Iビームカーソルなどのマスク描画でアルファ値が崩れるのを避けるため、ARGBではなくRGBに揃える
	 * 
	 * @param image 変換対象画像
	 * @return TYPE_INT_RGBの画像
	 */
	private BufferedImage toRgbImage(BufferedImage image) {
		if (image.getType() == BufferedImage.TYPE_INT_RGB) {
			return image;
		}

		BufferedImage rgbImage = new BufferedImage(
				image.getWidth(),
				image.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		Graphics2D graphic = rgbImage.createGraphics();

		try {
			// スクリーンショット用途なので、不透明RGB画像へ変換する
			graphic.drawImage(image, 0, 0, null);
		} finally {
			graphic.dispose();
		}

		return rgbImage;
	}

	/**
	 * DIB作成用のBITMAPINFOを作成する
	 * 
	 * @param width 画像幅
	 * @param height 画像高さ
	 * @return BITMAPINFO
	 */
	private BITMAPINFO createBitmapInfo(int width, int height) {
		BITMAPINFO bitmapInfo = new BITMAPINFO();

		bitmapInfo.bmiHeader.biSize = bitmapInfo.bmiHeader.size();
		bitmapInfo.bmiHeader.biWidth = width;

		// 負数にするとトップダウンDIBになる
		// Java画像と同じく、配列の先頭を画像の上側として扱える
		bitmapInfo.bmiHeader.biHeight = -height;

		bitmapInfo.bmiHeader.biPlanes = 1;
		bitmapInfo.bmiHeader.biBitCount = 32;
		bitmapInfo.bmiHeader.biCompression = BI_RGB;
		bitmapInfo.bmiHeader.biSizeImage = width * height * 4;
		bitmapInfo.bmiHeader.biXPelsPerMeter = 0;
		bitmapInfo.bmiHeader.biYPelsPerMeter = 0;
		bitmapInfo.bmiHeader.biClrUsed = 0;
		bitmapInfo.bmiHeader.biClrImportant = 0;

		// Java側で設定した値をネイティブメモリへ反映する
		bitmapInfo.write();

		return bitmapInfo;
	}

	/**
	 * ピクセル配列をRGB画像向けに正規化する
	 * TYPE_INT_RGBではアルファ値を使わないため、上位8bitを落としてRGBだけ残す
	 * 
	 * @param pixels 画像ピクセル配列
	 */
	private void normalizeRgbPixels(int[] pixels) {
		if (pixels == null) {
			return;
		}

		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = pixels[i] & 0x00FFFFFF;
		}
	}

	/**
	 * GetIconInfoが作成したビットマップを削除する
	 * 
	 * @param iconInfo アイコン情報
	 */
	private void deleteIconInfoBitmaps(ICONINFO iconInfo) {
		if (iconInfo == null) {
			return;
		}

		if (iconInfo.hbmColor != null) {
			Gdi32Ex.INSTANCE.DeleteObject(iconInfo.hbmColor);
		}

		if (iconInfo.hbmMask != null) {
			Gdi32Ex.INSTANCE.DeleteObject(iconInfo.hbmMask);
		}
	}
}