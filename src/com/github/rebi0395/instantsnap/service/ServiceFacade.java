package com.github.rebi0395.instantsnap.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.repository.PropertiesRepository;

/**
 * 対応するサービスクラスに処理を振り分ける、司令塔クラス
 */
public class ServiceFacade implements KeyEventListener {

	/** ユーザーの設定値 */
	private ConfigBean config;
	/** プロパティファイルのリポジトリ */
	private PropertiesRepository editor;
	/** 撮影トリガーの管理クラス */
	private TriggerKeyManager keyManager;
	/** 撮影範囲を解決するクラス */
	private SnapAreaResolver areaResolver;
	/** 撮影処理クラス */
	private TakeSnapService takeSnap;

	/** スレッドセーフに撮影処理を呼び出すためのクラス */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	/** 設定値の保存処理と、撮影処理の同時呼び出しを防止するオブジェクト */
	private final Object configLock = new Object();
	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(ServiceFacade.class);

	/** コンストラクタ 初期化処理を実行する */
	public ServiceFacade() {
		init();
	}

	/**
	 * アプリ起動時の初期化処理を行う
	 */
	public void init() {
		try {
			// ユーザーの設定値をプロパティファイルを読み込み初期化
			this.config = new ConfigBean();
			this.editor = new PropertiesRepository();
			editor.read(config);

			// サービスクラスはfacadeクラスのみ生成する
			this.keyManager = new TriggerKeyManager(config, this);
			this.areaResolver = new SnapAreaResolver();
			this.takeSnap = new TakeSnapService(areaResolver);

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
	 * @param configBean ユーザーの設定値を保持するインスタンス
	 */
	public void save(ConfigBean configBean) {
		try {
			synchronized (configLock) { // 排他制御
				editor.save(config); // プロパティファイルを更新
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
	 * 撮影トリガーが押されたとき、撮影処理を呼び出す
	 */
	@Override
	public void requestSnap() {
		// 別スレッド立ち上げ
		executor.submit(() -> {
			synchronized (configLock) { // 排他制御
				try {
					// 撮影処理
					takeSnap.execute(config);

				} catch (ServiceException e) {
					log.error("スクリーンショットの撮影または保存に失敗しました", e);
				}
			}
		});
	}

	/**
	 * pauseキーが押されたとき、キー監視を終了する
	 */
	@Override
	public void requestExit() {
		try {
			keyManager.deactivate();
			executor.shutdown(); // 撮影処理のスレッドを終了させる
		} catch (ServiceException e) {
			log.error("キー監視の終了処理に失敗しました", e);
		}
	}

	/**
	 * ユーザーの設定値を取得する
	 * @return config
	 */
	public ConfigBean getConfigBean() {
		return config;
	}
}
