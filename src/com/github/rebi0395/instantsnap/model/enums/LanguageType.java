package com.github.rebi0395.instantsnap.model.enums;

import java.util.Locale;

import com.github.rebi0395.instantsnap.common.AppConstant;

import javafx.util.StringConverter;

/**
 * 言語設定を定義する列挙型クラス
 */
public enum LanguageType implements BaseEnum {

	/** 英語 */
	ENGLISH("en", "English", Locale.ENGLISH),
	/** 日本語 */
	JAPANESE("ja", "日本語", Locale.JAPANESE);

	/** プロパティファイルと紐つく値 */
	private final String value;
	/** 表示名 */
	private final String baseDisplayName;
	/** ResourceBandleが読むプロパティファイルの指定 */
	private final Locale locale;

	/** コンストラクタ */
	LanguageType(String value, String baseDisplayName, Locale locale) {
		this.value = value;
		this.baseDisplayName = baseDisplayName;
		this.locale = locale;
	}

	/**
	 * @return value プロパティファイルと紐つく値
	 */
	@Override
	public String getValue() {
		return value;
	}

	/**
	 * @return baseDisplayName 表示名
	 */
	@Override
	public String getBaseDisplayName() {
		return baseDisplayName;
	}

	/**
	 * @return locale ResourceBandleが読むプロパティファイルの指定
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * プロパティファイルの値から対応する LanguageType を取得する。
	 * 不正な値が指定された場合は デフォルト値を返し、ログを出力する。
	 * @param raw プロパティファイルと紐つく値
	 * @return 対応する LanguageType、またはデフォルトの LanguageType
	 */
	public static LanguageType parseOrDefault(String raw) {
		return BaseEnum.parseOrDefault(LanguageType.class, raw, ENGLISH, AppConstant.PROPERTY_LANGUAGE);
	}

	/**
	 * 列挙型と文字列を相互変換する	JavaFXコンバーターを取得する
	 * @return FormatType と文字列を相互変換するJavaFXコンバーター
	 */
	public static StringConverter<LanguageType> toConverter() {
		return BaseEnum.toConverter(LanguageType.class, ENGLISH);
	}
}
