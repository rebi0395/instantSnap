package com.github.rebi0395.instantsnap.model.enums;
import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import javafx.util.StringConverter;

/**
 * 撮影後の挙動パターンを定義するクラス
 */
public enum SnapAfter implements BaseEnum {

	/** 撮影後、通知を送付する */
	NOTICE("1", "notice"),
	/** 保存した画像ファイルを開く */
	OPEN_IMAGE("2", "OpenFile"),
	/** 何もしない(デフォルト値) */
	NOTHING("0", "Nothing");

	/** プロパティファイルと紐つく値 */
	private final String value;
	/** 表示名 */
	private final String displayName;

	/** コンストラクタ */
	SnapAfter(String value, String displayName) {
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
	 * プロパティファイルの値から対応する SnapAfter を取得する。
	 * 不正な値が指定された場合は null を返す。
	 *
	 * @param raw プロパティファイルと紐つく値
	 * @return 対応する SnapAfter、または不正値の場合は null
	 */
	public static SnapAfter parseValue(String raw) {
		return BaseEnum.parseValue(SnapAfter.class, raw);
	}

	/**
	 * プロパティファイルの値から対応する SnapAfter を取得する。
	 * 不正な値が指定された場合は デフォルト値を返し、ログを出力する。
	 * @param raw プロパティファイルと紐つく値
	 * @return 対応する SnapAfter、またはデフォルトの SnapAfter
	 */
	public static SnapAfter parseOrDefault(String raw) {
		return BaseEnum.parseOrDefault(SnapAfter.class, raw, NOTHING, SNAP_AFTER);
	}
	
	/**
	 * 列挙型と文字列を相互変換する	JavaFXコンバーターを取得する
	 * @return SnapAfter と文字列を交互変換するJavaFXコンバーター
	 */
	public static StringConverter<SnapAfter> toConverter() {
		return BaseEnum.toConverter(SnapAfter.class, NOTHING);
	}
}