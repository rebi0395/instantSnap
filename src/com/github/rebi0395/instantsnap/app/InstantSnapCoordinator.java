package com.github.rebi0395.instantsnap.app;

import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.listener.RangeSelectionListener;
import com.github.rebi0395.instantsnap.listener.SnapKeyEventListener;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.bean.SnapResultBean;
import com.github.rebi0395.instantsnap.model.enums.AfterSnapActionType;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.model.enums.SnapAreaType;
import com.github.rebi0395.instantsnap.model.enums.SnapRouteType;
import com.github.rebi0395.instantsnap.model.enums.ThemeType;
import com.github.rebi0395.instantsnap.repository.PropertiesRepository;
import com.github.rebi0395.instantsnap.service.ActiveWindowResolver;
import com.github.rebi0395.instantsnap.service.MouseCursorService;
import com.github.rebi0395.instantsnap.service.FileOpenService;
import com.github.rebi0395.instantsnap.service.FilePathService;
import com.github.rebi0395.instantsnap.service.SnapAreaResolver;
import com.github.rebi0395.instantsnap.service.SnapSaveService;
import com.github.rebi0395.instantsnap.service.SoundPlayer;
import com.github.rebi0395.instantsnap.service.TakeSnapService;
import com.github.rebi0395.instantsnap.service.TmpFolderService;
import com.github.rebi0395.instantsnap.view.swing.dialog.RangeSelectionDialog;
import com.github.rebi0395.instantsnap.view.swing.presenter.SnapNotificationPresenter;

/**
 * 対応するサービスクラスに処理を振り分ける、司令塔クラス
 */
public class InstantSnapCoordinator implements RangeSelectionListener, SnapKeyEventListener, AutoCloseable {

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
	/** マウスカーソルを書き込むクラス */
	private MouseCursorService mouseCursorService;

	/** 撮影結果を表示するSwing通知 */
	private final SnapNotificationPresenter notificationPresenter;
	/** 範囲選択画面を表示するクラス */
	private RangeSelectionDialog rangeSelectionDialog;

	/** 範囲選択画面を表示中ならtrue */
	private volatile boolean rangeSelectionModeActive = false;
	/** ドラッグ範囲 */
	private volatile Rectangle rangeSelection;

	/** スレッドセーフに撮影処理を呼び出すためのクラス */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	/** 設定値の保存処理と、撮影処理の同時呼び出しを防止するオブジェクト */
	private final Object configLock = new Object();
	/** 終了処理の二重実行を防止するフラグ */
	private final AtomicBoolean closed = new AtomicBoolean(false);

