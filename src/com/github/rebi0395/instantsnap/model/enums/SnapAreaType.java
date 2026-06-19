package com.github.rebi0395.instantsnap.model.enums;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import javafx.util.StringConverter;

/**
 * 撮影モードを指定するクラス
 */
public enum SnapAreaType implements BaseEnum {

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
	SnapAreaType(String value, String displayName) {
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
	 * プロパティファイルの値から対応する SnapAreaType を取得する。
	 * 不正な値が指定された場合は デフォルト値を返し、ログを出力する。
	 * @param raw プロパティファイルと紐つく値
	 * @return 対応する SnapAreaType、またはデフォルトの SnapAreaType
	 */
	public static SnapAreaType parseOrDefault(String raw) {
		return BaseEnum.parseOrDefault(SnapAreaType.class, raw, ACTIVE_WINDOW, PROPERTY_SNAP_AREA);
	}

	/**
	 * 列挙型と文字列を相互変換する	JavaFXコンバーターを取得する
	 * @return SnapAreaType と文字列を交互変換するJavaFXコンバーター
	 */
	public static StringConverter<SnapAreaType> toConverter() {
		return BaseEnum.toConverter(SnapAreaType.class, ACTIVE_WINDOW);
	}
}
