package com.github.rebi0395.instantsnap.service;

import java.awt.Desktop;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.model.enums.AfterSnapActionType;

/**
 * ファイルを開くクラス
 */
public class FileOpenService {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(FileOpenService.class);

	/**
	 * ユーザー設定値から指定の方法でファイルを開く
	 * 例外発生時はログを出力し、例外を投げない
	 * 
	 * @param afterSnapActionType 撮影後動作のEnum
	 * @param path 保存した画像のファイルパス
	 */
	public void execute(AfterSnapActionType afterSnapActionType, Path path) {
		switch (afterSnapActionType) {
		case OPEN_IMAGE:
			openImage(path);
			break;

		case OPEN_PAINT:
			openPaint(path);
			break;

		default:
			return; // 何もしない	
		}
	}

	/**
	 * スクリーンショット撮影後、画像ファイルを開きます
	 * @param targetPath 保存した画像のパス
	 */
	public void openImage(Path targetPath) {
		try {
			Desktop.getDesktop().open(targetPath.toAbsolutePath().toFile());
		} catch (Exception e) {
			log.warn("Failed to open directory. (画像を開くのに失敗しました)", e);
		}
	}

	/**
	 * スクリーンショット撮影後、ペイントで画像ファイルを開きます
	 * @param targetPath 保存した画像のパス
	 */
	public void openPaint(Path targetPath) {
		try {
			new ProcessBuilder("mspaint", targetPath.toAbsolutePath().toString()).start();
		} catch (Exception e) {
			log.warn("ペイントで画像を開くのに失敗しました", e);
		}
	}

	/**
	 * 画像の保存先ファイルを、スクリーンショットを選択状態で開きます
	 * @param targetPath 保存した画像のパス
	 */
	public void openDirectory(Path targetPath) {
		try {
			String os = System.getProperty("os.name").toLowerCase();
			// windowの場合、コマンドを直接叩いて実行する
			if (os.contains("win")) {
				new ProcessBuilder(
						"cmd", "/c", // コマンドプロンプト起動
						"start", "", // ウィンドウタイトルは空で指定
						"explorer.exe", // エクスプローラー起動
						"/select," + targetPath.toAbsolutePath() // 絶対パスのファイルを選択状態にする
				).start();
			} else {
				Desktop.getDesktop().open(targetPath.getParent().toFile());
			}
		} catch (Exception e) {
			// ログを出して例外を握りつぶす
			log.warn("ディレクトリを開くのに失敗しました", e);
		}
	}
}
