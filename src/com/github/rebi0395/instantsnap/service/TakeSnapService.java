package com.github.rebi0395.instantsnap.service;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;

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

	/**
	 * コンストラクタ
	 * @param areaResolver 撮影範囲を解決するインスタンス
	 * @param saveService 画像を保存するインスタンス
	 * @param soundPlayer 音声を再生するインスタンス
	 */
	public TakeSnapService(SnapAreaResolver areaResolver, SnapSaveService saveService, SoundPlayer soundPlayer) {
		this.areaResolver = areaResolver;
		this.saveService = saveService;
		this.soundPlayer = soundPlayer;
	}

	/**
	 * 撮影処理を実行する
	 * @param config ユーザーの設定値
	 * @return 撮影処理結果
	 * @throws ServiceException
	 */
	public SnapResultBean execute(ConfigBean config) throws ServiceException {

		// 行うべきアクションが設定されていない場合、早期リターン
		if (!hasResultAction(config)) {
			return new SnapResultBean();
		}

		// 結果クラスを作成
		SnapResultBean resultBean = new SnapResultBean();

		try {
			// 画面サイズを取得
			Rectangle snapSize = areaResolver.resolveArea(config.getSnapAreaType());

			// 撮影
			Robot robot = new Robot();
			BufferedImage snap = robot.createScreenCapture(snapSize);

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
