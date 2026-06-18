package com.github.rebi0395.instantsnap.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.SnapResultBean;
import com.github.rebi0395.instantsnap.repository.PropertiesRepository;
import com.github.rebi0395.instantsnap.service.KeyEventListener;
import com.github.rebi0395.instantsnap.service.SnapAfterService;
import com.github.rebi0395.instantsnap.service.SnapAreaResolver;
import com.github.rebi0395.instantsnap.service.SoundPlayer;
import com.github.rebi0395.instantsnap.service.TakeSnapService;
import com.github.rebi0395.instantsnap.service.TmpFolderService;
import com.github.rebi0395.instantsnap.service.TriggerKeyManager;
import com.github.rebi0395.instantsnap.view.presenter.SnapNotificationPresenter;

import javafx.application.Platform;

/**
 * 対応するサービスクラスに処理を振り分ける、司令塔クラス
 */
public class InstantSnapCoordinator implements KeyEventListener {

	/** ユーザーの設定値 */
	private final ConfigBean config;
	/** プロパティファイルのリポジトリ */
	private final PropertiesRepository repository;
	/** 一時保存ディレクトリの管理クラス */
	private final TmpFolderService tmpFolder;
	/** 撮影範囲を解決するクラス */
	private final SnapAreaResolver areaResolver;
	/** 撮影後の処理クラス */
	private final SnapAfterService afterService;
	/** 効果音再生クラス */
	private final SoundPlayer soundPlayer;
	/** 撮影トリガーの管理クラス */
	private final TriggerKeyManager keyManager;
	/** 全体の撮影処理クラス */
	private final TakeSnapService takeSnapService;
	/** 撮影通知を表示するクラス */ 
	private final SnapNotificationPresenter sPresenter;
	

	/** スレッドセーフに撮影処理を呼び出すためのクラス */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	/** 設定値の保存処理と、撮影処理の同時呼び出しを防止するオブジェクト */
	private final Object configLock = new Object();
	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(InstantSnapCoordinator.class);

	/** コンストラクタ 初期化処理を実行する */
	public InstantSnapCoordinator(SnapNotificationPresenter sPresenter) {
		// インスタンスを生成
		this.config = new ConfigBean();
		this.repository = new PropertiesRepository();
		this.tmpFolder = new TmpFolderService();
		this.areaResolver = new SnapAreaResolver();
		this.afterService = new SnapAfterService();
		this.soundPlayer = new SoundPlayer();

		// 依存性の注入があるクラス
		this.keyManager = new TriggerKeyManager(config, this);
		this.takeSnapService = new TakeSnapService(areaResolver, soundPlayer, afterService);
		
		// Stage情報を持つクラス
		this.sPresenter = sPresenter;

		init();
	}

	/**
	 * アプリ起動時の初期化処理を行う
	 */
	public void init() {
		try {
			// ユーザーの設定値をプロパティファイルを読み込み初期化
			repository.read(config);
			// 一時保存ファイルのリセット
			tmpFolder.cleanTmpDirectory();
			// キー監視開始
			keyManager.activate();

		} catch (ServiceException e) {
			// チェック済例外
			log.error("アプリの初期化に失敗しました", e);
		} catch (Exception e) {
			// 非チェック例外
			log.error("想定外のエラーが発生しました", e);
		}
	}

	/**
	 * プロパティファイルに設定を保存します
	 */
	public void save() {
		try {
			synchronized (configLock) { // 排他制御
				repository.save(config); // プロパティファイルを更新
			}
			// チェック済例外
		} catch (ServiceException e) {
			log.error("設定の更新に失敗しました", e);
			// 非チェック例外
		} catch (Exception e) {
			log.error("想定外のエラーが発生しました", e);
		}
	}

	/**
	 * アプリを終了する
	 */
	public void exit() {
		try {
			keyManager.deactivate(); // キー監視の停止
		} catch (ServiceException e) {
			log.error("Instant Snapの終了処理に失敗しました", e);
		} finally {
			executor.shutdown(); // 撮影処理の終了
			soundPlayer.close(); // 音声リソースの開放
			tmpFolder.cleanTmpDirectory(); // 一時保存ファイルの削除
			Platform.exit(); // 画面を閉じる
		}
	}

	/**
	 * 撮影トリガーが押されたとき、撮影処理を呼び出す
	 */
	@Override
	public void requestSnap() {
		// 別スレッド立ち上げ
		executor.submit(() -> {
			synchronized (configLock) { // 排他制御
				try {
					// 撮影処理
					SnapResultBean result = takeSnapService.execute(config);

				} catch (ServiceException e) {
					log.error("スクリーンショットの撮影または保存に失敗しました", e);
				}
			}
		});
	}

	/**
	 * アプリの終了キーが押されたとき、アプリを終了する
	 */
	@Override
	public void requestExit() {
		exit(); // アプリを正常終了させる
	}

	/**
	 * ユーザーの設定値を取得する
	 * @return config
	 */
	public ConfigBean getConfigBean() {
		return config;
	}
}
