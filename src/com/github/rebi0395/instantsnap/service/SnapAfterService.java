package com.github.rebi0395.instantsnap.service;

import java.awt.Desktop;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.model.enums.SnapAfter;

public class SnapAfterService {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SnapAfterService.class);

	/**
	 * 撮影後の処理クラスに、処理を依頼するメソッドです
	 * @param resolver 撮影後解決クラス
	 * @param path 保存した画像のファイルパス
	 */
	public void execute(SnapAfter snapAfter, Path path) {
		switch (snapAfter) {
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
	 * 通知トースト押下時、画像の保存先ファイルを、スクリーンショットを選択状態で開きます
	 * @param path 保存した画像のパス
	 */
	public void showToast(Path path) {
	}
	// TODO viewクラスに依頼する処理を書く

	/**
	 * スクリーンショット撮影後、画像ファイルを開きます
	 * @param path 保存した画像のパス
	 */
	public void openImage(Path path) {
		try {
			Desktop.getDesktop().open(path.toAbsolutePath().toFile());
		} catch (Exception e) {
			log.warn("Failed to open directory. (画像を開くのに失敗しました)", e);
		}
	}

	/**
	 * スクリーンショット撮影後、ペイントで画像ファイルを開きます
	 * @param path 保存した画像のパス
	 */
	public void openPaint(Path path) {
		try {
			new ProcessBuilder("mspaint", path.toAbsolutePath().toString()).start();
		} catch (Exception e) {
			log.warn("ペイントで画像を開くのに失敗しました", e);
		}
	}

	/**
	 * 通知トースト押下時、画像の保存先ファイルを、スクリーンショットを選択状態で開きます
	 * @param path 保存した画像のパス
	 */
	public void openDirectory(Path path) {
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
			// ログを出して例外を握りつぶす
			log.warn("Failed to open directory. (ディレクトリを開くのに失敗しました)", e);
		}
	}
}
