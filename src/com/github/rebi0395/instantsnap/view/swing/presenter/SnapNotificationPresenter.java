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
 * このクラスでは表示データ、通知アクション、表示時間、アニメーションを管理します。
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

	/** 現在表示している通知ウィンドウ */
	private JWindow currentWindow;

	/** 表示・非表示アニメーション用タイマー */
	private Timer animationTimer;

	/** 自動非表示用タイマー */
	private Timer dismissTimer;

	/** ウィンドウ透明度が利用できる場合true */
	private boolean opacitySupported = true;

	/**
	 * 通知を表示します。
	 *
	 * @param result 撮影結果インスタンス
	 * @param fileOpen ファイルを開く処理を持つインスタンス
	 * @param converter メッセージキーをメッセージに変換するクラス
	 */
	public void show(SnapResultBean result, FileOpenService fileOpen, MessageKeyConverter converter) {
		if (result == null || !result.hasResult()) {
			// 通知の表示が不要な場合、早期リターン
			return;
		}

		// 撮影スレッドのBeanをSwingスレッドへ持ち込まないよう、表示に必要な値だけコピーする。
		NotificationRequest notificationRequest = createNotificationData(result, converter);

		// Swingの専用スレッドに通知表示を依頼する。
		SwingUtilities.invokeLater(() -> {
			showNotification(notificationRequest, fileOpen);
		});
	}

	/**
	 * 撮影結果を元に、通知に必要な情報を通知情報レコードにまとめて返却します。
	 *
	 * @param result 撮影結果クラス
	 * @param converter メッセージ取得クラス
	 * @return 通知情報
	 */
	private NotificationRequest createNotificationData(
			SnapResultBean result,
			MessageKeyConverter converter) {

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
	 * 通知画面を作成し、画面右下に表示します。
	 *
	 * @param notificationRequest 通知表示に必要な情報
	 * @param fileOpen ファイルを開く処理を持つインスタンス
	 */
	private void showNotification(NotificationRequest notificationRequest, FileOpenService fileOpen) {
		// 連続撮影時に通知とタイマーが蓄積しないよう、前回分を先に破棄する。
		disposeCurrentNotification();

		// 通知を表示するウィンドウを作成する。
		JWindow window = new JWindow();
		window.setName("InstantSnapNotification");
		window.setType(Type.POPUP);
		window.setAlwaysOnTop(true);
		window.setFocusableWindowState(false);
		try {
			window.setBackground(new Color(0, 0, 0, 0));
		} catch (UnsupportedOperationException e) {
			// 透明背景が使えない環境でも、通常の白背景で通知表示を継続する。
			log.debug("通知ウィンドウの透明背景を利用できません", e);
		}

		// 通知が持つアクションを作成する。
		NotificationAction notificationAction = createNotificationAction(notificationRequest, fileOpen);

		// 通知パネルを作成し、文言とアクションをまとめて設定する。
		SnapNotificationPanel notificationPanel = new SnapNotificationPanel();
		notificationPanel.setup(
				notificationRequest.messageList(),
				notificationRequest.openMessage(),
				notificationAction.openAction(),
				notificationAction.closeAction());

		window.setContentPane(notificationPanel); // 通知ウィンドウにパネルを設定する。
		window.pack();

		// タスクバーを除いた表示可能領域を取得し、通知の右下座標を算出する。
		Rectangle usableBounds = resolveUsableScreenBounds(window.getGraphicsConfiguration());
		int endX = usableBounds.x + usableBounds.width - window.getWidth() - NOTIFICATION_MARGIN;
		int endY = usableBounds.y + usableBounds.height - window.getHeight() - NOTIFICATION_MARGIN;
		int startY = endY + NOTIFICATION_FADE_IN_DISTANCE;

		currentWindow = window;
		window.setLocation(endX, startY);
		setWindowOpacity(window, 0.0f);
		window.setVisible(true); // 表示後にフェードインを開始する。

		showWithFadeIn(window, endX, startY, endY, notificationRequest.isTmpSaved());
	}

	/**
	 * 通知が持つアクションを作成し、通知アクションレコードにまとめて返却します。
	 *
	 * @param notificationRequest 通知情報
	 * @param fileOpen ファイルを開く処理を持つインスタンス
	 * @return 通知アクションを持つレコード
	 */
	private NotificationAction createNotificationAction(
			NotificationRequest notificationRequest,
			FileOpenService fileOpen) {

		// 画像保存がある場合、通知クリック時に保存先を開く処理を設定する。
		Runnable openAction = null;
		if (notificationRequest.openActionAvailable() && notificationRequest.targetPath() != null) {
			openAction = () -> {
				hideWithFadeOut();
				fileOpen.openDirectory(notificationRequest.targetPath());
			};
		}

		// 閉じる押下時の処理を設定する。
		Runnable closeAction = () -> {
			hideWithFadeOut();
		};

		return new NotificationAction(openAction, closeAction);
	}

	/** 通知を下から上へ移動しながらフェードイン表示します。 */
	private void showWithFadeIn(JWindow window, int x, int startY, int endY, boolean temporarySaved) {
		long startNanos = System.nanoTime();
		animationTimer = new Timer(ANIMATION_INTERVAL_MILLIS, event -> {
			if (window != currentWindow) {
				((Timer) event.getSource()).stop();
				return;
			}

			double progress = calculateProgress(startNanos);
			// ウィンドウを下から上へ移動しながら透明度を上げる。
			window.setLocation(x, interpolate(startY, endY, progress));
			setWindowOpacity(window, (float) progress);

			if (progress >= 1.0) {
				((Timer) event.getSource()).stop();
				animationTimer = null;
				int displayMillis = NOTIFICATION_DISPLAY_MILLIS;
				if (temporarySaved) {
					// 一時保存のときは文言が長いため、表示時間を長くする。
					displayMillis = NOTIFICATION_DISPLAY_LONG_MILLIS;
				}
				// 指定時間経過後に自動で閉じるタイマーを開始する。
				startDismissTimer(displayMillis);
			}
		});
		animationTimer.start();
	}

	/** 指定時間経過後に通知を閉じるタイマーを開始します。 */
	private void startDismissTimer(int delayMillis) {
		dismissTimer = new Timer(delayMillis, event -> {
			hideWithFadeOut();
		});
		dismissTimer.setRepeats(false);
		dismissTimer.start();
	}

	/** 現在の通知を下へ移動しながらフェードアウトします。 */
	private void hideWithFadeOut() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> {
				hideWithFadeOut();
			});
			return;
		}

		JWindow window = currentWindow;
		if (window == null) {
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

		animationTimer = new Timer(ANIMATION_INTERVAL_MILLIS, event -> {
			if (window != currentWindow) {
				((Timer) event.getSource()).stop();
				return;
			}

			double progress = calculateProgress(startNanos);
			// ウィンドウを上から下へ移動しながら透明度を下げる。
			window.setLocation(startX, interpolate(startY, endY, progress));
			setWindowOpacity(window, (float) (startOpacity * (1.0 - progress)));

			if (progress >= 1.0) {
				((Timer) event.getSource()).stop();
				animationTimer = null;
				disposeWindow(window);
			}
		});
		animationTimer.start();
	}

	/** アニメーション開始からの進捗を0.0～1.0で返します。 */
	private double calculateProgress(long startNanos) {
		double elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000.0;
		return Math.min(1.0, elapsedMillis / NOTIFICATION_ANIMATION_MILLIS);
	}

	/** 開始値と終了値の間を進捗率で補間します。 */
	private int interpolate(int start, int end, double progress) {
		return (int) Math.round(start + ((end - start) * progress));
	}

	/** タスクバーなどを除いた、通知を配置可能な画面領域を取得します。 */
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

	/** 対応環境の場合だけウィンドウ透明度を変更します。 */
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

	/** 透明度非対応環境では完全表示として扱います。 */
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

	/** 現在表示している通知と関連タイマーを即時破棄します。 */
	private void disposeCurrentNotification() {
		stopTimer(animationTimer);
		stopTimer(dismissTimer);
		animationTimer = null;
		dismissTimer = null;

		if (currentWindow != null) {
			currentWindow.setVisible(false);
			currentWindow.dispose();
			currentWindow = null;
		}
	}

	/** 指定ウィンドウが現在の通知なら破棄します。 */
	private void disposeWindow(JWindow window) {
		window.setVisible(false);
		window.dispose();
		if (window == currentWindow) {
			currentWindow = null;
		}
	}

	/** nullでないSwingタイマーを停止します。 */
	private void stopTimer(Timer timer) {
		if (timer != null) {
			timer.stop();
		}
	}

	/** 現在表示している通知を破棄し、関連タイマーを停止します。 */
	public void close() {
		if (SwingUtilities.isEventDispatchThread()) {
			disposeCurrentNotification();
		} else {
			SwingUtilities.invokeLater(() -> {
				disposeCurrentNotification();
			});
		}
	}

	/**
	 * 通知表示に必要な情報をまとめたレコードです。
	 *
	 * @param messageList 通知メッセージリスト
	 * @param openMessage 保存先フォルダを開く表示文言
	 * @param targetPath 保存先パス
	 * @param openActionAvailable 通知クリックで保存先を開ける場合true
	 * @param isTmpSaved 一時保存の場合true
	 */
	private record NotificationRequest(
			List<String> messageList,
			String openMessage,
			Path targetPath,
			boolean openActionAvailable,
			boolean isTmpSaved) {
	}

	/**
	 * 通知に必要なアクションをまとめて持つレコードです。
	 *
	 * @param openAction 保存先フォルダを開くアクション。画像保存でない場合はnull
	 * @param closeAction 通知を閉じるアクション
	 */
	private record NotificationAction(
			Runnable openAction,
			Runnable closeAction) {
	}
}
