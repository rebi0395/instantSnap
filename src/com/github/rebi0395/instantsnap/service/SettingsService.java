package com.github.rebi0395.instantsnap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.repository.PropertiesRepository;

/**
 * JavaFX設定画面で使用する設定値と保存処理を管理するクラスです。
 * 撮影、キー監視、通知などの常駐機能は保持しません。
 */
public final class SettingsService {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SettingsService.class);

	/** ユーザーの設定値 */
	private final ConfigBean config;

	/** プロパティファイルの読込・保存処理 */
	private final PropertiesRepository repository;

	/** メッセージを多言語変換するクラス */
	private final MessageKeyConverter converter;

	/** 保存先パスの検査に使用するクラス */
	private final FilePathService filePathService;

	/**
	 * プロパティファイルから設定値を読み込み、設定画面用の依存関係を生成します。
	 *
	 * @throws ServiceException 設定値の読込に失敗した場合
	 */
	public SettingsService() throws ServiceException {
		this.config = new ConfigBean();
		this.repository = new PropertiesRepository();
		this.filePathService = new FilePathService();

		repository.read(config);
		this.converter = new MessageKeyConverter(config.getLanguageType());
	}

	/** プロパティファイルへ現在の設定値を保存します。 */
	public void save() {
		try {
			repository.save(config);
		} catch (ServiceException e) {
			log.error("設定の更新に失敗しました", e);
		}
	}

	/**
	 * ユーザーの設定値を取得します。
	 *
	 * @return ユーザーの設定値
	 */
	public ConfigBean getConfigBean() {
		return config;
	}

	/**
	 * メッセージ変換クラスを取得します。
	 *
	 * @return メッセージ変換クラス
	 */
	public MessageKeyConverter getMessageKeyConverter() {
		return converter;
	}

	/**
	 * 保存先パスの検査に使用するクラスを取得します。
	 *
	 * @return ファイルパス処理クラス
	 */
	public FilePathService getFilePathService() {
		return filePathService;
	}
}
