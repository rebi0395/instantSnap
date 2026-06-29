package com.github.rebi0395.instantsnap.model.enums;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.time.format.DateTimeFormatter;

import javafx.util.StringConverter;

/**
 * ファイル名に付与する日時形式を列挙するクラス
 */
public enum FileNameDateFormatType implements BaseEnum {

	/** ミリ秒まで（ドット区切り） */
	MILLISECOND_DOT("1", "yyyyMMdd.HHmmss.SSS", "yyyyMMdd.HHmmss.SSS"),
	/** 秒まで（ドット区切り） */
	SECOND_DOT("2", "yyyyMMdd.HHmmss", "yyyyMMdd.HHmmss"),
	/** ミリ秒まで（アンダースコア区切り） */
	MILLISECOND_UNDERSCORE("3", "yyyyMMdd_HHmmss_SSS", "yyyyMMdd_HHmmss_SSS"),
	/** 秒まで（アンダースコア区切り） */
	SECOND_UNDERSCORE("4", "yyyyMMdd_HHmmss", "yyyyMMdd_HHmmss");

	/** プロパティファイルと紐つく値 */
	private final String value;
	/** 表示名 */
	private final String baseDisplayName;
	/** 日時フォーマット */
	private final String pattern;

	/** コンストラクタ */
	FileNameDateFormatType(String value, String baseDisplayName, String pattern) {
		this.value = value;
		this.baseDisplayName = baseDisplayName;
		this.pattern = pattern;
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
	 * @return pattern 日時フォーマット
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * 日時フォーマッターを取得する
	 * @return 日時フォーマッター
	 */
	public DateTimeFormatter getDateFormatter() {
		return DateTimeFormatter.ofPattern(pattern);
	}

	/**
	 * プロパティファイルの値から対応する FileNameDateFormatType を取得する。
	 * 不正な値が指定された場合はデフォルト値を返し、ログを出力する。
	 * @param raw プロパティファイルと紐つく値
	 * @return 対応する FileNameDateFormatType、またはデフォルトの FileNameDateFormatType
	 */
	public static FileNameDateFormatType parseOrDefault(String raw) {
		return BaseEnum.parseOrDefault(
				FileNameDateFormatType.class,
				raw,
				MILLISECOND_DOT,
				PROPERTY_FILE_NAME_DATE_FORMAT);
	}

	/**
	 * 列挙型と文字列を相互変換する JavaFXコンバーターを取得する
	 * @return FileNameDateFormatType と文字列を相互変換するJavaFXコンバーター
	 */
	public static StringConverter<FileNameDateFormatType> toConverter() {
		return BaseEnum.toConverter(FileNameDateFormatType.class, MILLISECOND_DOT);
	}
}
