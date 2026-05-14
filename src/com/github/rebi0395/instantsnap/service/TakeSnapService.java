package com.github.rebi0395.instantsnap.service;

import static com.github.rebi0395.instantsnap.util.AppConstant.*;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import com.github.rebi0395.instantsnap.core.Option;
import com.github.rebi0395.instantsnap.exception.ServiceException;

/**
 * 画面の撮影処理を行うクラス
 */
public class TakeSnapService {
	
	/**
	 * 実行メソッド
	 * @param option
	 * @throws ServiceException
	 */
	public void execute(Option option) throws ServiceException {
		try {
			// 1.画面サイズを取得
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			Rectangle screenSize = new Rectangle(d);
			
			// 2.撮影
			Robot robot = new Robot();
			BufferedImage snap = robot.createScreenCapture(screenSize);
			
			// 3.ファイル名作成(yyyyMMdd_HHmmss)
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss.SSS");
			String systemTime = now.format(dtf);
			String fileName = systemTime + "." + option.getFormat();
					
			// 4.保存
			Path dir = Paths.get(option.getSavePath());
			// 実在チェック、ディレクトリチェック
			if (!Files.exists(dir) || !(Files.isDirectory(dir))) {
				// 保存先フォルダがフォルダでない場合、デフォルト値を設定する
				option.setSavePath(DEFAULT_SAVE_PATH);
				dir = Paths.get(option.getSavePath());
				Files.createDirectories(dir); // 念押しで再生成
				System.out.println("画像保存先が不正のため、画像保存先を初期化しました");
			}
			Path target = dir.resolve(fileName);
			ImageIO.write(snap, option.getFormat(), target.toFile());		
			
			// 5.ログ出力
			System.out.println("スクリーンショットの撮影処理に成功しました");
			
		} catch (AWTException | IOException e) {
			throw new ServiceException("スクリーンショットの撮影に失敗しました", e);
		}
	}
}
