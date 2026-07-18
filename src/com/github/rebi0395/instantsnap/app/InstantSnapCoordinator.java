package com.github.rebi0395.instantsnap.app;

import java.awt.Rectangle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.SnapResultBean;
import com.github.rebi0395.instantsnap.model.enums.AfterSnapActionType;
import com.github.rebi0395.instantsnap.repository.PropertiesRepository;
import com.github.rebi0395.instantsnap.service.MouseCursorService;
import com.github.rebi0395.instantsnap.service.FileOpenService;
import com.github.rebi0395.instantsnap.service.FilePathService;
import com.github.rebi0395.instantsnap.service.SnapAreaResolver;
import com.github.rebi0395.instantsnap.service.SnapSaveService;
import com.github.rebi0395.instantsnap.service.SoundPlayer;
import com.github.rebi0395.instantsnap.service.TakeSnapService;
import com.github.rebi0395.instantsnap.service.TmpFolderService;
import com.github.rebi0395.instantsnap.service.TriggerKeyManager;
import com.github.rebi0395.instantsnap.service.TriggerKeyManager.KeyEventListener;
import com.github.rebi0395.instantsnap.view.RangeSelectionWindow;
import com.github.rebi0395.instantsnap.view.RangeSelectionWindow.RangeSelectionListener;
import com.github.rebi0395.instantsnap.view.presenter.SnapNotificationPresenter;

import javafx.application.Platform;

/**
 * 対応するサービスクラスに処理を振り分ける、司令塔クラス
 */
public class InstantSnapCoordinator implements KeyEventListener, RangeSelectionListener {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(InstantSnapCoordinator.class);

	/** ユーザーの設定値 */
	private final ConfigBean config;
	/** プロパティファイルのリポジトリ */
	private final PropertiesRepository repository;
	/** メッセージを多言語変換するクラス */
	private final MessageKeyConverter converter;

	/** 一時保存ディレクトリの管理クラス */
	private final TmpFolderService tmpFolder;
	/** ファイル名生成、ディレクトリ解決を行うクラス */
	private final FilePathService filePathService;
	/** 撮影トリガーの管理クラス */
	private final TriggerKeyManager keyManager;

	/** 撮影範囲を解決するクラス */
	private final SnapAreaResolver areaResolver;
	/** 画像を保存するクラス */
	private final SnapSaveService saveService;
	/** 全体の撮影処理クラス */
	private final TakeSnapService takeSnapService;

	/** ファイルを開くクラス */
	private final FileOpenService fileOpenService;
	/** 効果音再生クラス */
	private final SoundPlayer soundPlayer;
	/** 撮影通知を表示するクラス */
	private final SnapNotificationPresenter notificationPresenter;
	/** マウスカーソルを書き込むクラス */
	private MouseCursorService mouseCursorService;

	/** スレッドセーフに撮影処理を呼び出すためのクラス */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	/** 設定値の保存処理と、撮影処理の同時呼び出しを防止するオブジェクト */
	private final Object configLock = new Object();

	/** コンストラクタ 初期化処理を実行する */
	public InstantSnapCoordinator(SnapNotificationPresenter notificationPresenter) {
		// インスタンスを生成
		this.config = new ConfigBean();
		this.repository = new PropertiesRepository();
		this.tmpFolder = new TmpFolderService();
		this.filePathService = new FilePathService();
		this.areaResolver = new SnapAreaResolver();
		this.fileOpenService = new FileOpenService();
		this.soundPlayer = new SoundPlayer();
		this.mouseCursorService = new MouseCursorService();

		// 依存性の注入があるクラス
		this.converter = new MessageKeyConverter(config.getLanguageType());
		this.saveService = new SnapSaveService(filePathService);
		this.keyManager = new TriggerKeyManager(config, this);
		this.takeSnapService = new TakeSnapService(areaResolver, saveService, soundPlayer, mouseCursorService);

		// Stage情報を持つクラス
		this.notificationPresenter = notificationPresenter;

		init();
	}

	/**
	 * アプリ起動時の初期化処理を行う
	 */
	public void init() {
		try {
			// ユーザーの設定値をプロパティファイルを読み込み初期化
			repository.read(config);
			// 言語設定を初期化
			converter.changeLanguage(config.getLanguageType());
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
			SnapResultBean result;

			// 撮影後、使う変数
			boolean showNotification;
			boolean soundNotificationEnabled;
			AfterSnapActionType afterSnapActionType;

			synchronized (configLock) { // 排他制御
				try {
					// 撮影処理
					result = takeSnapService.execute(config);

					// ロック中に設定値を退避する
					showNotification = config.getIsShowNotification();
					soundNotificationEnabled = config.getIsNotificationSoundEnabled();
					afterSnapActionType = config.getAfterSnapActionType();

				} catch (Exception e) {
					log.error("スクリーンショットの撮影または保存に失敗しました", e);
					return;
				}
			}

			// 撮影結果がない場合、早期リターン
			if (!result.hasResult()) {
				return;
			}

			try {
				// 通知表示が有効な場合
				if (showNotification) {
					notificationPresenter.show(result, fileOpenService, converter);
				}

				// 通知音が有効な場合
				if (soundNotificationEnabled) {
					soundPlayer.playNotificationSound();
				}

				// 撮影後アクションを実行
				if (result.getTargetPath() != null) {
					fileOpenService.execute(afterSnapActionType, result.getTargetPath());
				}

			} catch (Exception e) {
				log.error("撮影後処理に失敗しました", e);
			}
		});
	}

	/**
	 * 範囲トリガーが押されたとき、撮影範囲選択モードに以降する
	 */
	@Override
	public void requestRangeSelection() {
		RangeSelectionWindow range = new RangeSelectionWindow();
		range.show(this);
	}

	@Override
	public void onSelected(Rectangle selectedArea) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void onCancelled() {
		// TODO 自動生成されたメソッド・スタブ

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

	/**
	 * メッセージキーをメッセージに変換するコンバーターを取得する
	 * @return converter
	 */
	public MessageKeyConverter getMessageKeyConverter() {
		return converter;
	}

	/**
	 * ファイル名生成、ディレクトリ解決を行うクラスを取得する
	 * @return filePathService
	 */
	public FilePathService getFilePathService() {
		return filePathService;
	}
}
