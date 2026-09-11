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

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

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

	/** マウスの通常移動量 */
	private static final int NORMAL_MOVE = 1;

	/** マウスの高速移動量 */
	private static final int FAST_MOVE = 20;

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
		if (EventQueue.isDispatchThread()) {
			prepareNextSelectionOnEventThread();
			return;
		}

		try {
			EventQueue.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					prepareNextSelectionOnEventThread();
				}
			});

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("次の範囲選択待ちへの更新中に割り込みが発生しました", e);

		} catch (InvocationTargetException e) {
			log.warn("次の範囲選択待ちへの更新に失敗しました", e.getCause());
		}
	}

	/**
	 * Swingスレッド上で範囲選択パネルを次の選択待ちに戻します。
	 */
	private void prepareNextSelectionOnEventThread() {
		if (!isShowing() || selectionPanel == null) {
			return;
		}
		selectionPanel.prepareNextSelection();
		selectionPanel.repaint();
	}

	/**
	 * 撮影範囲選択を受け付ける画面を表示する。
	 *
	 * @param screenDevice 表示対象のモニター
	 */
	private void showRangeSelectionDialog(GraphicsDevice screenDevice) {
		JDialog createdDialog = null;

		try {
			// 表示対象モニターの設定と画面領域を取得する
			GraphicsConfiguration configuration = screenDevice.getDefaultConfiguration();
			Rectangle screenArea = configuration.getBounds();

			// モニターと同じAWT座標系を使用する範囲選択パネルを作成する
			RangeSelectionPanel createdPanel = new RangeSelectionPanel(screenArea, config, converter, listener);

			// ダイアログの生成
			createdDialog = new JDialog(
					(Dialog) null, // ownerなし
					null, // タイトルなし
					false, // モーダルにしない
					configuration); // モニターの描画範囲

			// ダイアログの設定
			createdDialog.setUndecorated(true); // ダイアログ枠を消す
			createdDialog.setBackground(new Color(0, 0, 0, 0)); // ダイアログ自体の背景を透明にする
			createdDialog.setAlwaysOnTop(true); // ダイアログを最前面に表示し続ける設定
			createdDialog.setContentPane(createdPanel); // 範囲選択パネルをダイアログに設定する
			createdDialog.setBounds(screenArea); // ダイアログの描画範囲を対象モニターと同じ位置、大きさにする
			createdDialog.setVisible(true); // 範囲選択画面を表示する
			createdDialog.toFront(); // ダイアログを最前面へ移動する
			createdDialog.requestFocus(); // フォーカスを要求する

			// 範囲選択パネルのマウス位置を初期化する
			createdPanel.initializeCurrentPoint();

			// キーボードによる範囲選択処理を登録する
			JRootPane root = createdDialog.getRootPane();
			registerEscapeAction(root); // ESCキー
			registerEnterKeyAction(root); // エンターキー

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

			// close処理から現在のダイアログとパネルを参照できるように保持する
			this.dialog = createdDialog;
			this.selectionPanel = createdPanel;

			// 範囲選択画面はユーザー設定にかかわらず、常に撮影対象から除外する。
			screenCaptureExclusionService.setExcluded(createdDialog, true);

			listener.onShown(); // 画面表示を通知

		} catch (Exception e) {
			if (createdDialog != null) {
				createdDialog.dispose();
			}
			this.dialog = null;
			this.selectionPanel = null;
			log.error("範囲選択画面の表示に失敗しました", e);
		}
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
		if (!isShowing()) {
			return;
		}

		// 破棄前に選択状態を初期化する
		selectionPanel.resetSelection();

		// 破棄するダイアログを退避した後、保持している参照を初期化する
		JDialog closingDialog = dialog;
		this.dialog = null;
		this.selectionPanel = null;

		closingDialog.setVisible(false); // 画面を非表示にする
		closingDialog.dispose(); // Swingのダイアログリソースを破棄する
		listener.onClosed(); // リスナー側の設定値を破棄させる
	}

	/**
	 * 範囲選択画面を表示しているかを取得する。
	 *
	 * @return 画面を表示中ならtrue
	 */
	private boolean isShowing() {
		return dialog != null && dialog.isShowing();
	}

}
