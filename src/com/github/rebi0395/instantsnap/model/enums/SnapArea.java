package com.github.rebi0395.instantsnap.model.enums;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.awt.Rectangle;

import com.github.rebi0395.instantsnap.service.SnapAreaResolver;

import javafx.util.StringConverter;

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
	 * 列挙型に対応する撮影範囲の座標を返却する
	 * @param resolver 撮影範囲を特定するクラス
	 * @return 撮影範囲の座標
	 */
	public Rectangle resolveArea(SnapAreaResolver resolver) {
		switch (this) {
		case ACTIVE_WINDOW:
			return resolver.getActiveWindowArea();

		case ACTIVE_SCREEN:
			return resolver.getActiveScreenArea();

		case MAIN_SCREEN:
			return resolver.getMainScreenArea();

		case FULL_SCREEN:
			return resolver.getFullScreenArea();

		default:
			return resolver.getDefaultScreenArea();
		}
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

	/**
	 * 列挙型と文字列を相互変換する	JavaFXコンバーターを取得する
	 * @return SnapArea と文字列を交互変換するJavaFXコンバーター
	 */
	public static StringConverter<SnapArea> toConverter() {
		return BaseEnum.toConverter(SnapArea.class, ACTIVE_WINDOW);
	}
}
