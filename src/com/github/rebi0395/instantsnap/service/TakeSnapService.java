package com.github.rebi0395.instantsnap.service;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.ImageTransferable;
import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.Option;

/**
 * 画面の撮影処理を行うクラス
 */
public class TakeSnapService {
	
	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(TakeSnapService.class);
	
	/**
	 * 実行メソッド
	 * @param option
	 * @throws ServiceException
	 */
	public void execute(Option option) throws ServiceException {
		try {
			// 画面サイズを取得
			SnapAreaResolver areaResolver = new SnapAreaResolver();
			Rectangle snapSize = areaResolver.getSnapArea(option.getSnapArea());
			
			// 撮影
			Robot robot = new Robot();
			BufferedImage snap = robot.createScreenCapture(snapSize);
			
			// ファイル名作成(yyyyMMdd_HHmmss.SSS)
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss.SSS");
			String systemTime = now.format(dtf);
			String fileName = systemTime + "." + option.getFormatType().getValue();
			
			// 保存
			Path dir = Paths.get(option.getSavePath());
			// 実在チェック、ディレクトリチェック
			if (Files.exists(dir)){
				if (!Files.isDirectory(dir)) {
					// 保存先フォルダがフォルダでない場合、デフォルト値を設定する
					option.setSavePath(DEFAULT_SAVE_PATH);
					dir = Paths.get(option.getSavePath());
					Files.createDirectories(dir); // 念押しで再生成する
					log.warn("画像保存先が不正のため、画像保存先をデフォルト（" + DEFAULT_SAVE_PATH + "）に設定しました");
				}
			} else {
				// ファイルがない場合、自動で作成する
				Files.createDirectories(dir);
				log.warn("画像保存先パスが見つからないため、ファルダを自動生成しました");
			}
			Path target = dir.resolve(fileName);
			ImageIO.write(snap, option.getFormatType().toString(), target.toFile());
			log.info("スクリーンショットの撮影に成功しました");
			
			// 撮影後、ディレクトリなどを開く
			SnapAfterResolver afterResolver = new SnapAfterResolver ();
			afterResolver.afterAction(option.getSnapAfter(), target);
			
			// 設定が有効であれば、クリップボードに画像を保存する
			if (option.getIsCopyToClipboard()) {
				copyToClipboard(snap);
			}
			
		} catch (AWTException | IOException e) {
			throw new ServiceException("スクリーンショットの撮影に失敗しました", e);
		}
	}
	
	/**
	 * クリップボードに画像データを保存する。
	 * クリップボードロック等で失敗時、3回まで試行する
	 * @param image 画像データ
	 */
	private void copyToClipboard(BufferedImage image) {
		ImageTransferable transferable = new ImageTransferable(image); // コンストラクタで画像を登録する
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); // クリップボード取得
		int retry = 0; // 試行回数
		Exception stackTrace = null; // catch時のスタックトレースを保持する 
		while (retry <= 3) { // 試行は3回まで
			retry++;
			try {
				clipboard.setContents(transferable, null); // ownerは不要のためnull
				// 保存成功時の処理
				log.info("クリップボードに画像を保存しました");
				return; // メソッドを終了する
			} catch (Exception e) {
				stackTrace = e; // スタックトレースを保存
				try {
					Thread.sleep(50); // 50ms置いて再送
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt(); // 割り込みを復元
	                return; // 再送
				}
			}
		}
		// 保存失敗時、ログを出す
		log.error("クリップボードへの画像保存に失敗しました", stackTrace);
	}
}
