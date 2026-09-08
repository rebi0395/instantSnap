package com.github.rebi0395.instantsnap.service;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

import com.github.rebi0395.instantsnap.common.OsUtil;
import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.SnapResultBean;
import com.github.rebi0395.instantsnap.model.enums.AfterSnapActionType;

/**
 * 画面の撮影処理を行うクラス
 */
public class TakeSnapService {

	/** 撮影範囲を解決するインスタンス */
	private final SnapAreaResolver areaResolver;
	/** 画像を保存するインスタンス */
	private final SnapSaveService saveService;
	/** 音声を再生するインスタンス */
	private SoundPlayer soundPlayer;
	/** マウスカーソルを書き込むクラス */
	private MouseCursorService mouseCursorService;

	/**
	 * コンストラクタ
	 * @param areaResolver 撮影範囲を解決するインスタンス
	 * @param saveService 画像を保存するインスタンス
	 * @param soundPlayer 音声を再生するインスタンス
	 * @param mouseCursorService マウスカーソルを書き込むインスタンス
	 */
	public TakeSnapService(SnapAreaResolver areaResolver, SnapSaveService saveService, 
			SoundPlayer soundPlayer, MouseCursorService mouseCursorService) {
		this.areaResolver = areaResolver;
		this.saveService = saveService;
		this.soundPlayer = soundPlayer;
		this.mouseCursorService = mouseCursorService;
	}

	/**
	 * 撮影処理を実行する
	 *
	 * @param config ユーザーの設定値
	 * @param rangeSelection ドラッグ指定の撮影範囲
	 * @param windowHandle 撮影要求時に取得したウィンドウのネイティブハンドル
	 * @return 撮影処理結果
	 * @throws ServiceException 撮影処理に失敗した場合
	 */
	public SnapResultBean execute(
			ConfigBean config,
			Rectangle rangeSelection,
			long windowHandle) throws ServiceException {

		// 行うべきアクションが設定されていない場合、早期リターン
		if (!hasResultAction(config)) {
			return new SnapResultBean();
		}

		// 結果クラスを作成
		SnapResultBean resultBean = new SnapResultBean();

		try {
			// 撮影種別にかかわらずウィンドウ情報を渡し、範囲解決の入口を統一する。
			Rectangle snapSize = areaResolver.resolveArea(
					config.getSnapAreaType(),
					windowHandle);
			if (rangeSelection != null) {
				// ドラッグ指定している場合は、撮影範囲をドラッグ範囲で上書きする
				snapSize = rangeSelection;
			}
			// 撮影
			Robot robot = new Robot();
			BufferedImage snap = robot.createScreenCapture(snapSize);

			// マウスカーソルを画像に含める設定が有効
			if (config.getIsIncludeCursor() && OsUtil.isWindows()) {
				// Windowsカーソルをスクリーンショットに描画する
				snap = mouseCursorService.drawCursor(snap, snapSize);
			}
			// カメラのシャッター音が有効なら、鳴らす
			if (config.getIsSnapSoundEnabled()) {
				soundPlayer.playShutterSound();
			}

			// 保存処理
			saveService.saveImage(config, snap, resultBean);
			saveService.copyToClipboard(config, snap, resultBean);

		} catch (AWTException e) {
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
				|| (config.getAfterSnapActionType() != AfterSnapActionType.NONE);
	}
}
