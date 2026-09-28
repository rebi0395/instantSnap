package com.github.rebi0395.instantsnap.view.swing.dialog;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.listener.RangeSelectionListener;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.service.ScreenCaptureExclusionService;
import com.github.rebi0395.instantsnap.view.swing.panel.RangeSelectionPanel;

/**
 * Swingを使用して範囲選択画面の表示を制御するダイアログクラス
 */
public class RangeSelectionDialog {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(RangeSelectionDialog.class);

	/** ユーザー設定値 */
	private ConfigBean config;

	/** 言語変換コンバーター */
	private MessageKeyConverter converter;

	/** 範囲決定通知リスナー */
	private final RangeSelectionListener listener;

	/** 範囲選択画面を撮影対象から除外するサービス */
	private final ScreenCaptureExclusionService screenCaptureExclusionService;

	/** 表示中の範囲選択ダイアログ */
	private JDialog dialog;

	/** 範囲選択の描画とマウス操作を行うパネル */
	private RangeSelectionPanel selectionPanel;

	/** 範囲選択の現在の表示モニター */
	private GraphicsDevice screenDevice;

	/** モード中に再利用する、モニターごとの範囲選択画面 */
	private final Map<GraphicsDevice, SelectionWindow> selectionWindows = new HashMap<>();

	/** 範囲選択モニターの切り替え監視タイマー */
	private Timer screenMonitorTimer;

	/** マウスの通常移動量 */
	private static final int NORMAL_MOVE = 1;

	/** マウスの高速移動量 */
	private static final int FAST_MOVE = 20;

	/** マウスポインターの所属モニターのチェックの監視間隔(ms) */
	private static final int MONITOR_TIMER = 50;

	/**
	 * コンストラクタ
	 * @param config ユーザー設定値
	 * @param converter メッセージ変換クラス
	 * @param listener 範囲選択結果の通知先
	 */
	public RangeSelectionDialog(ConfigBean config, MessageKeyConverter converter, RangeSelectionListener listener) {
		this.config = config;
		this.converter = converter;
		this.listener = listener;
		this.screenCaptureExclusionService = new ScreenCaptureExclusionService();
	}

	/**
	 * 範囲選択モードの有効・無効を切り替える。
	 * 画面が表示中なら閉じ、表示されていなければ開く。
	 *
	 * @param screenDevice 範囲選択画面を表示するモニター
	 */
	public void switchRangeSelectionMode(GraphicsDevice screenDevice) {
		// Swingコンポーネントの操作をイベントディスパッチスレッドへ依頼する
		EventQueue.invokeLater(() -> {
			if (isShowing()) {
				closeRangeSelectionDialog();
				return;
			}

			showRangeSelectionDialog(screenDevice);
		});
	}

	/**
	 * アプリ終了時に範囲選択画面を破棄する。
	 * 呼び出し元の終了処理より先にダイアログ破棄を完了させる。
	 */
	public void close() {
		if (EventQueue.isDispatchThread()) {
			closeRangeSelectionDialog();
			return;
		}

		try {
			// Swingのダイアログ破棄が終わるまで呼び出し元スレッドを待機させる
			EventQueue.invokeAndWait(() -> {
				closeRangeSelectionDialog();
			});

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("範囲選択画面の終了待機中に割り込みが発生しました", e);

		} catch (InvocationTargetException e) {
			log.warn("範囲選択画面の終了処理に失敗しました", e.getCause());
		}
	}

	/**
	 * 撮影後も範囲撮影モードを継続するため、次の範囲選択待ちに戻します。
	 * 撮影開始前に選択枠の描画を確実に消すため、Swingスレッドの処理完了を待機します。
	 */
	public void prepareNextSelection() {
		Runnable prepareSelection = new Runnable() {
			@Override
			public void run() {
				if (!isShowing() || selectionPanel == null) {
					return;
				}
				selectionPanel.prepareNextSelection();
				selectionPanel.repaint();
			}
		};

		if (EventQueue.isDispatchThread()) {
			prepareSelection.run();
			return;
		}

		try {
			EventQueue.invokeAndWait(prepareSelection);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("次の範囲選択待ちへの更新中に割り込みが発生しました", e);

		} catch (InvocationTargetException e) {
			log.warn("次の範囲選択待ちへの更新に失敗しました", e.getCause());
		}
	}

