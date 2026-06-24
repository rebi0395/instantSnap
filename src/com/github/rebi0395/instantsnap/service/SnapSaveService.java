package com.github.rebi0395.instantsnap.service;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.ImageTransferable;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.SnapResultBean;
import com.github.rebi0395.instantsnap.model.enums.AfterSnapActionType;

/**
 * 画像データを保存するクラス
 */
public class SnapSaveService {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SnapSaveService.class);

	/** 一時保存の場合につける先頭文字 */
	private static final String TEMPORARY_FILE_NAME_PREFIX = "temporary_";

	/** コンストラクタ */
	public SnapSaveService() {
		// 何もしない
	}

	/**
	 * 画像を保存する
	 * 
	 * @param config ユーザー設定値クラス
	 * @param image スクリーンショットの画像データ
	 * @param result 結果を格納するクラス
	 */
	public void saveImage(ConfigBean config, BufferedImage image, SnapResultBean result) {

		// 画像保存や一時保存をしない場合、早期リターン
		if (!isFileRequired(config)) {
			return;
		}

		try {
			// 保存先パス取得
			String fileName = createFileName(config); // ファイル名生成
			Path dir = resolveSaveDirectory(config); // ディレクトリ解決
			Path targetPath = dir.resolve(fileName); // 保存先パス取得
			targetPath = resolveDuplicateFileName(targetPath); // 重複チェック

			// 保存処理
			boolean isWritten = ImageIO.write(image, config.getFormatType().getValue(), targetPath.toFile());
			if (!isWritten) {
				log.error("対応していない画像形式のため、画像保存に失敗しました: {}", config.getFormatType().getValue());
				return;
			}

			// 結果格納
			result.setTargetPath(targetPath);
			if (isTemporarySave(config)) {
				result.setIsTmpSaved(true);
			} else {
				result.setIsImageSaved(true);
			}
			log.info("スクリーンショットの画像保存に成功しました");

		} catch (IOException e) {
			log.error("スクリーンショットの画像保存に失敗しました", e);
		}
	}

	/**
	 * クリップボードに画像データを保存する。
	 * クリップボードロック等で失敗時、3回まで試行する
	 * 
	 * @param config ユーザー設定値クラス
	 * @param image スクリーンショットの画像データ
	 * @param result 結果を格納するクラス
	 */
	public void copyToClipboard(ConfigBean config, BufferedImage image, SnapResultBean result) {

		// クリップボード保存をしない場合、早期リターン
		if (!config.getIsCopyToClipboard()) {
			return;
		}

		ImageTransferable transferable = new ImageTransferable(image); // コンストラクタで画像を登録する
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); // クリップボード取得
		Exception lastException = null; // catch時のスタックトレースを保持する

		for (int retry = 1; retry <= 3; retry++) { // 試行は3回まで
			try {
				clipboard.setContents(transferable, null); // ownerは不要のためnull
				// 保存成功時の処理
				log.info("クリップボードへ画像をコピーしました");
				result.setIsClipboardCopied(true);
				return; // メソッドを終了する

			} catch (Exception e) {
				lastException = e; // スタックトレースを保存
				try {
					Thread.sleep(50); // 50ms置いて再送
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					log.warn("クリップボードへのコピーの再試行中に割り込みが発生しました", ie);
					return;
				}
			}
		}
		log.error("クリップボードへのコピーに失敗しました", lastException);
	}

	/**
	 * 画像ファイルが必要かを返す
	 *
	 * @param config ユーザー設定値
	 * @return 画像保存ON、または撮影後アクションありの場合 true
	 */
	public boolean isFileRequired(ConfigBean config) {
		return config.getIsSaveImage()
				|| config.getAfterSnapActionType() != AfterSnapActionType.NONE;
	}

	/**
	 * 一時保存かを返す
	 *
	 * @param config ユーザー設定値
	 * @return 撮影後アクションが一時保存。または
	 * 画像保存OFF、かつ撮影後アクションありの場合 true
	 */
	public boolean isTemporarySave(ConfigBean config) {
		return config.getAfterSnapActionType() == AfterSnapActionType.SAVE_TO_TMP ||
				(!config.getIsSaveImage() && config.getAfterSnapActionType() != AfterSnapActionType.NONE);
	}

	/**
	 * ファイル名を生成する
	 * @param config ユーザー設定値
	 * @return ファイル名
	 */
	public String createFileName(ConfigBean config) {

		// ファイル名作成(yyyyMMdd.HHmmss.SSS)
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss.SSS");
		String prefix = config.getFileNamePrefix(); // 先頭文字取得

		// ファイル名の要素組み立て
		StringBuilder builder = new StringBuilder();

		if (isTemporarySave(config)) {
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
	public Path resolveDuplicateFileName(Path targetPath) {

		String fileName = targetPath.getFileName().toString();
		Path uniqueFilePath = targetPath;

		int num = 0; // 被った場合、ファイル名につける数字
		int lastDotIndex = fileName.lastIndexOf("."); // 拡張子なしは考慮しない
		String base = fileName.substring(0, lastDotIndex); // 拡張子前
		String extension = fileName.substring(lastDotIndex); // 拡張子

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

			Path parent = targetPath.getParent();
			uniqueFilePath = parent.resolve(newName);

		}
		return uniqueFilePath;
	}

	/**
	 * 設定された画像保存先パスが、ディレクトリであるか確認する
	 * 不正であれば、デフォルトのディレクトリパスを返却する
	 * 画像保存しない場合は、一時保存ファイル用のディレクトリパスを返却する
	 * 
	 * @param config ユーザー設定値
	 * @return 画像保存先ディレクトリ
	 * @throws IOException
	 */
	private Path resolveSaveDirectory(ConfigBean config) throws IOException {

		if (isTemporarySave(config)) {
			// 一時保存ファイルのディレクトリパスを返却する
			Path tmpDir = Path.of(TMP_SAVE_DIR);
			Files.createDirectories(tmpDir);
			return tmpDir;

		} else {
			Path dir = Path.of(config.getSavePath());
			// 実在チェック
			if (Files.exists(dir)) {
				// ディレクトリかチェック
				if (!Files.isDirectory(dir)) {
					// ディレクトリでない場合、デフォルト値を設定する
					config.setSavePath(DEFAULT_SAVE_DIR);
					dir = Path.of(config.getSavePath());
					Files.createDirectories(dir); // 念押しで再生成する
					log.warn("画像保存先が不正のため、画像保存先をデフォルト({})に設定しました", DEFAULT_SAVE_DIR);
				}
			} else {
				// ファイルがない場合、自動で作成する
				Files.createDirectories(dir);
				log.warn("画像保存先パスが見つからないため、フォルダを自動生成しました");
			}
			return dir;
		}
	}
}
