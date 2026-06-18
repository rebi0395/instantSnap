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
import com.github.rebi0395.instantsnap.model.bean.SnapResultBean;
import com.github.rebi0395.instantsnap.model.enums.SnapAfter;

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
	/** 撮影後処理を行うインスタンス */
	private final SnapAfterService afterService;

	/**
	 * コンストラクタ
	 * @param areaResolver 撮影範囲を解決するインスタンス
	 * @param soundPlayer 効果音を再生するためのインスタンス
	 * @param afterService 撮影後処理を行うインスタンス
	 */
	public TakeSnapService(SnapAreaResolver areaResolver,
			SoundPlayer soundPlayer, SnapAfterService afterService) {
		this.areaResolver = areaResolver;
		this.soundPlayer = soundPlayer;
		this.afterService = afterService;
	}

	/**
	 * 撮影処理を実行する
	 * @param config ユーザーの設定値
	 * @throws ServiceException
	 */
	public SnapResultBean execute(ConfigBean config) throws ServiceException {
		if (!hasResultAction(config)) {
			// 行うべきアクションが設定されていない場合、早期リターン
			return new SnapResultBean();
		}

		SnapResultBean resultBean = new SnapResultBean(); // 結果クラスを作成
		boolean result; // 一時格納変数

		try {
			// 画面サイズを取得
			Rectangle snapSize = areaResolver.resolveArea(config.getSnapArea());

			// 撮影
			Robot robot = new Robot();
			BufferedImage snap = robot.createScreenCapture(snapSize);

			Path targetPath = null;
			// 画像保存、または撮影後処理で画像ファイルが必要な場合のみ、ファイルに保存する
			if (config.getIsSaveImage() || config.getSnapAfter() != SnapAfter.NOTHING) {

				// ファイル名作成(yyyyMMdd_HHmmss.SSS)
				LocalDateTime now = LocalDateTime.now();
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss.SSS");
				String systemTime = now.format(dtf);
				String fileName = systemTime + "." + config.getFormatType().getValue();

				if (!config.getIsSaveImage()) {
					// 一時保存なら、ファイル名の先頭をつけ、分かりやすくする
					fileName = "temporary_" + fileName;
				}

				// ディレクトリを確認し、保存パスを確定
				Path dir = resolveSaveDirectory(config); // 画像保存をしない場合は、一時保存パスを取得する
				targetPath = dir.resolve(fileName);

				// 画像を保存
				result = saveImage(snap, config.getFormatType().getValue(), targetPath);

				// 結果を格納
				if (result) {
					resultBean.setTargetPath(targetPath);
					if (config.getIsSaveImage()) {
						// 画像保存時
						resultBean.setIsImageSaved(true);
					} else {
						// 一時保存時
						resultBean.setIsTmpSaved(true);
					}
				}
			}

			// クリップボードコピーが有効であれば、クリップボードに保存する
			if (config.getIsCopyToClipboard()) {
				// クリップボード保存
				result = copyToClipboard(snap);
				// 結果を格納
				resultBean.setIsClipboardCopied(result);
			}

			// 撮影音設定が有効であれば、鳴らす
			if (config.getIsSoundEnabled()) {
				soundPlayer.playShutterSound();
			}

			// 撮影後設定が「何もしない」でないとき、撮影後処理を実行する
			if (config.getSnapAfter() != SnapAfter.NOTHING && targetPath != null) {
				afterService.execute(config.getSnapAfter(), targetPath);
			}

		} catch (AWTException | IOException e) {
			throw new ServiceException("スクリーンショットの撮影に失敗しました", e);
		}
		return resultBean;
	}

	/**
	 * スクショ起動キー押下後、行うべきアクションがあるかを確認する
	 * 画像を保存しない、クリップボードに保存しない、撮影後に「何もしない」を
	 * 全て選択している場合のみ、falseを返却する
	 * 
	 * @param config ユーザー設定値
	 * @return スクショ起動キー押下後、行うべきアクションがあればtrue / なければ false
	 */
	private boolean hasResultAction(ConfigBean config) {
		return config.getIsSaveImage()
				|| config.getIsCopyToClipboard()
				|| (config.getSnapAfter() != SnapAfter.NOTHING);
	}

	/**
	 * 設定された画像保存先パスが、ディレクトリであるか確認する
	 * 不正であれば、デフォルトのディレクトリパスを返却する
	 * 画像保存しない場合は、一時保存ファイル用のディレクトリパスを返却する
	 * 
	 * @param config ユーザー設定値
	 * @return 画像保存先ディレクトリ
	 * @throws IOException
	 */
	private Path resolveSaveDirectory(ConfigBean config) throws IOException {

		if (!config.getIsSaveImage() && config.getSnapAfter() != SnapAfter.NOTHING) {
			// 画像保存をしない、かつ、撮影後設定が「何もしない」でない場合
			// 一時保存ファイルのディレクトリパスを返却する
			Path tmpDir = Path.of(TMP_SAVE_DIR);
			Files.createDirectories(tmpDir);
			return tmpDir;

		} else {
			Path dir = Path.of(config.getSavePath());
			// 実在チェック、ディレクトリチェック
			if (Files.exists(dir)) {
				if (!Files.isDirectory(dir)) {
					// 保存先フォルダがフォルダでない場合、デフォルト値を設定する
					config.setSavePath(DEFAULT_SAVE_DIR);
					dir = Path.of(config.getSavePath());
					Files.createDirectories(dir); // 念押しで再生成する
					log.warn("画像保存先が不正のため、画像保存先をデフォルト({})に設定しました", DEFAULT_SAVE_DIR);
				}
			} else {
				// ファイルがない場合、自動で作成する
				Files.createDirectories(dir);
				log.warn("画像保存先パスが見つからないため、フォルダを自動生成しました");
			}
			return dir;
		}
	}

	/**
	 * 画像データを指定したフォルダに保存する。
	 * 
	 * @param image 画像データ
	 * @param format ファイルの拡張子
	 * @param targetPath 保存先パス
	 * @return true: 成功 / false: 失敗
	 * @throws IOException
	 */
	private boolean saveImage(BufferedImage image, String format, Path targetPath) throws IOException {
		boolean isWritten = ImageIO.write(image, format, targetPath.toFile());
		if (!isWritten) {
			throw new IOException("対応していない画像形式です: " + format);
		}
		log.info("スクリーンショットの画像保存に成功しました");
		return isWritten;
	}

	/**
	 * クリップボードに画像データを保存する。
	 * クリップボードロック等で失敗時、3回まで試行する
	 * @param image 画像データ
	 * @return true: 成功 / false: 失敗
	 */
	private boolean copyToClipboard(BufferedImage image) {
		ImageTransferable transferable = new ImageTransferable(image); // コンストラクタで画像を登録する
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); // クリップボード取得
		Exception lastException = null; // catch時のスタックトレースを保持する

		for (int retry = 1; retry <= 3; retry++) { // 試行は3回まで
			try {
				clipboard.setContents(transferable, null); // ownerは不要のためnull
				// 保存成功時の処理
				log.info("クリップボードに画像を保存しました");
				return true; // メソッドを終了する
			} catch (Exception e) {
				lastException = e; // スタックトレースを保存
				try {
					Thread.sleep(50); // 50ms置いて再送
				} catch (InterruptedException ie) {
					log.warn("クリップボードに保存の再試行中に割り込みが発生しました", ie);
					return false;
				}
			}
		}

		// 保存失敗時、ログを出す
		log.error("クリップボードへの画像保存に失敗しました", lastException);
		return false;
	}
}
