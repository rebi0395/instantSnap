package com.github.rebi0395.instantsnap.common;

import java.util.HashMap;
import java.util.Map;

import static com.github.rebi0395.instantsnap.common.StrCheckUtil.*;
import static com.github.kwhat.jnativehook.keyboard.NativeKeyEvent.*;

import javafx.scene.input.KeyCode;

/**
 * JavaFXのKeyCode、または文字列を、JNativeHookのNativeKeyCodeに変換するクラス
 */
public class NativeKeyCodeConverter {

	/** KeyCodeをキーにもち、Nativeキーコードを値として持つ変換マップ */
	private static final Map<KeyCode, Integer> KEYCODE_MAP = new HashMap<>();
	/** Nativeキーコードをキーにもち、KeyCodeを値として持つ変換マップ */
	private static final Map<Integer, KeyCode> NATIVE_MAP = new HashMap<>();

	/** 右Altキーが拡張キー扱いされた場合、飛んでくるキーコードを定義 */
	public static final int VC_ALT_R = 3640; // 3640
	/** 右Ctrlキーが拡張キー扱いされた場合、飛んでくるキーコードを定義 */
	public static final int VC_CONTROL_R = 3613; // 3613
	/** 右Shiftキーが拡張キー扱いされた場合、飛んでくるキーコードを定義 */
	public static final int VC_SHIFT_R = 3638; // 3638

	// インスタンス化しない
	private NativeKeyCodeConverter() {
	}

	/*
	 * マップを初期化する静的イニシャライザ 
	 */
	static {
		// JavaFXキーコードをキー、Nativeキーコードを値としたMAP生成
		// --- A〜Z ---
		KEYCODE_MAP.put(KeyCode.A, VC_A);
		KEYCODE_MAP.put(KeyCode.B, VC_B);
		KEYCODE_MAP.put(KeyCode.C, VC_C);
		KEYCODE_MAP.put(KeyCode.D, VC_D);
		KEYCODE_MAP.put(KeyCode.E, VC_E);
		KEYCODE_MAP.put(KeyCode.F, VC_F);
		KEYCODE_MAP.put(KeyCode.G, VC_G);
		KEYCODE_MAP.put(KeyCode.H, VC_H);
		KEYCODE_MAP.put(KeyCode.I, VC_I);
		KEYCODE_MAP.put(KeyCode.J, VC_J);
		KEYCODE_MAP.put(KeyCode.K, VC_K);
		KEYCODE_MAP.put(KeyCode.L, VC_L);
		KEYCODE_MAP.put(KeyCode.M, VC_M);
		KEYCODE_MAP.put(KeyCode.N, VC_N);
		KEYCODE_MAP.put(KeyCode.O, VC_O);
		KEYCODE_MAP.put(KeyCode.P, VC_P);
		KEYCODE_MAP.put(KeyCode.Q, VC_Q);
		KEYCODE_MAP.put(KeyCode.R, VC_R);
		KEYCODE_MAP.put(KeyCode.S, VC_S);
		KEYCODE_MAP.put(KeyCode.T, VC_T);
		KEYCODE_MAP.put(KeyCode.U, VC_U);
		KEYCODE_MAP.put(KeyCode.V, VC_V);
		KEYCODE_MAP.put(KeyCode.W, VC_W);
		KEYCODE_MAP.put(KeyCode.X, VC_X);
		KEYCODE_MAP.put(KeyCode.Y, VC_Y);
		KEYCODE_MAP.put(KeyCode.Z, VC_Z);

		// --- 0〜9 ---
		KEYCODE_MAP.put(KeyCode.DIGIT0, VC_0);
		KEYCODE_MAP.put(KeyCode.DIGIT1, VC_1);
		KEYCODE_MAP.put(KeyCode.DIGIT2, VC_2);
		KEYCODE_MAP.put(KeyCode.DIGIT3, VC_3);
		KEYCODE_MAP.put(KeyCode.DIGIT4, VC_4);
		KEYCODE_MAP.put(KeyCode.DIGIT5, VC_5);
		KEYCODE_MAP.put(KeyCode.DIGIT6, VC_6);
		KEYCODE_MAP.put(KeyCode.DIGIT7, VC_7);
		KEYCODE_MAP.put(KeyCode.DIGIT8, VC_8);
		KEYCODE_MAP.put(KeyCode.DIGIT9, VC_9);

		// --- F1〜F12 ---
		KEYCODE_MAP.put(KeyCode.F1, VC_F1);
		KEYCODE_MAP.put(KeyCode.F2, VC_F2);
		KEYCODE_MAP.put(KeyCode.F3, VC_F3);
		KEYCODE_MAP.put(KeyCode.F4, VC_F4);
		KEYCODE_MAP.put(KeyCode.F5, VC_F5);
		KEYCODE_MAP.put(KeyCode.F6, VC_F6);
		KEYCODE_MAP.put(KeyCode.F7, VC_F7);
		KEYCODE_MAP.put(KeyCode.F8, VC_F8);
		KEYCODE_MAP.put(KeyCode.F9, VC_F9);
		KEYCODE_MAP.put(KeyCode.F10, VC_F10);
		KEYCODE_MAP.put(KeyCode.F11, VC_F11);
		KEYCODE_MAP.put(KeyCode.F12, VC_F12);

		// --- ナビゲーションキー ---
		KEYCODE_MAP.put(KeyCode.PAGE_UP, VC_PAGE_UP);
		KEYCODE_MAP.put(KeyCode.PAGE_DOWN, VC_PAGE_DOWN);
		KEYCODE_MAP.put(KeyCode.HOME, VC_HOME);
		KEYCODE_MAP.put(KeyCode.END, VC_END);

		// --- 矢印キー ---
		KEYCODE_MAP.put(KeyCode.UP, VC_UP);
		KEYCODE_MAP.put(KeyCode.DOWN, VC_DOWN);
		KEYCODE_MAP.put(KeyCode.LEFT, VC_LEFT);
		KEYCODE_MAP.put(KeyCode.RIGHT, VC_RIGHT);

		// --- Insert / Delete ---
		KEYCODE_MAP.put(KeyCode.INSERT, VC_INSERT);
		KEYCODE_MAP.put(KeyCode.DELETE, VC_DELETE);

		// --- Space / PrintScreen ---
		KEYCODE_MAP.put(KeyCode.SPACE, VC_SPACE);
		KEYCODE_MAP.put(KeyCode.PRINTSCREEN, VC_PRINTSCREEN);

		// Nativeキーコードをキー、javaFXキーコードを値とするマップ生成
		for (Map.Entry<KeyCode, Integer> entry : KEYCODE_MAP.entrySet()) {
			NATIVE_MAP.put(entry.getValue(), entry.getKey());
		}
	}

