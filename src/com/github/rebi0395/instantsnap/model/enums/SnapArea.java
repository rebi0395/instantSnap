package com.github.rebi0395.instantsnap.model.enums;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

/**
 * 撮影モードを指定するクラス
 */
public enum SnapArea implements BaseEnum {

	/** 使用しているウィンドウのみ撮影 */
	ACTIVE_WINDOW("1", "activeWindow"),
	/** マウスポインターのあるモニターを撮影 */
	ACTIVE_SCREEN("2", "activeScreen"),
	/** メインモニターを固定して撮影 */
	MAIN_SCREEN("3", "mainScreen"),
	/** 全てのモニターを全て含んで撮影 */
	FULL_SCREEN("4", "fullScreen");

	/** プロパティファイルと紐つく値 */
	private final String value;
	/** 表示名 */
	private final String displayName;

	/** コンストラクタ */
	SnapArea(String value, String displayName) {
		this.value = value;
		this.displayName = displayName;
	}

	/**
	 * @return value プロパティファイルと紐つく値
	 */
	@Override
	public String getValue() {
		return value;
	}

	/**
	 * @return displayName 表示名
	 */
	@Override
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * プロパティファイルの値から対応する SnapArea を取得する。
	 * 不正な値が指定された場合は null を返す。
	 *
	 * @param raw プロパティファイルと紐つく値
	 * @return 対応する SnapArea、または不正値の場合は null
	 */
	public static SnapArea parseValue(String raw) {
	    return BaseEnum.parseValue(SnapArea.class, raw);
	}

	/**
	 * プロパティファイルの値から対応する SnapArea を取得する。
	 * 不正な値が指定された場合は デフォルト値を返し、ログを出力する。
	 * @param raw プロパティファイルと紐つく値
	 * @return 対応する SnapArea、またはデフォルトの SnapArea
	 */
	public static SnapArea parseOrDefault(String raw) {
	    return BaseEnum.parseOrDefault(SnapArea.class, raw, ACTIVE_WINDOW, SNAP_AREA);
	}
}