	/**
	 * 撮影範囲選択を受け付ける画面を表示する。
	 *
	 * @param screenDevice 表示対象のモニター
	 */
	private void showRangeSelectionDialog(GraphicsDevice screenDevice) {
		try {
			// 画面作成はモニター切り替え時と共通化し、モード開始の通知はここだけで行う。
			SelectionWindow createdWindow = showSelectionWindow(screenDevice);
			this.dialog = createdWindow.dialog();
			this.selectionPanel = createdWindow.panel();
			this.screenDevice = screenDevice;

			listener.onShown();
			startScreenMonitor();

		} catch (Exception e) {
			stopScreenMonitor();
			disposeSelectionWindows();
			this.dialog = null;
			this.selectionPanel = null;
			this.screenDevice = null;
			log.error("範囲選択画面の表示に失敗しました", e);
		}
	}

	/**
	 * 指定モニター用の範囲選択画面を作成する。表示は呼び出し元で行う。
	 * モードの開始・終了通知と監視タイマーの操作は呼び出し元で行う。
	 *
	 * @param targetDevice 表示対象のモニター
	 * @return 作成したダイアログと範囲選択パネル
	 */
	private SelectionWindow createSelectionWindow(GraphicsDevice targetDevice) {
		JDialog createdDialog = null;

		try {
			// モニターごとの位置・解像度・表示倍率に対応する描画設定を使用する。
			GraphicsConfiguration configuration = targetDevice.getDefaultConfiguration();
			Rectangle screenArea = configuration.getBounds();
			RangeSelectionPanel createdPanel = new RangeSelectionPanel(screenArea, config, converter, listener);

			createdDialog = new JDialog(
					(Dialog) null, // ownerなし
					null, // タイトルなし
					false, // モーダルにしない
					configuration);

			createdDialog.setUndecorated(true); // ダイアログ枠を消す
			createdDialog.setBackground(new Color(0, 0, 0, 0)); // 背景を透明にする
			createdDialog.setAlwaysOnTop(true); // 範囲選択画面を最前面に維持する
			createdDialog.setContentPane(createdPanel);

			// 表示前に操作を登録し、初回表示でも切り替え後でも同じキー操作を受け付ける。
			JRootPane root = createdDialog.getRootPane();
			registerEscapeAction(root);
			registerEnterKeyAction(root);

			// 方向キーの登録
			registerArrowKeyAction(root, KeyEvent.VK_LEFT, false, new Point(-NORMAL_MOVE, 0));
			registerArrowKeyAction(root, KeyEvent.VK_RIGHT, false, new Point(NORMAL_MOVE, 0));
			registerArrowKeyAction(root, KeyEvent.VK_UP, false, new Point(0, -NORMAL_MOVE));
			registerArrowKeyAction(root, KeyEvent.VK_DOWN, false, new Point(0, NORMAL_MOVE));
			// 方向キー + シフトキー
			registerArrowKeyAction(root, KeyEvent.VK_LEFT, true, new Point(-FAST_MOVE, 0));
			registerArrowKeyAction(root, KeyEvent.VK_RIGHT, true, new Point(FAST_MOVE, 0));
			registerArrowKeyAction(root, KeyEvent.VK_UP, true, new Point(0, -FAST_MOVE));
			registerArrowKeyAction(root, KeyEvent.VK_DOWN, true, new Point(0, FAST_MOVE));

			// ネイティブウィンドウを生成し、画面に表示する前に撮影除外を設定する。
			createdDialog.pack();
			createdDialog.setBounds(screenArea);
			screenCaptureExclusionService.setExcluded(createdDialog, true);

			return new SelectionWindow(createdDialog, createdPanel);

		} catch (RuntimeException e) {
			// 作成途中の画面だけを破棄し、切り替え元の画面は呼び出し元で維持する。
			if (createdDialog != null) {
				createdDialog.dispose();
			}
			throw e;
		}
	}

