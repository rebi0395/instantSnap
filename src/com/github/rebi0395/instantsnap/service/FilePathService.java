package com.github.rebi0395.instantsnap.service;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.StrCheckUtil;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;

/**
 * ファイル名生成、ディレクトリ解決を行うクラス
 */
public class FilePathService {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(FilePathService.class);

	/** 一時保存の場合につける先頭文字 */
	private static final String TEMPORARY_FILE_NAME_PREFIX = "temporary_";

	/**
	 * ファイル名を生成する
	 * @param config ユーザー設定値
	 * @return ファイル名
	 */
	public String createFileName(ConfigBean config) {

		// ファイル名作成(yyyyMMdd.HHmmss.SSS)
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter dtf = config.getFileNameDateFormatType().getDateFormatter();
		String prefix = config.getFileNamePrefix(); // 先頭文字取得

		// ファイル名の要素組み立て
		StringBuilder builder = new StringBuilder();

		if (config.isTemporarySave()) {
			// 一時保存なら、ファイル名の先頭をつけ、分かりやすくする
			builder.append(TEMPORARY_FILE_NAME_PREFIX);
		}

		builder.append(prefix);
		builder.append(now.format(dtf));
		builder.append(".");
		builder.append(config.getFormatType().getValue());

		return builder.toString();
	}

	/**
	 * 同名ファイルが存在する場合、ファイル名に連番を付けて重複しないパスを返す
	 * @param targetPath 保存予定のパス
	 * @return 重複しない保存予定パス
	 */
	public Path createUniqueFilePath(Path targetPath) {

		String fileName = targetPath.getFileName().toString();
		Path uniqueFilePath = targetPath;

		int num = 0; // 被った場合、ファイル名につける数字
		int lastDotIndex = fileName.lastIndexOf("."); // 拡張子なしは考慮しない
		String base = fileName.substring(0, lastDotIndex); // 拡張子前
		String extension = fileName.substring(lastDotIndex); // 拡張子
		Path parent = targetPath.getParent(); // 親フォルダ

		// 重複しなくなるまで繰り返す
		while (Files.exists(uniqueFilePath)) {
			num++; // 数字を +1

			// ファイル名組み立て
			StringBuilder builder = new StringBuilder();
			builder.append(base);
			builder.append("(");
			builder.append(num);
			builder.append(")");
			builder.append(extension);

			String newName = builder.toString();
			uniqueFilePath = parent.resolve(newName);

		}
		return uniqueFilePath;
	}

	/**
	 * ファイル名プレビューを生成する
	 * 実際の日時ではなく、日時形式の表示名を使用する。
	 * 
	 * @param config ユーザー設定値
	 * @return ファイル名プレビュー
	 */
	public String createFileNamePreview(ConfigBean config) {

		StringBuilder builder = new StringBuilder();
		builder.append(config.getFileNamePrefix()); // 先頭文字
		builder.append(config.getFileNameDateFormatType().getBaseDisplayName()); // 日付
		builder.append("."); // ドット
		builder.append(config.getFormatType().getValue()); // 拡張子
		return builder.toString();
	}

