package com.github.rebi0395.instantsnap.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.Option;
import com.github.rebi0395.instantsnap.repository.PropertiesRepository;
import com.github.rebi0395.instantsnap.service.KeyEventListener;
import com.github.rebi0395.instantsnap.service.TakeSnapService;
import com.github.rebi0395.instantsnap.service.TriggerKeyManager;

/**
 * アプリ全体を司るコントローラークラス
 */
public class MainController implements KeyEventListener {
	
	private Option option;
	private PropertiesRepository editor;
	private TriggerKeyManager manager;
	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(MainController.class);
	
	public MainController(){
	}
	
	/**
	 * アプリ起動時の初期化処理を行います
	 */
	public void init() {
		try {
			this.option = new Option();
			this.editor = new PropertiesRepository();
			this.editor.read(option);
			this.manager = new TriggerKeyManager(option, this);
			this.manager.activate();

		// チェック済例外
		} catch (ServiceException e) {
			log.error("アプリの初期化に失敗しました", e);
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
				snap.execute(option);
				
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
}
