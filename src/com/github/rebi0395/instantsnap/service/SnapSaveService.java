package com.github.rebi0395.instantsnap.service;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.ImageTransferable;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.SnapResultBean;

/**
 * 画像データを保存するクラス
 */
public class SnapSaveService {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SnapSaveService.class);

	/** ファイル名生成、ディレクトリ解決を行うクラス */
	private final FilePathService filePathService;
	
	/** コンストラクタ */
	public SnapSaveService(FilePathService filePathService) {
		this.filePathService = filePathService;
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
		if (!config.isFileRequired()) {
			return;
		}

		try {
			// 保存先パス取得
			String fileName = filePathService.createFileName(config); // ファイル名生成
			Path dir = filePathService.resolveSaveDirectory(config); // ディレクトリ解決
			Path targetPath = dir.resolve(fileName); // 保存先パス取得
			targetPath = filePathService.createUniqueFilePath(targetPath); // 重複チェック

			// 保存処理
			boolean isWritten = ImageIO.write(image, config.getFormatType().getValue(), targetPath.toFile());
			if (!isWritten) {
				log.error("対応していない画像形式のため、画像保存に失敗しました: {}", config.getFormatType().getValue());
				return;
			}

			// 結果格納
			result.setTargetPath(targetPath);
			if (config.isTemporarySave()) {
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
}