	/**
	 * 設定された画像保存先パスが、ディレクトリであるか確認する
	 * 不正であれば、デフォルトのディレクトリパスを返却する
	 * 画像保存しない場合は、一時保存ファイル用のディレクトリパスを返却する
	 * 
	 * @param savePath 設定されている保存先パス
	 * @param isTemporary 一時保存であるか
	 * @return 画像保存先ディレクトリ
	 * @throws RuntimeException デフォルトの保存先ディレクトリの作成に失敗した場合
	 */
	public Path resolveSaveDirectory(String savePath, boolean isTemporary) {
		Path dir;
		try {
			// 一時保存であるかチェック
			if (isTemporary) {
				// 一時保存ファイルのディレクトリパスを返却する
				Path tmpDir = toPath(TMP_SAVE_DIR);
				Files.createDirectories(tmpDir);
				return tmpDir;
			}
			// パスオブジェクトに変換
			dir = toPath(savePath);
			
			// パスに予約名が含まれている場合、デフォルト反映
			if (containsWindowsReservedName(dir)) {
				log.warn("画像保存先フォルダを使用できないため、デフォルトの保存先({})に保存しました: {}",
						DEFAULT_SAVE_DIR, dir);
				return createDefaultSaveDirectory();
			}

			// 存在しない場合はディレクトリを作成する。既にディレクトリなら何もしない
			Files.createDirectories(dir);

		} catch (IOException | InvalidPathException | SecurityException e) {
			// ファイル・予約語・権限不足などで使用できない場合は例外になる
			log.warn("画像保存先フォルダを使用できないため、デフォルトの保存先({})に保存しました: {}",
					DEFAULT_SAVE_DIR, savePath);
			log.debug("スタックトレース:", e);

			// デフォルトディレクトリパスに変更し、ディレクトリがなければ作成
			try {
				return createDefaultSaveDirectory();
			} catch (IOException e1) {
				log.error("デフォルトの保存先({})の作成に失敗しました", DEFAULT_SAVE_DIR, e1);
				throw new RuntimeException("デフォルトの保存先の作成に失敗しました", e1);
			}
		}
		return dir;
	}

	/**
	 * 画像保存先パスの表示用文字列を解決する
	 * 設定値が不正な場合は、デフォルト保存先を表示する。
	 * 実際の設定値は変更しない。
	 * 
	 * @param savePath 画像保存先パス
	 * @return 表示用の画像保存先パス
	 */
	public String resolveSaveDirectoryPreview(String savePath) {
		if (isInvalidSaveDirectoryPath(savePath)) {
			return DEFAULT_SAVE_DIR;
		}
		return savePath.strip();
	}

	/**
	 * 画像保存先パスが不正か判定する
	 * 存在しない通常のディレクトリパスは、保存時に作成できる可能性があるため不正扱いしない。
	 * 
	 * @param savePath 設定されている画像保存先パス
	 * @return 表示用の画像保存先パスが不正値: true
	 */
	public boolean isInvalidSaveDirectoryPath(String savePath) {

		// 空の場合は不正
		if (StrCheckUtil.isBlankSafe(savePath)) {
			return true;
		}

		try {
			Path dir = toPath(savePath);

			// Windows予約語が含まれる場合は不正
			if (containsWindowsReservedName(dir)) {
				return true;
			}

			// 既に存在していて、ディレクトリではない場合は不正
			if (Files.exists(dir) && !Files.isDirectory(dir)) {
				return true;
			}

			// 存在しないが、パスとしては成立している場合
			return false;

			// パス変換に失敗した場合は不正
		} catch (InvalidPathException | SecurityException e) {
			return true;
		}
	}

	/**
	 * パス内にWindows予約名が含まれているか判定する
	 * パス区切りごとに確認する。
	 * 
	 * @param path 確認対象のパス
	 * @return Windows予約名が含まれる場合true
	 */
	private boolean containsWindowsReservedName(Path path) {
		for (Path name : path) {
			if (StrCheckUtil.isWindowsReservedNames(name.toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * デフォルト保存先ディレクトリを作成して返却する
	 * 
	 * @return デフォルト保存先ディレクトリ
	 * @throws IOException デフォルト保存先の作成に失敗した場合
	 */
	private Path createDefaultSaveDirectory() throws IOException {

		Path dir = toPath(DEFAULT_SAVE_DIR);
		Files.createDirectories(dir); // 正しい前提のため、例外は考えない
		return dir;
	}

	/**
	 * 保存先パス文字列をPathに変換する
	 * 
	 * @param savePath 保存先パス文字列
	 * @return Path
	 */
	private Path toPath(String savePath) {
		return Path.of(savePath.strip()).normalize();
	}
}
