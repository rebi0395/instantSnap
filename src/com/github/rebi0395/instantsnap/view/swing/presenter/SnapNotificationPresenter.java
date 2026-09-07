package com.github.rebi0395.instantsnap.view.swing.presenter;

import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsDevice.WindowTranslucency;
import java.awt.Insets;
import java.awt.IllegalComponentStateException;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.MessageKeyConstant;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.model.bean.SnapResultBean;
import com.github.rebi0395.instantsnap.service.FileOpenService;
import com.github.rebi0395.instantsnap.view.swing.panel.SnapNotificationPanel;

/**
 * 撮影後に表示するSwing通知の表示制御を行うプレゼンターです。
 * <p>
 * 通知パネルの部品生成と描画は {@link SnapNotificationPanel} に委譲し、
 * このクラスでは表示データ、通知操作、表示時間、アニメーションを管理します。
 * showは撮影スレッドから呼べます。通知ウィンドウとタイマーはEDT上で操作します。
 * </p>
 */
public final class SnapNotificationPresenter {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SnapNotificationPresenter.class);

	/** 通知表示時間 */
	private static final int NOTIFICATION_DISPLAY_MILLIS = 5_000;

	/** 一時保存時の通知表示時間 */
	private static final int NOTIFICATION_DISPLAY_LONG_MILLIS = 8_000;

	/** 通知表示位置の画面端からの余白 */
	private static final int NOTIFICATION_MARGIN = 20;

	/** 通知フェードイン移動量 */
	private static final int NOTIFICATION_FADE_IN_DISTANCE = 40;

	/** 通知フェードアウト移動量 */
	private static final int NOTIFICATION_FADE_OUT_DISTANCE = 40;

	/** 通知フェードイン・フェードアウト時間 */
	private static final int NOTIFICATION_ANIMATION_MILLIS = 250;

	/** アニメーションの更新間隔 */
	private static final int ANIMATION_INTERVAL_MILLIS = 16;

	/** 撮影スレッドとEDTの間で最新通知を受け渡すためのロック */
	private final Object notificationRequestLock = new Object();

	/** アプリ終了まで再利用する通知ウィンドウ */
	private JWindow notificationWindow;

	/** アプリ終了まで再利用する通知パネル */
	private SnapNotificationPanel notificationPanel;

	/** EDTでまだ表示していない最新の通知情報 */
	private NotificationRequest pendingNotificationRequest;

	/** 最新通知を表示する処理がEDTへ登録済みの場合true */
	private boolean notificationUpdateScheduled;

	/** 古いタイマーイベントが新しい通知を変更しないようにする世代番号 */
	private long notificationGeneration;

	/** 終了後に新しい通知を受け付けないためのフラグ */
	private volatile boolean closed;

	/** 表示・非表示アニメーション用タイマー */
	private Timer animationTimer;

	/** 自動非表示用タイマー */
	private Timer dismissTimer;

	/** ウィンドウ透明度が利用できる場合true */
	private boolean opacitySupported = true;

	/** 現在の通知から開く保存先パス */
	private Path currentTargetPath;

	/** 保存先フォルダを開くサービス */
	private final FileOpenService fileOpenService;

	/** 通知文言を現在の言語へ変換するクラス */
	private final MessageKeyConverter converter;

	/**
	 * 通知の表示と保存先操作に必要なクラスを受け取ります。
	 *
	 * @param fileOpenService 保存先フォルダを開くサービス
	 * @param converter 常駐側と共有するメッセージ変換クラス
	 */
	public SnapNotificationPresenter(FileOpenService fileOpenService, MessageKeyConverter converter) {
		this.fileOpenService = fileOpenService;
		this.converter = converter;
	}

	/**
	 * 撮影結果から表示データを作り、EDTへ通知表示を依頼します。
	 * 連続撮影の場合、表示中の通知は最新の通知で置き換えます。
	 *
	 * @param result 撮影結果。nullまたは保存・コピー結果がない場合は表示しない
	 */
	public void show(SnapResultBean result) {
		if (result == null || !result.hasResult()) {
			// 通知の表示が不要な場合、早期リターン
			return;
		}

		// 撮影スレッドのBeanをSwingスレッドへ持ち込まないよう、表示に必要な値だけコピーする。
		NotificationRequest notificationRequest = createNotificationData(result);

		boolean scheduleNotificationUpdate = false;
		synchronized (notificationRequestLock) {
			if (closed) {
				return;
			}

			// EDTが混雑していても通知要求を蓄積せず、未表示分を最新の1件で置き換える。
			pendingNotificationRequest = notificationRequest;
			if (!notificationUpdateScheduled) {
				notificationUpdateScheduled = true;
				scheduleNotificationUpdate = true;
			}
		}

		if (scheduleNotificationUpdate) {
			SwingUtilities.invokeLater(() -> {
				showLatestNotification();
			});
		}
	}

	/**
	 * EDTに登録された複数の通知要求を最新の1件へまとめて表示します。
	 * 表示中に次の撮影が完了した場合は、その1件だけを改めてEDTへ登録します。
	 */
	private void showLatestNotification() {
		NotificationRequest notificationRequest;
		synchronized (notificationRequestLock) {
			if (closed) {
				pendingNotificationRequest = null;
				notificationUpdateScheduled = false;
				return;
			}
			notificationRequest = pendingNotificationRequest;
			pendingNotificationRequest = null;
			notificationUpdateScheduled = false;
		}

		if (notificationRequest != null) {
			showNotification(notificationRequest);
		}
	}

	/**
	 * 撮影結果を元に、通知に必要な情報を通知情報レコードにまとめて返却します。
	 *
	 * @param result 撮影結果クラス
	 * @return 画像本体を保持しない、文言と保存先だけの通知情報
	 */
	private NotificationRequest createNotificationData(SnapResultBean result) {

		// 通知に表示するメッセージリストを作成する。
		List<String> noticeMessageList = new ArrayList<String>();
		if (result.getIsImageSaved()) {
			// 画像保存に成功
			noticeMessageList.add(converter.getMessage(
					MessageKeyConstant.NOTIFICATION_IMAGE_SAVED,
					"Image saved."));
		}
		if (result.getIsClipboardCopied()) {
			// クリップボードコピーに成功
			noticeMessageList.add(converter.getMessage(
					MessageKeyConstant.NOTIFICATION_CLIPBOARD_SAVED,
					"Copied to clipboard."));
		}
		if (result.getIsTmpSaved()) {
			// 一時保存に成功
			noticeMessageList.add(converter.getMessage(
					MessageKeyConstant.NOTIFICATION_TMP_IMAGE_SAVED,
					"Image saved to the temporary folder."));
			noticeMessageList.add(converter.getMessage(
					MessageKeyConstant.NOTIFICATION_TMP_WARN,
					"Temporary images are deleted when the app exits.\nMove files you want to keep."));
		}

		// 通知クリックでフォルダが開けることを伝える文言を取得する。
		String openMessage = converter.getMessage(
				MessageKeyConstant.NOTIFICATION_OPEN_SAVE_DIR,
				"Open save folder");

		return new NotificationRequest(
				List.copyOf(noticeMessageList),
				openMessage,
				result.getTargetPath(),
				result.isOpenActionAvailable(),
				result.getIsTmpSaved());
	}

	/**
	 * EDT上で再利用中の通知内容を更新し、ポインターがある画面の右下に表示します。
	 *
	 * @param notificationRequest 通知表示に必要な情報
	 */
	private void showNotification(NotificationRequest notificationRequest) {
		if (closed) {
			return;
		}

		// 前回のアニメーションを止め、同じウィンドウとパネルへ最新内容を設定する。
		stopNotificationTimers();
		notificationGeneration++;
		long generation = notificationGeneration;
		JWindow window = getOrCreateNotificationWindow();

		// 保存先を開ける通知の場合だけ、Presenterが操作に必要な値を保持する。
		currentTargetPath = null;
		boolean openDirectoryAvailable = false;
		if (notificationRequest.openDirectoryAvailable() && notificationRequest.targetPath() != null) {
			currentTargetPath = notificationRequest.targetPath();
			openDirectoryAvailable = true;
		}

		// 再利用する通知パネルへ表示に必要な値を設定する。
		notificationPanel.setup(
				notificationRequest.messageList(),
				notificationRequest.openMessage(),
				openDirectoryAvailable);

		window.pack();

		// タスクバーを除いた表示可能領域を取得し、通知の右下座標を算出する。
		Rectangle usableBounds = resolveUsableScreenBounds(window.getGraphicsConfiguration());
		int endX = usableBounds.x + usableBounds.width - window.getWidth() - NOTIFICATION_MARGIN;
		int endY = usableBounds.y + usableBounds.height - window.getHeight() - NOTIFICATION_MARGIN;
		int startY = endY + NOTIFICATION_FADE_IN_DISTANCE;

		window.setLocation(endX, startY);
		setWindowOpacity(window, 0.0f);
		window.setVisible(true); // 表示後にフェードインを開始する。

		showWithFadeIn(window, notificationRequest.isTmpSaved(), generation);
	}

	/**
	 * 通知ウィンドウとパネルを初回だけ生成し、以降の撮影では同じインスタンスを返します。
	 *
	 * @return 表示内容を差し替えて再利用する通知ウィンドウ
	 */
	private JWindow getOrCreateNotificationWindow() {
		if (notificationWindow != null) {
			return notificationWindow;
		}

		JWindow createdWindow = new JWindow();
		createdWindow.setName("InstantSnapNotification");
		createdWindow.setType(Type.POPUP);
		createdWindow.setAlwaysOnTop(true);
		createdWindow.setFocusableWindowState(false);
		try {
			createdWindow.setBackground(new Color(0, 0, 0, 0));
		} catch (UnsupportedOperationException e) {
			// 透明背景が使えない環境でも、通常の白背景で通知表示を継続する。
			log.debug("通知ウィンドウの透明背景を利用できません", e);
		}

		notificationPanel = new SnapNotificationPanel(this);
		createdWindow.setContentPane(notificationPanel);
		notificationWindow = createdWindow;
		return createdWindow;
	}

	/**
	 * 通知パネルからEDT上で呼び出し、通知を閉じて保存先フォルダを開きます。
	 * クリップボードだけの通知など、保存先を持たない場合は何もしません。
	 */
	public void openSaveDirectory() {
		// 通知破棄で参照を消しても、今回開くパスはこの呼び出し内で保持する。
		Path targetPath = currentTargetPath;
		if (targetPath == null) {
			return;
		}

		hideWithFadeOut();
		fileOpenService.openDirectory(targetPath);
	}

	/**
	 * 閉じるボタンから現在の通知を閉じます。
	 */
	public void closeNotification() {
		hideWithFadeOut();
	}

	/**
	 * 配置済みの通知を下から上へ移動しながらフェードイン表示します。
	 *
	 * @param window 開始位置に配置済みの通知ウィンドウ
	 * @param temporarySaved 一時保存の通知として表示時間を延長する場合true
	 * @param generation 表示対象となる通知の世代番号
	 */
	private void showWithFadeIn(JWindow window, boolean temporarySaved, long generation) {
		// 座標を重複して渡さず、表示開始時のウィンドウから一度だけ取得する。
		int x = window.getX();
		int startY = window.getY();
		int endY = startY - NOTIFICATION_FADE_IN_DISTANCE;
		long startNanos = System.nanoTime();
		animationTimer = new Timer(ANIMATION_INTERVAL_MILLIS, event -> {
			Timer sourceTimer = (Timer) event.getSource();
			if (sourceTimer != animationTimer || generation != notificationGeneration || closed) {
				sourceTimer.stop();
				return;
			}

			double progress = calculateProgress(startNanos);
			// ウィンドウを下から上へ移動しながら透明度を上げる。
			window.setLocation(x, interpolate(startY, endY, progress));
			setWindowOpacity(window, (float) progress);

			if (progress >= 1.0) {
				sourceTimer.stop();
				animationTimer = null;
				int displayMillis = NOTIFICATION_DISPLAY_MILLIS;
				if (temporarySaved) {
					// 一時保存のときは文言が長いため、表示時間を長くする。
					displayMillis = NOTIFICATION_DISPLAY_LONG_MILLIS;
				}
				// 指定時間経過後に自動で閉じるタイマーを開始する。
				startDismissTimer(displayMillis, generation);
			}
		});
		animationTimer.start();
	}

	/**
	 * フェードイン完了後、指定時間で通知を閉じる1回限りのタイマーを開始します。
	 *
	 * @param delayMillis 通知を表示しておく時間（ミリ秒）
	 * @param generation 表示対象となる通知の世代番号
	 */
	private void startDismissTimer(int delayMillis, long generation) {
		dismissTimer = new Timer(delayMillis, event -> {
			Timer sourceTimer = (Timer) event.getSource();
			if (sourceTimer != dismissTimer || generation != notificationGeneration || closed) {
				sourceTimer.stop();
				return;
			}
			dismissTimer = null;
			hideWithFadeOut();
		});
		dismissTimer.setRepeats(false);
		dismissTimer.start();
	}

	/**
	 * 現在の通知を下へ移動しながらフェードアウトします。
	 */
	private void hideWithFadeOut() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> {
				hideWithFadeOut();
			});
			return;
		}

		JWindow window = notificationWindow;
		if (window == null || !window.isVisible()) {
			currentTargetPath = null;
			return;
		}
		stopTimer(dismissTimer);
		dismissTimer = null;
		stopTimer(animationTimer);

		int startX = window.getX();
		int startY = window.getY();
		int endY = startY + NOTIFICATION_FADE_OUT_DISTANCE;
		float startOpacity = readWindowOpacity(window);
		long startNanos = System.nanoTime();
		long generation = notificationGeneration;

		animationTimer = new Timer(ANIMATION_INTERVAL_MILLIS, event -> {
			Timer sourceTimer = (Timer) event.getSource();
			if (sourceTimer != animationTimer || generation != notificationGeneration || closed) {
				sourceTimer.stop();
				return;
			}

			double progress = calculateProgress(startNanos);
			// ウィンドウを上から下へ移動しながら透明度を下げる。
			window.setLocation(startX, interpolate(startY, endY, progress));
			setWindowOpacity(window, (float) (startOpacity * (1.0 - progress)));

			if (progress >= 1.0) {
				sourceTimer.stop();
				animationTimer = null;
				window.setVisible(false);
				currentTargetPath = null;
			}
		});
		animationTimer.start();
	}

	/**
	 * 単調増加する時計からアニメーションの進捗を算出します。
	 *
	 * @param startNanos 開始時にSystem.nanoTimeで取得した時刻（ナノ秒）
	 * @return 0.0～1.0の進捗率。設定時間を過ぎた場合は1.0
	 */
	private double calculateProgress(long startNanos) {
		double elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000.0;
		return Math.min(1.0, elapsedMillis / NOTIFICATION_ANIMATION_MILLIS);
	}

	/**
	 * 開始座標と終了座標の間を進捗率で補間します。
	 *
	 * @param start 開始座標
	 * @param end 終了座標
	 * @param progress 0.0～1.0の進捗率
	 * @return 四捨五入した現在の座標
	 */
	private int interpolate(int start, int end, double progress) {
		return (int) Math.round(start + ((end - start) * progress));
	}

	/**
	 * ポインターがある画面からタスクバーなどを除いた領域を取得します。
	 *
	 * @param fallbackConfiguration ポインター位置を取得できない場合に使う画面情報
	 * @return 画面全体の座標系で表した通知配置可能領域
	 */
	private Rectangle resolveUsableScreenBounds(GraphicsConfiguration fallbackConfiguration) {
		GraphicsConfiguration configuration = fallbackConfiguration;
		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		if (pointerInfo != null && pointerInfo.getDevice() != null) {
			configuration = pointerInfo.getDevice().getDefaultConfiguration();
		}

		Rectangle bounds = new Rectangle(configuration.getBounds());
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
		bounds.x += screenInsets.left;
		bounds.y += screenInsets.top;
		bounds.width -= screenInsets.left + screenInsets.right;
		bounds.height -= screenInsets.top + screenInsets.bottom;
		return bounds;
	}

	/**
	 * 対応環境の場合だけウィンドウ透明度を変更します。
	 * 非対応と判明した後は、以降の透明度操作を省略して表示を継続します。
	 *
	 * @param window 透明度を変更する通知ウィンドウ
	 * @param opacity 0.0（透明）～1.0（不透明）。範囲外の値は上下限に補正
	 */
	private void setWindowOpacity(JWindow window, float opacity) {
		if (!opacitySupported) {
			return;
		}
		try {
			GraphicsDevice device = window.getGraphicsConfiguration().getDevice();
			if (!device.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT)) {
				opacitySupported = false;
				return;
			}
			window.setOpacity(Math.max(0.0f, Math.min(1.0f, opacity)));
		} catch (IllegalComponentStateException | UnsupportedOperationException e) {
			opacitySupported = false;
			log.debug("通知ウィンドウの透明度変更を利用できません", e);
		}
	}

	/**
	 * フェードアウト開始時の透明度を取得します。
	 *
	 * @param window 透明度を取得する通知ウィンドウ
	 * @return 現在の透明度。透明度非対応環境の場合は1.0
	 */
	private float readWindowOpacity(JWindow window) {
		if (!opacitySupported) {
			return 1.0f;
		}
		try {
			return window.getOpacity();
		} catch (IllegalComponentStateException | UnsupportedOperationException e) {
			opacitySupported = false;
			return 1.0f;
		}
	}

	/**
	 * 通知タイマーを停止します。
	 * ウィンドウとパネルは次回の通知で再利用するため、ここでは破棄しません。
	 */
	private void stopNotificationTimers() {
		stopTimer(animationTimer);
		stopTimer(dismissTimer);
		animationTimer = null;
		dismissTimer = null;
	}

	/**
	 * アプリ終了時に、再利用していた通知ウィンドウ、パネル、タイマーを破棄します。
	 */
	private void disposeNotificationResources() {
		notificationGeneration++;
		stopNotificationTimers();
		if (notificationWindow != null) {
			notificationWindow.setVisible(false);
			notificationWindow.dispose();
			notificationWindow = null;
		}
		notificationPanel = null;
		currentTargetPath = null;
	}

	/**
	 * Swingタイマーを停止します。呼び出し元が保持する参照は変更しません。
	 *
	 * @param timer 停止するタイマー。nullの場合は何もしない
	 */
	private void stopTimer(Timer timer) {
		if (timer != null) {
			timer.stop();
		}
	}

	/**
	 * 現在表示している通知と関連タイマーを、アニメーションなしで破棄します。
	 * EDT以外から呼ばれた場合はEDTへ処理を予約し、完了を待たずに戻ります。
	 */
	public void close() {
		synchronized (notificationRequestLock) {
			closed = true;
			pendingNotificationRequest = null;
			notificationUpdateScheduled = false;
		}

		if (SwingUtilities.isEventDispatchThread()) {
			disposeNotificationResources();
		} else {
			SwingUtilities.invokeLater(() -> {
				disposeNotificationResources();
			});
		}
	}

	/**
	 * 通知表示に必要な情報をまとめたレコードです。
	 *
	 * @param messageList 通知メッセージリスト
	 * @param openMessage 保存先フォルダを開く表示文言
	 * @param targetPath 保存先パス
	 * @param openDirectoryAvailable 通知クリックで保存先を開ける場合true
	 * @param isTmpSaved 一時保存の場合true
	 */
	private record NotificationRequest(
			List<String> messageList,
			String openMessage,
			Path targetPath,
			boolean openDirectoryAvailable,
			boolean isTmpSaved) {
	}
}
