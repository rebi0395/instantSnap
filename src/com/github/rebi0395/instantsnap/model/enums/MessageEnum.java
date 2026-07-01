package com.github.rebi0395.instantsnap.model.enums;

import static com.github.rebi0395.instantsnap.common.StrCheckUtil.*;

import com.github.rebi0395.instantsnap.common.MessageKeyConverter;

import javafx.util.StringConverter;

/**
 * メッセージキーを持つEnumの基底インターフェース
 */
public interface MessageEnum {

	/**
	 * 基準表示名（英語）を取得する
	 * @return 基準表示名(英語)
	 */
	String getBaseDisplayName();
	
	/**
	 * メッセージキーを取得する
	 * @return メッセージキー
	 */
	String getMessageKey();

	/**
	 * メッセージキーを変換し、メッセージを取得する
	 * @param converter
	 * @return メッセージ
	 */
	default String getMessage(MessageKeyConverter converter) {
		if (converter == null) {
			return getBaseDisplayName();
		}
		return converter.getMessage(this);

	}

	/**
	 * 列挙型の値とメッセージ(メッセージキー変換後)を相互変換するJavaFXコンバーターを取得する
	 * @param <E>
	 * @param enums 列挙型のオブジェクト
	 * @param defaultValue 列挙型のデフォルト値
	 * @return
	 */
	static <E extends Enum<E> & MessageEnum> StringConverter<E> toMessageConverter(
			Class<E> enums, E defaultValue, MessageKeyConverter converter) {

		return new StringConverter<E>() {
			/**
			 * 列挙型の値を表示名に変換する
			 */
			@Override
			public String toString(E value) {
				if (value == null) {
					return defaultValue.getMessage(converter);

				}
				return value.getMessage(converter);
			}

			/**
			 * メッセージキーを変換したメッセージから、列挙型の値に変換する
			 */
			@Override
			public E fromString(String str) {
				if (isBlankSafe(str)) {
					return defaultValue;
				}
				String target = stripSafe(str);

				for (E mode : enums.getEnumConstants()) {
					if (mode.getMessage(converter).equalsIgnoreCase(target)) {
						return mode;
					}
				}

				for (E mode : enums.getEnumConstants()) {
					if (mode.getBaseDisplayName().equalsIgnoreCase(target)) {
						return mode;
					}
				}

				return defaultValue; // 不正値の場合はデフォルト値を返す
			}
		};
	}
}
