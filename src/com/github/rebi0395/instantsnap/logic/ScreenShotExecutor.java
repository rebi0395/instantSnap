package com.github.rebi0395.instantsnap.logic;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import com.github.rebi0395.instantsnap.core.Option;

public class ScreenShotExecutor {
	
	public void execute(Option option) {
		try {
			// 1.画面サイズを取得
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			Rectangle screenSize = new Rectangle(d);
			
			// 2.撮影
			Robot robot = new Robot();
			BufferedImage screenShot = robot.createScreenCapture(screenSize);
			
			// 3.ファイル名作成(yyyyMMdd_HHmmss)
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
			String systemTime = now.format(dtf);
			String fileName = systemTime + "." + option.getFormat();
					
			// 4.保存
			File file = new File(option.getSavePath(), fileName);
			// 指定先のフォルダが存在しないとき、自動で作成
			File parentDir = file.getParentFile();
			if (parentDir != null && !parentDir.exists()) {
			    parentDir.mkdirs(); 
			}
			ImageIO.write(screenShot, option.getFormat(), file);		
			
			// 5.ログ出力
			
		} catch (AWTException | IOException e) {
			System.out.println("失敗");
		} finally {
			
		}

	}
}
