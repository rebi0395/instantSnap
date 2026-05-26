package com.github.rebi0395.instantsnap.service;

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
	private TriggerKeyManager manager;
	
	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(ServiceFacade.class);
	
	public ServiceFacade(){
	}
	
	/**
	 * アプリ起動時の初期化処理を行う
	 */
	public void init() {
		try {
			this.config = new ConfigBean();
			this.editor = new PropertiesRepository();
			editor.read(config);
			this.manager = new TriggerKeyManager(this);
			manager.activate(config);

		// チェック済例外
		} catch (ServiceException e) {
			log.error("アプリの初期化に失敗しました", e);
		// 非チェック例外
		} catch (Exception e) {
			log.error("想定外のエラーが発生しました", e);
		}
	}
	
	/**
	 * コンフィグに変更がある場合などに呼び出し、再度起動します。
	 */
	public void reset() {
		try {
			editor.save(config); // プロパティファイルを更新
			if (manager.isKeyListenerRunning()) {
				manager.deactivate(); // 一度キー監視を停止する
			}
			this.manager.activate(config); // 再起動

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
		new Thread(() -> {
			TakeSnapService snap = new TakeSnapService();
    		try {
    			// 撮影処理
				snap.execute(config);
				
			} catch (ServiceException e) {
				log.error("スクリーンショットの撮影または保存に失敗しました", e);
			}
		}).start();
	}

	/**
	 * pauseキーが押されたとき、キー監視を終了する
	 */
	@Override
	public void requestExit() {
		try {
			manager.deactivate();
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

	/**
	 * ユーザーの設定値インスタンスをセットする
	 * @param config セットする
	 */
	public void setConfigBean(ConfigBean configBean) {
		this.config = configBean;
	}
}