	/**
	 * モニター用の画面を取得して表示する。
	 * モード中は同じ画面を再利用し、全画面の描画領域を切り替えるたびに確保しない。
	 *
	 * @param targetDevice 表示対象のモニター
	 * @return 表示したダイアログとパネル
	 */
	private SelectionWindow showSelectionWindow(GraphicsDevice targetDevice) {
		SelectionWindow window = selectionWindows.get(targetDevice);
		if (window == null) {
			window = createSelectionWindow(targetDevice);
		}

		try {
			// 再利用するパネルは前回の座標と選択状態を引き継がず、待機状態から表示する。
			window.panel().prepareNextSelection();
			window.dialog().setVisible(true);
			window.dialog().toFront();
			window.dialog().requestFocus();
			window.panel().initializeCurrentPoint();
			window.panel().repaint();

			// 表示に成功した画面だけを保持する。作成数は使用したモニター数に収まる。
			selectionWindows.put(targetDevice, window);
			return window;

		} catch (RuntimeException e) {
			selectionWindows.remove(targetDevice);
			window.dialog().dispose();
			throw e;
		}
	}

	/**
	 * モード中に保持していた全モニターの画面を破棄する。
	 * 非表示の画面も解放し、モード終了後に描画領域やパネルを保持しない。
	 */
	private void disposeSelectionWindows() {
		for (SelectionWindow window : selectionWindows.values()) {
			window.dialog().dispose();
		}
		selectionWindows.clear();
	}

	/**
	 * 待機中の範囲選択画面を別モニターへ切り替える。
	 * Swingのイベントディスパッチスレッド上で呼び出す。
	 *
	 * @param nextDevice 切り替え先のモニター
	 */
	private void changeSelectionScreen(GraphicsDevice nextDevice) {
		if (!isShowing() || selectionPanel == null || !selectionPanel.isWaiting()
				|| nextDevice == null || nextDevice.equals(screenDevice)) {
			return;
		}

		SelectionWindow nextWindow;
		try {
			// 新しい画面の表示が成功するまで、現在の画面と参照を保持する。
			nextWindow = showSelectionWindow(nextDevice);
		} catch (RuntimeException e) {
			log.warn("範囲選択画面のモニター切り替えに失敗しました", e);
			return;
		}

		JDialog previousDialog = dialog;
		this.dialog = nextWindow.dialog();
		this.selectionPanel = nextWindow.panel();
		this.screenDevice = nextDevice;

		// モードと監視タイマーを継続したまま、元の画面は次回の切り替えに備えて非表示にする。
		// onClosed/onShownやresetSelectionは呼ばず、通知先の撮影範囲を維持する。
		previousDialog.setVisible(false);
	}

