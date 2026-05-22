package com.github.rebi0395.instantsnap.service;

import java.awt.Desktop;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.enums.SnapAfter;

public class SnapAfterResolver {
	
	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SnapAfterResolver.class);

	public void afterAction(SnapAfter after, Path path) throws ServiceException {
		switch (after) {
			case OPEN_DIR:
				openDirectory(path);
			case OPEN_IMAGE:
				openImage(path);
			default:
				return; // 何もしない	
		}
	}

	public void openDirectory(Path path) throws ServiceException {
		try {
			String os = System.getProperty("os.name").toLowerCase();
			// windowの場合、コマンドを直接叩いて実行する
			if (os.contains("win")) {
				new ProcessBuilder(
					"cmd", "/c", // コマンドプロンプト起動
					"start", "", // ウィンドウタイトルは空で指定
					"explorer.exe", // エクスプローラー起動
					 "/select," + path.toAbsolutePath() // 絶対パスのファイルを選択状態にする
				).start();
			} else {
				Desktop.getDesktop().open(path.getParent().toFile());
			}
		} catch (Exception e) {
			log.warn("Failed to open directory. (ディレクトリを開くのに失敗しました)", e);
		}
	}
	
	public void openImage(Path path) throws ServiceException {
		try {
			String os = System.getProperty("os.name").toLowerCase();
			// windowの場合、コマンドを直接叩いて実行する
			if (os.contains("win")) {
				new ProcessBuilder(
					"cmd", "/c", // コマンドプロンプト起動
					"start", "", // ウィンドウタイトルは空で指定
					"explorer.exe", // エクスプローラー起動
					path.toAbsolutePath().toString() // 絶対パスのファイルを選択状態にする
				).start();
			} else {
				Desktop.getDesktop().open(path.getParent().toFile());
			}
		} catch (Exception e) {
			log.warn("Failed to open directory. (ディレクトリを開くのに失敗しました)", e);
		}
	}
}
