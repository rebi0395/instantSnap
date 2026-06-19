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
	 * @return SnapResultBean 処理結果格納クラス
	 * @throws IOException
	 */
	public SnapResultBean save(ConfigBean config, BufferedImage image, SnapResultBean result) throws IOException {

		// 画像保存、一時保存をする場合
		if (isFileRequired(config)) {

			// 保存先パス取得
			String filename = createFileName(config); // ファイル名生成
			Path dir = resolveSaveDirectory(config); // ディレクトリ解決
			Path targetPath = dir.resolve(filename); // 保存先パス取得
			// 保存処理
			boolean isSaved = saveImage(image, config.getFormatType().getValue(), targetPath);
			// 結果格納
			if (isSaved) {
				result.setTargetPath(targetPath);
				if (isTemporarySave(config)) {
					result.setIsTmpSaved(true);
				} else {
					result.setIsImageSaved(true);
				}

			}
		}

		// クリップボード保存をする場合
		if (config.getIsCopyToClipboard()) {
			// クリップボード保存処理
			boolean isCopied = copyToClipboard(image);
			// 結果格納
			if (isCopied) {
				result.setIsClipboardCopied(isCopied);
			}
		}

		return result;
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
	 * @return 画像保存OFF、かつ撮影後アクションありの場合 true
	 */
	public boolean isTemporarySave(ConfigBean config) {
		return !config.getIsSaveImage()
				&& config.getAfterSnapActionType() != AfterSnapActionType.NONE;
	}

	/**
	 * ファイル名を生成する
	 * @param config ユーザー設定値
	 * @return ファイル名
	 */
	public String createFileName(ConfigBean config) {

		// ファイル名作成(yyyyMMdd_HHmmss.SSS)
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss.SSS");
		String fileName = now.format(dtf) + "." + config.getFormatType().getValue();

		if (isTemporarySave(config)) {
			// 一時保存なら、ファイル名の先頭をつけ、分かりやすくする
			fileName = "temporary_" + fileName;
		}

		return fileName;
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

	/**
	 * 画像データを指定したフォルダに保存する。
	 * 
	 * @param image 画像データ
	 * @param format ファイルの拡張子
	 * @param targetPath 保存先パス
	 * @return true: 成功 / false: 失敗
	 * @throws IOException
	 */
	private boolean saveImage(BufferedImage image, String format, Path targetPath) throws IOException {
		boolean isWritten = ImageIO.write(image, format, targetPath.toFile());
		if (!isWritten) {
			throw new IOException("対応していない画像形式です: " + format);
		}
		log.info("スクリーンショットの画像保存に成功しました");
		return isWritten;
	}

	/**
	 * クリップボードに画像データを保存する。
	 * クリップボードロック等で失敗時、3回まで試行する
	 * @param image 画像データ
	 * @return true: 成功 / false: 失敗
	 */
	private boolean copyToClipboard(BufferedImage image) {
		ImageTransferable transferable = new ImageTransferable(image); // コンストラクタで画像を登録する
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); // クリップボード取得
		Exception lastException = null; // catch時のスタックトレースを保持する

		for (int retry = 1; retry <= 3; retry++) { // 試行は3回まで
			try {
				clipboard.setContents(transferable, null); // ownerは不要のためnull
				// 保存成功時の処理
				log.info("クリップボードに画像を保存しました");
				return true; // メソッドを終了する
			} catch (Exception e) {
				lastException = e; // スタックトレースを保存
				try {
					Thread.sleep(50); // 50ms置いて再送
				} catch (InterruptedException ie) {
					log.warn("クリップボードに保存の再試行中に割り込みが発生しました", ie);
					return false;
				}
			}
		}
		return false;
	}
}
