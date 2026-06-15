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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.ImageTransferable;
import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;

/**
 * 画面の撮影処理を行うクラス
 */
public class TakeSnapService {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(TakeSnapService.class);
	/** 撮影範囲を解決するためのインスタンス */
	private final SnapAreaResolver areaResolver;
	/** 効果音を再生するためのインスタンス */
	private final SoundPlayer soundPlayer;

	/**
	 * コンストラクタ
	 * @param areaResolver 撮影範囲を解決するインスタンス
	 * @param soundPlayer 効果音を再生するためのインスタンス
	 */
	public TakeSnapService(SnapAreaResolver areaResolver, SoundPlayer soundPlayer) {
		this.areaResolver = areaResolver;
		this.soundPlayer = soundPlayer;
	}

	/**
	 * 撮影処理を実行する
	 * @param config ユーザーの設定値
	 * @throws ServiceException
	 */
	public void execute(ConfigBean config) throws ServiceException {
		try {
			// 画面サイズを取得
			Rectangle snapSize = areaResolver.resolveArea(config.getSnapArea());

			// 撮影
			Robot robot = new Robot();
			BufferedImage snap = robot.createScreenCapture(snapSize);

			// ファイル名作成(yyyyMMdd_HHmmss.SSS)
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss.SSS");
			String systemTime = now.format(dtf);
			String fileName = systemTime + "." + config.getFormatType().getValue();
			
			// 保存先パス設定
			Path dir = resolveSaveDirectory(config);
			Path targetPath = dir.resolve(fileName);

			// 画像保存が有効であれば、保存する
			if (config.getIsSaveImage()) {
				saveImage(snap, targetPath, config); // 保存したファイルの絶対パス
			}

			// クリップボードコピーが有効であれば、クリップボードに保存する
			if (config.getIsCopyToClipboard()) {
				copyToClipboard(snap);
			}
			
			// 撮影音設定が有効であれば、鳴らす
			if (config.getIsSoundEnabled()) {
				soundPlayer.playShutterSound();
			}

			// 撮影後、ディレクトリなどを開く
			SnapAfterService afterService = new SnapAfterService();
			afterService.execute(config.getSnapAfter(), targetPath);

		} catch (AWTException | IOException e) {
			throw new ServiceException("スクリーンショットの撮影に失敗しました", e);
		}
	}

	/**
	 * 設定された画像保存先パスがディレクトリフォルダであるか解決する
	 * @param image 画像データ
	 * @throws IOException
	 */
	private Path resolveSaveDirectory(ConfigBean config) throws IOException {
		
		if (!config.getIsSaveImage()) {
			// 画像保存しない場合
			Path tmpDir = Path.of(TMP_SAVE_PATH);
		}
		
		
		
		
		Path dir = Path.of(config.getSavePath());
		// 実在チェック、ディレクトリチェック
		if (Files.exists(dir)) {
			if (!Files.isDirectory(dir)) {
				// 保存先フォルダがフォルダでない場合、デフォルト値を設定する
				config.setSavePath(DEFAULT_SAVE_PATH);
				dir = Path.of(config.getSavePath());
				Files.createDirectories(dir); // 念押しで再生成する
				log.warn("画像保存先が不正のため、画像保存先をデフォルト（" + DEFAULT_SAVE_PATH + "）に設定しました");
			}
		} else {
			// ファイルがない場合、自動で作成する
			Files.createDirectories(dir);
			log.warn("画像保存先パスが見つからないため、フォルダを自動生成しました");
		}
		return dir;
	}

	/**
	 * 画像データを指定したフォルダに保存する。
	 * @param image 画像データ
	 * @throws IOException
	 */
	private void saveImage(BufferedImage snap, Path targetPath, ConfigBean config) throws IOException {
		ImageIO.write(snap, config.getFormatType().getValue(), targetPath.toFile());
		log.info("スクリーンショットの画像保存に成功しました");
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