	/**
	 * 常駐処理に必要な撮影サービスとSwing通知を生成し、設定を読み込みます。
	 * メインウィンドウとキー監視の起動・終了はInstantSnapAppが管理します。
	 */
	public InstantSnapCoordinator() {
		// インスタンスを生成
		this.config = new ConfigBean();
		this.repository = new PropertiesRepository();
		this.tmpFolder = new TmpFolderService();
		this.filePathService = new FilePathService();
		this.areaResolver = new SnapAreaResolver(new ActiveWindowResolver());
		this.fileOpenService = new FileOpenService();
		this.soundPlayer = new SoundPlayer();
		this.mouseCursorService = new MouseCursorService();

		// 依存性の注入があるクラス
		this.converter = new MessageKeyConverter(config.getLanguageType());
		this.saveService = new SnapSaveService(filePathService);
		this.rangeSelectionDialog = new RangeSelectionDialog(config, converter, this);
		this.takeSnapService = new TakeSnapService(areaResolver, saveService, soundPlayer, mouseCursorService);

		// 通知が繰り返し使うサービスは生成時に渡し、撮影時は結果だけを渡す。
		this.notificationPresenter = new SnapNotificationPresenter(fileOpenService, converter);

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
			// 前回終了時に残った一時ファイルを整理する。
			tmpFolder.cleanTmpDirectory();

		} catch (ServiceException e) {
			// チェック済例外
			log.error("アプリの初期化に失敗しました", e);
		} catch (Exception e) {
			// 非チェック例外
			log.error("想定外のエラーが発生しました", e);
		}
	}

	/**
	 * 設定画面または直接編集で保存されたプロパティを再読込します。
	 * 撮影との同時更新を避けるため、設定読込と言語反映を同じロック内で行います。
	 *
	 * @return 再読込に成功した場合true。終了処理中または読込に失敗した場合false
	 */
	public boolean reloadConfig() {
		if (closed.get()) {
			return false;
		}

		synchronized (configLock) {
			try {
				// キー監視は同じConfigBeanを参照しているため、停止せずに設定値だけ更新する。
				repository.read(config);
				converter.changeLanguage(config.getLanguageType());
				return true;
			} catch (ServiceException e) {
				log.error("設定の再読み込みに失敗しました", e);
				return false;
			}
		}
	}

	/**
	 * ミニウィンドウで選択された言語を保存し、常駐側へ即時反映します。
	 *
	 * @param languageType 新しい言語設定。nullの場合は更新しない
	 * @return 保存と反映に成功した場合、または変更が不要な場合true
	 */
	public boolean updateLanguage(LanguageType languageType) {
		if (languageType == null || closed.get()) {
			return false;
		}

		synchronized (configLock) {
			LanguageType previousLanguage = config.getLanguageType();
			if (previousLanguage == languageType) {
				return true;
			}

			try {
				// 既存のテンプレート維持方式で、変更後の設定値を保存する。
				config.setLanguageType(languageType);
				repository.save(config);
				converter.changeLanguage(languageType);
				return true;
			} catch (ServiceException e) {
				config.setLanguageType(previousLanguage);
				// 保存できなかった言語を画面に残さないよう、常駐側の表示言語も戻す。
				converter.changeLanguage(previousLanguage);
				log.error("言語設定の更新に失敗しました", e);
				return false;
			}
		}
	}

	/**
	 * ミニウィンドウで選択された撮影範囲を保存します。
	 * 保存に失敗した場合はConfigBeanを変更前の値へ戻します。
	 *
	 * @param snapAreaType 新しい撮影範囲。nullの場合は更新しない
	 * @return 保存に成功した場合、または変更が不要な場合true
	 */
	public boolean updateSnapArea(SnapAreaType snapAreaType) {
		if (snapAreaType == null || closed.get()) {
			return false;
		}

		synchronized (configLock) {
			SnapAreaType previousType = config.getSnapAreaType();
			if (previousType == snapAreaType) {
				return true;
			}
			try {
				config.setSnapAreaType(snapAreaType);
				repository.save(config);
				return true;
			} catch (ServiceException e) {
				config.setSnapAreaType(previousType);
				log.error("撮影範囲設定の更新に失敗しました", e);
				return false;
			}
		}
	}

	/**
	 * 現在設定されている画像保存先フォルダを開きます。
	 * 未作成の保存先は撮影時と同じ規則で作成してから開きます。
	 */
	public void openSaveDirectory() {
		if (closed.get()) {
			return;
		}

		String savePath;
		boolean isTemporarySave;
		synchronized (configLock) {
			savePath = config.getSavePath();
			isTemporarySave = config.isTemporarySave();
		}
		fileOpenService.openDirectory(filePathService.resolveSaveDirectory(savePath, isTemporarySave));
	}

	/**
	 * ミニウィンドウで変更された通知表示設定を保存します。
	 * 保存に失敗した場合はConfigBeanを変更前の値へ戻します。
	 *
	 * @param enabled 撮影後の通知を表示する場合true
	 * @return 保存に成功した場合、または変更が不要な場合true
	 */
	public boolean updateNotificationEnabled(boolean enabled) {
		if (closed.get()) {
			return false;
		}

		synchronized (configLock) {
			boolean previousValue = config.getIsShowNotification();
			if (previousValue == enabled) {
				return true;
			}
			try {
				config.setIsShowNotification(enabled);
				repository.save(config);
				return true;
			} catch (ServiceException e) {
				config.setIsShowNotification(previousValue);
				log.error("通知表示設定の更新に失敗しました", e);
				return false;
			}
		}
	}

	/**
	 * このCoordinatorが所有する撮影・通知関連のリソースを解放します。
	 * キー監視の停止とプロセスの終了はInstantSnapAppが担当します。
	 * 複数回呼ばれても解放処理は一度だけ実行します。
	 */
	@Override
	public void close() {
		if (!closed.compareAndSet(false, true)) {
			return;
		}
		rangeSelectionDialog.close(); // Swingで表示している範囲選択画面を破棄
		notificationPresenter.close(); // 表示中の通知とタイマーを破棄
		executor.shutdown(); // 撮影処理の終了
		soundPlayer.close(); // 音声リソースの開放
		tmpFolder.cleanTmpDirectory(); // 一時保存ファイルの削除
	}

	/**
	 * 撮影要求時の範囲選択結果を退避し、撮影処理をキューへ登録します。
	 *
	 * @param snapRoute 撮影要求が発生した経路
	 */
	@Override
	public void requestSnap(SnapRouteType snapRoute) {
		if (closed.get()) {
			return;
		}
		if (rangeSelectionModeActive && rangeSelection == null) {
			// 範囲選択画面が表示中、かつ範囲が未確定
			return;
		}

		Rectangle snapRange;
		if (rangeSelection == null) {
			snapRange = null;
		} else {
			snapRange = new Rectangle(rangeSelection);
		}
		// 撮影要求へコピーした範囲を、次の選択や撮影へ持ち越さない。
		this.rangeSelection = null;

		// 範囲撮影後の画面状態を、自動終了設定に従って更新する。
		if (snapRange != null) {
			if (config.getIsCloseRangeSelectionAfterSnapEnabled()) {
				rangeSelectionDialog.close();
			} else if (snapRoute != SnapRouteType.RANGE_MODE) {
				// 手動撮影後もモードを継続する場合は、次の範囲選択待ちに戻す。
				rangeSelectionDialog.prepareNextSelection();
			}
		}

		// 別スレッド立ち上げ
		executor.submit(() -> {
			SnapResultBean result;
			AfterSnapData afterSnapData;
			synchronized (configLock) { // 排他制御
				try {
					// 撮影処理
					result = takeSnapService.execute(config, snapRange, snapRoute);

					// ロック中に撮影後処理に使う分の設定値を退避する
					afterSnapData = new AfterSnapData(
							new SnapResultBean(result),
							config.getIsShowNotification(),
							config.getIsNotificationSoundEnabled(),
							config.getAfterSnapActionType(),
							config.getThemeType(),
							config.getLanguageType());

				} catch (Exception e) {
					log.error("スクリーンショットの撮影または保存に失敗しました", e);
					return;
				}
			}

			// 撮影結果がある場合は、撮影後処理を行う
			if (afterSnapData.result().hasResult()) {
				afterSnapAction(afterSnapData);
			}

		});
	}
	
	/**
	 * 範囲トリガーが押されたとき呼ばれる。撮影範囲選択モードの無効・有効を切り替える
	 */
	@Override
	public void requestRangeSelection() {
		if (closed.get()) {
			return;
		}
		GraphicsDevice screenDevice = areaResolver.getGraphicsDeviceAtPointer();
		rangeSelectionDialog.switchRangeSelectionMode(screenDevice);
	}

	/**
	 * 範囲選択画面が開かれた場合に呼ばれる。選択範囲を破棄し、選択範囲表示フラグをonにする
	 */
	@Override
	public void onShown() {
		rangeSelection = null;
		rangeSelectionModeActive = true;
	}

	/**
	 * 範囲選択画面が閉じられた場合に呼ばれる。選択範囲を破棄し、選択範囲表示フラグをoffにする
	 */
	@Override
	public void onClosed() {
		rangeSelection = null;
		rangeSelectionModeActive = false;
	}

	/**
	 * 範囲選択結果を保持し、自動撮影が有効なら直ちに撮影を要求します。
	 *
	 * @param selectedArea 確定した撮影範囲。選択解除時はnull
	 */
	@Override
	public void updateRangeArea(Rectangle selectedArea) {
		if (selectedArea == null) {
			rangeSelection = null;
			return;
		}
		rangeSelection = new Rectangle(selectedArea);
		if (config.getIsAutoSnapAfterRangeSelectionEnabled()) {
			requestSnap(SnapRouteType.RANGE_MODE);
		}
	}

	/**
	 * 起動したJavaFX設定プロセスを内部ウィンドウとして外部撮影対象から除外します。
	 *
	 * @param processId 設定プロセスのID
	 */
	public void registerSettingsProcess(long processId) {
		areaResolver.registerSettingsProcess(processId);
	}

	/**
	 * 終了したJavaFX設定プロセスを内部ウィンドウの一覧から解除します。
	 *
	 * @param processId 終了した設定プロセスのID
	 */
	public void releaseSettingsProcess(long processId) {
		areaResolver.releaseSettingsProcess(processId);
	}

	/**
	 * 撮影後処理を実施する
	 *
	 * @param afterSnapData 撮影後処理の判定で使うrecord
	 */
	private void afterSnapAction(AfterSnapData afterSnapData) {

		try {
			// 通知より先に音を鳴らし、画面表示の準備による遅延を避ける。
			if (afterSnapData.soundNotificationEnabled()) {
				soundPlayer.playNotificationSound();
			}

			// 通知表示が有効な場合
			if (afterSnapData.showNotification()) {
				notificationPresenter.show(
						afterSnapData.result(),
						afterSnapData.themeType(),
						afterSnapData.languageType());
			}

			// 撮影後アクションを実行
			if (afterSnapData.result().getTargetPath() != null) {
				fileOpenService.execute(afterSnapData.afterSnapActionType(), afterSnapData.result().getTargetPath());
			}

		} catch (Exception e) {
			log.error("撮影後処理に失敗しました", e);
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
	 * メッセージキーをメッセージに変換するコンバーターを取得する
	 * @return converter
	 */
	public MessageKeyConverter getMessageKeyConverter() {
		return converter;
	}
	/**
	 * 撮影後の処理で使う変数をひとまとめにしたレコードクラス
	 *
	 * @param result 撮影結果
	 * @param showNotification 通知表示の有無
	 * @param soundNotificationEnabled 通知音の有無（撮影音とは別の設定）
	 * @param afterSnapActionType 撮影後動作
	 * @param themeType 撮影時点で選択されているテーマ
	 * @param languageType 撮影時点で選択されている言語
	 */
	private record AfterSnapData(
			SnapResultBean result,
			boolean showNotification,
			boolean soundNotificationEnabled,
			AfterSnapActionType afterSnapActionType,
			ThemeType themeType,
			LanguageType languageType) {
	}
}