	/**
	 * JavaFXのキーコードをNativeのキーコードに変換する
	 * マップに存在しない場合は、未定義キーとして返却する
	 * @param code JavaFXのキーコード
	 * @return Nativeキーコード
	 */
	public static int toNativeKeyCode(KeyCode code) {
		return KEYCODE_MAP.getOrDefault(code, VC_UNDEFINED);
	}

	/**
	 * 引数がNativeキーコードに含まれているかを判定し、キーコードであれば返却する
	 * 含まれていなければ未定義を返す
	 * @param keyNum キーコードか検査する数字
	 * @return キーコード / null, 空、数字でない、マップにない場合は未定義(0)を返却
	 */
	public static int toNativeKeyCode(int keyNum) {
		if (NATIVE_MAP.containsKey(keyNum)) {
			return keyNum;
		} else {
			return VC_UNDEFINED;
		}
	}

	/**
	 * 文字列がNativeキーコードであるかを判定し、キーコードであればintに型変換する
	 * @param num プロパティファイルなどから取得した文字列
	 * @return キーコード / null, 空、数字でない、マップにない場合は未定義(0)を返却
	 */
	public static int toNativeKeyCode(String num) {
		if (isBlankSafe(num)) {
			return VC_UNDEFINED; // 空白やnullの場合、未定義を返す
		}

		try {
			int keyNum = Integer.parseInt(stripSafe(num));
			return toNativeKeyCode(keyNum);

		} catch (NumberFormatException e) {
			return VC_UNDEFINED; // 整数値に変換できなかった場合は、未定義とする
		}
	}

	/**
	 * NativeキーからJavaFXキーを返却する
	 * @param code nativeのキーコード
	 * @return 引数に対応するJavaFXのKeyCode
	 */
	public static KeyCode toFXKeyCode(int nativeCode) {
		return NATIVE_MAP.get(nativeCode);
	}

	/**
	 * 拡張キー（右側の Shift / Ctrl / Alt）を、左側と同じ正規キーコードに正規化する。
	 * @param nativeCode nativeのキーコード
	 * @return 正規化されたキーコード
	 */
	public static int normalizeNativeKeyCode(int nativeCode) {
		switch (nativeCode) {
		case VC_ALT_R:
			return VC_ALT;

		case VC_CONTROL_R:
			return VC_CONTROL;

		case VC_SHIFT_R:
			return VC_SHIFT;

		default:
			return nativeCode;
		}
	}

}
