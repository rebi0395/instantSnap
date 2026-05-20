package com.github.rebi0395.instantsnap.service;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
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
			// 1.画面サイズを取得
			SnapAreaResolver areaResolver = new SnapAreaResolver();
			Rectangle snapSize = areaResolver.getSnapArea(option.getSnapMode());
			
			// 2.撮影
			Robot robot = new Robot();
			BufferedImage snap = robot.createScreenCapture(snapSize);
			
			// 3.ファイル名作成(yyyyMMdd_HHmmss)
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss.SSS");
			String systemTime = now.format(dtf);
			String fileName = systemTime + "." + option.getFormat().getValue();
			
			// 4.保存
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
			ImageIO.write(snap, option.getFormat().toString(), target.toFile());		
			
			// 5.ログ出力
			log.info("スクリーンショットの撮影に成功しました");
			
		} catch (AWTException | IOException e) {
			throw new ServiceException("スクリーンショットの撮影に失敗しました", e);
		}
	}
}
