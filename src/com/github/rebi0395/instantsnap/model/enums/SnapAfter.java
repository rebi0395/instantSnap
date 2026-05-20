package com.github.rebi0395.instantsnap.model.enums;

import java.awt.Desktop;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;

/**
 * 撮影後の挙動パターンを定義するクラス
 */
public enum SnapAfter implements BaseEnum {

	/** 何もしない(デフォルト値) */
	NOTHING("1", "Nothing"){
		public void afterAction(Path path) {}
	},
	/** 保存先フォルダを開く */
	OPEN_DIR("2", "OpenDirectory"){
		public void afterAction(Path path) throws ServiceException {
			try {
				String os = System.getProperty("os.name").toLowerCase();
				if (os.contains("win")) {
					// TODO 最前面に出す処理と、既に開いていたら開かない処理を追加
	                new ProcessBuilder("explorer.exe", "/select,", path.toAbsolutePath().toString()).start();
				} else {				
					Desktop.getDesktop().open(path.getParent().toFile());
				}
			} catch (Exception e) {
				log.warn("ディレクトリフォルダを開くのに失敗しました。パス：{}", path.getParent(), e);
			}
		}
	},
	/** 保存した画像ファイルを開く */
	OPEN_IMAGE("3", "OpenFile"){
		public void afterAction(Path path) throws ServiceException {
			try {
				Desktop.getDesktop().open(path.toFile());
				
			} catch (Exception e) {
				log.warn("ディレクトリフォルダを開くのに失敗しました。パス：{}", path.getParent(), e);
			}
		}
	};
	
	public void afterAction(Path path) throws ServiceException {
        // 何もしない（NOTHINGはこれに自動で流れます）
    };

	/** プロパティファイルと紐つく値 */
	private final String value;
	/** 表示名 */
	private final String displayName;
	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SnapAfter.class);

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
	 * @param value プロパティファイルと紐つく値
	 * @return 対応する SnapAfter、または不正値の場合は null
	 */
	public static SnapAfter fromValue(String value) {
		return BaseEnum.fromValue(SnapAfter.class, value);
	}
}
