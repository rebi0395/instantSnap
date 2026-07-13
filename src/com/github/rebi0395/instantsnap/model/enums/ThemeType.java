package com.github.rebi0395.instantsnap.model.enums;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;
import static com.github.rebi0395.instantsnap.common.MessageKeyConstant.*;

import com.github.rebi0395.instantsnap.common.MessageKeyConverter;

import javafx.util.StringConverter;

/**
 * UI全体のテーマを管理する列挙型クラス
 */
public enum ThemeType implements BaseEnum, MessageEnum {

	/** デフォルト テーマなし */
	DEFAULT("0", "Default", THEME_DEFAULT, null, null),
	/** 絵本テーマ */
	STORYBOOK("1", "Storybook", THEME_STORYBOOK, CSS_MAIN_THEME_STORYBOOK, "theme-storybook");

	/** プロパティファイルと紐つく値 */
	private final String value;
	/** 基準表示名 */
	private final String baseDisplayName;
	/** メッセージキー */
	private final String messageKey;
	/** cssのファイルパス */
	private final String cssFilePath;
	/** テーマを適用するためのstyleClass */
	private final String styleClass;

	/** コンストラクタ */
	ThemeType(String value, String baseDisplayName, String messageKey, String cssFilePath, String styleClass) {
		this.value = value;
		this.baseDisplayName = baseDisplayName;
		this.messageKey = messageKey;
		this.cssFilePath = cssFilePath;
		this.styleClass = styleClass;
	}

	/**
	 * @return value プロパティファイルと紐つく値
	 */
	@Override
	public String getValue() {
		return value;
	}

	/**
	 * @return baseDisplayName 基準表示名（英語）
	 */
	@Override
	public String getBaseDisplayName() {
		return baseDisplayName;
	}

	/**
	 * @return messageKey メッセージキー
	 */
	@Override
	public String getMessageKey() {
		return messageKey;
	}

	/**
	 * @return cssFilePath cssのファイルパスをStringで返す
	 */
	public String getCssFilePath() {
		return cssFilePath;
	}

	/**
	 * @return styleClass テーマを適用するためのstyleClassを返す
	 */
	public String getStyleClass() {
		return styleClass;
	}

	/**
	 * プロパティファイルの値から対応する ThemeType を取得する。
	 * 不正な値が指定された場合は デフォルト値を返し、ログを出力する。
	 * @param raw プロパティファイルと紐つく値
	 * @return 対応する ThemeType、またはデフォルトの ThemeType
	 */
	public static ThemeType parseOrDefault(String raw) {
		return BaseEnum.parseOrDefault(ThemeType.class, raw, STORYBOOK, PROPERTY_THEME);
	}

	/**
	 * 列挙型と文字列を相互変換する	JavaFXコンバーターを取得する
	 * @return ThemeType と文字列を相互変換するJavaFXコンバーター
	 */
	public static StringConverter<ThemeType> toConverter(MessageKeyConverter converter) {
		return MessageEnum.toMessageConverter(ThemeType.class, DEFAULT, converter);
	}
}
