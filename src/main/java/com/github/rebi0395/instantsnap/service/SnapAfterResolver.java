package com.github.rebi0395.instantsnap.service;

import java.awt.Desktop;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapAfterResolver {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SnapAfterResolver.class);

	/**
	 * 通知トースト押下時、画像の保存先ファイルを、スクリーンショットを選択状態で開きます
	 * @param path 保存した画像のパス
	 */
	public void showToast (Path path) {}
	// TODO viewクラスに依頼する処理を書く
	
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
}
