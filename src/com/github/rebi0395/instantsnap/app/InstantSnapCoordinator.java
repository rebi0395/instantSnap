package com.github.rebi0395.instantsnap.app;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.repository.PropertiesRepository;
import com.github.rebi0395.instantsnap.service.KeyEventListener;
import com.github.rebi0395.instantsnap.service.SnapAfterService;
import com.github.rebi0395.instantsnap.service.SnapAreaResolver;
import com.github.rebi0395.instantsnap.service.SoundPlayer;
import com.github.rebi0395.instantsnap.service.TakeSnapService;
import com.github.rebi0395.instantsnap.service.TriggerKeyManager;

import javafx.application.Platform;

/**
 * 対応するサービスクラスに処理を振り分ける、司令塔クラス
 */
public class InstantSnapCoordinator implements KeyEventListener {

	/** ユーザーの設定値 */
	private ConfigBean config;
	/** プロパティファイルのリポジトリ */
	private PropertiesRepository repository;

	/** 撮影トリガーの管理クラス */
	private TriggerKeyManager keyManager;
	/** 撮影範囲を解決するクラス */
	private SnapAreaResolver areaResolver;
	/** 全体の撮影処理クラス */
	private TakeSnapService takeSnapService;
	/** 撮影後の処理クラス */
	private SnapAfterService afterService;
	/** 効果音再生クラス */
	private SoundPlayer soundPlayer;

	/** スレッドセーフに撮影処理を呼び出すためのクラス */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	/** 設定値の保存処理と、撮影処理の同時呼び出しを防止するオブジェクト */
	private final Object configLock = new Object();
	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(InstantSnapCoordinator.class);

	/** コンストラクタ 初期化処理を実行する */
	public InstantSnapCoordinator() {
		init();
	}

	/**
	 * アプリ起動時の初期化処理を行う
	 */
	public void init() {
		try {
			// ユーザーの設定値をプロパティファイルを読み込み初期化
			this.config = new ConfigBean();
			this.repository = new PropertiesRepository();
			repository.read(config);

			// サービスクラスのインスタンスを生成する
			this.keyManager = new TriggerKeyManager(config, this);
			this.areaResolver = new SnapAreaResolver();
			this.soundPlayer = new SoundPlayer();
			this.afterService = new SnapAfterService();
			this.takeSnapService = new TakeSnapService(areaResolver, soundPlayer, afterService);

			// キー監視開始
			keyManager.activate();

			// チェック済例外
		} catch (ServiceException e) {
			log.error("アプリの初期化に失敗しました", e);
			// 非チェック例外
		} catch (Exception e) {
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
	 * 
	 * @param status 終了コード。正常終了は0、異常終了は1
	 */
	public void exit(int status) {
		try {
			keyManager.deactivate(); // キー監視の停止
		} catch (ServiceException e) {
			log.error("Instant Snapの終了処理に失敗しました", e);
			status = 1;
		} finally {
			executor.shutdown(); // 撮影処理の終了
			soundPlayer.close(); // 音声リソースの開放
			Platform.exit(); // 画面を閉じる
			System.exit(status); // アプリのロジックの終了
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
					takeSnapService.execute(config);

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
		exit(0); // アプリを正常終了させる
	}

	/**
	 * ユーザーの設定値を取得する
	 * @return config
	 */
	public ConfigBean getConfigBean() {
		return config;
	}
}