	/**
	 * ESCキーで範囲選択画面を閉じるアクションを登録する。
	 *
	 * @param rootPane キー操作を登録するルートパネル
	 */
	private void registerEscapeAction(JRootPane rootPane) {
		KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); // ESCキー
		AbstractAction closeAction = new AbstractAction() { // アクション作成

			@Override
			public void actionPerformed(ActionEvent event) {
				closeRangeSelectionDialog();
			}
		};
		// ESCキーとアクション名を紐付ける
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKey, "closeRangeSelection");
		// アクション名と実際の処理を紐付ける
		rootPane.getActionMap().put("closeRangeSelection", closeAction);
	}

	/**
	 * エンターキーで選択範囲状態の操作を進めるアクションを登録する。
	 *
	 * @param rootPane キー操作を登録するルートパネル
	 */
	private void registerEnterKeyAction(JRootPane rootPane) {
		KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		AbstractAction advanceStateWithAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PointerInfo point = MouseInfo.getPointerInfo();
				if (point == null) {
					return;
				}
				selectionPanel.advanceStateWithAction();
			}
		};
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enterKey, "advanceStateWithAction");
		rootPane.getActionMap().put("advanceStateWithAction", advanceStateWithAction);
	}

	/**
	 * 矢印キーでマウス座標を変更するアクションを登録する。
	 *
	 * @param rootPane キー操作を登録するルートパネル
	 * @param keyCode 矢印キーのキーコード
	 * @param useShiftModifier shift修飾キーを使用するか
	 * @param movement 移動量
	 */
	private void registerArrowKeyAction(JRootPane rootPane, int KeyCode, boolean useShiftModifier, Point movement) {
		int shiftModifiers = 0; // 未定義
		if (useShiftModifier) {
			shiftModifiers = InputEvent.SHIFT_DOWN_MASK;
		}

		// -- マウス操作アクションの登録
		KeyStroke mouseKeyStroke = KeyStroke.getKeyStroke(KeyCode, shiftModifiers);
		String mouseActionMapKey = "mouse" + KeyCode + shiftModifiers; // アクションマップのキー
		AbstractAction arrowKeysMouseAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectionPanel.moveMouse(movement);
			}
		};
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(mouseKeyStroke, mouseActionMapKey);
		rootPane.getActionMap().put(mouseActionMapKey, arrowKeysMouseAction);

		// -- 選択範囲枠操作アクションの登録
		KeyStroke areaKeyStroke = KeyStroke.getKeyStroke(KeyCode, InputEvent.CTRL_DOWN_MASK | shiftModifiers);
		String areaActionMapKey = "area" + KeyCode + shiftModifiers; // アクションマップのキー
		AbstractAction arrowKeysAreaAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectionPanel.moveSelectionArea(movement);
			}
		};
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(areaKeyStroke, areaActionMapKey);
		rootPane.getActionMap().put(areaActionMapKey, arrowKeysAreaAction);
	}

	/**
	 * 撮影範囲選択を受け付ける画面を閉じる。
	 */
	private void closeRangeSelectionDialog() {
		// 画面の有無にかかわらず停止し、閉じた後に監視処理を残さない。
		stopScreenMonitor();
		boolean wasShowing = isShowing();

		if (selectionPanel != null) {
			selectionPanel.resetSelection();
		}

		// 表示中の画面に加え、別モニターで待機している非表示の画面も解放する。
		this.dialog = null;
		this.selectionPanel = null;
		this.screenDevice = null;
		disposeSelectionWindows();

		if (wasShowing) {
			listener.onClosed();
		}
	}

	/**
	 * 範囲選択画面を表示しているかを取得する。
	 *
	 * @return 画面を表示中ならtrue
	 */
	private boolean isShowing() {
		return dialog != null && dialog.isShowing();
	}

	/**
	 * マウスポインター所属モニターの監視を開始する
	 */
	private void startScreenMonitor() {
		// 再開時もタイマーを一つに保ち、開閉による監視の重複を防ぐ。
		stopScreenMonitor();
		screenMonitorTimer = new Timer(MONITOR_TIMER, event -> {
			checkScreenChanged();
		});
		screenMonitorTimer.start();
	}

	/**
	 * マウスポインター所属モニターの監視を停止する
	 */
	private void stopScreenMonitor() {
		if (screenMonitorTimer == null) {
			return;
		}

		screenMonitorTimer.stop();
		screenMonitorTimer = null;
	}

	/**
	 * 範囲選択モニターの対象が切り替わったかを確認する
	 */
	private void checkScreenChanged() {
		if (!isShowing() || selectionPanel == null || screenDevice == null || !selectionPanel.isWaiting()) {
			return;
		}
		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		if (pointerInfo == null) {
			return;
		}
		GraphicsDevice nowScreenDevice = pointerInfo.getDevice();
		if (nowScreenDevice == null || nowScreenDevice.equals(screenDevice)) {
			return;
		}

		// TimerはSwingスレッドで動作するため、そのまま画面を差し替えられる。
		changeSelectionScreen(nowScreenDevice);
	}

	/**
	 * 一つのモニター用に作成したダイアログとパネルをまとめて保持する。
	 */
	private record SelectionWindow(JDialog dialog, RangeSelectionPanel panel) {
	}

}
