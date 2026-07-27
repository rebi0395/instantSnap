package com.github.rebi0395.instantsnap.view.presenter;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.view.component.RangeSelectionPanel;

/**
 * Swingを使用して範囲選択画面の表示を制御するプレゼンタークラス
 */
public class RangeSelectionPresenter {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(RangeSelectionPresenter.class);

	/** ESCキーに割り当てる終了アクション名 */
	private static final String ACTION_CLOSE = "closeRangeSelection";

	/** 範囲決定通知リスナー */
	private final SelectionListener listener;

	/** 表示中の範囲選択ウィンドウ */
	private JWindow window;

	/** 範囲選択の描画とマウス操作を行うパネル */
	private RangeSelectionPanel selectionPanel;

	/**
	 * コンストラクタ
	 *
	 * @param listener 範囲選択結果の通知先
	 */
	public RangeSelectionPresenter(SelectionListener listener) {
		this.listener = listener;
	}

	/**
	 * 撮影選択完了時の通知インターフェース
	 */
	public interface SelectionListener {

		/**
		 * 範囲選択画面表示時に呼ばれるリスナー
		 */
		void onShown();

		/**
		 * 範囲選択解除後に呼ばれるリスナー
		 */
		void onClosed();

		/**
		 * 撮影範囲更新後に呼び出されるリスナー
		 *
		 * @param selectArea 更新後の撮影範囲
		 */
		void onUpdatedArea(Rectangle selectArea);
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
				closeRangeSelectionWindow();
				return;
			}

			showRangeSelectionWindow(screenDevice);
		});
	}

	/**
	 * アプリ終了時に範囲選択画面を破棄する。
	 * 呼び出し元の終了処理より先にウィンドウ破棄を完了させる。
	 */
	public void close() {
		if (EventQueue.isDispatchThread()) {
			closeRangeSelectionWindow();
			return;
		}

		try {
			// Swingのウィンドウ破棄が終わるまで呼び出し元スレッドを待機させる
			EventQueue.invokeAndWait(() -> {
				closeRangeSelectionWindow();
			});

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("範囲選択画面の終了待機中に割り込みが発生しました", e);

		} catch (InvocationTargetException e) {
			log.warn("範囲選択画面の終了処理に失敗しました", e.getCause());
		}
	}

	/**
	 * 撮影範囲選択を受け付ける画面を表示する。
	 *
	 * @param screenDevice 表示対象のモニター
	 */
	private void showRangeSelectionWindow(GraphicsDevice screenDevice) {
		JWindow createdWindow = null;

		try {
			// 表示対象モニターの設定と画面領域を取得する
			GraphicsConfiguration configuration = screenDevice.getDefaultConfiguration();
			Rectangle screenArea = configuration.getBounds();

			// モニターと同じAWT座標系を使用する範囲選択パネルを作成する
			RangeSelectionPanel createdPanel = new RangeSelectionPanel(screenArea, listener);

			// ウィンドウの設定
			createdWindow = new JWindow(configuration);
			createdWindow.setBackground(new Color(0, 0, 0, 0)); // ウィンドウ自体の背景を透明にする
			createdWindow.setAlwaysOnTop(true); // 操作中のゲーム画面より手前に表示する
			createdWindow.setFocusableWindowState(true); // ESCキーを受け取れるようにする
			createdWindow.setContentPane(createdPanel); // 範囲選択パネルを設定する
			createdWindow.setBounds(screenArea); // 対象モニターと同じ位置、大きさにする

			// ESCキーによる範囲選択解除処理を登録する
			registerEscapeAction(createdWindow.getRootPane());

			// close処理から現在のウィンドウとパネルを参照できるように保持する
			this.window = createdWindow;
			this.selectionPanel = createdPanel;

			createdWindow.setVisible(true); // 範囲選択画面を表示する
			createdWindow.toFront(); // ウィンドウを最前面へ移動する
			createdPanel.requestFocusInWindow(); // ESCキーを受け取れるようフォーカスを設定する

			listener.onShown(); // 画面表示を通知

		} catch (Exception e) {
			if (createdWindow != null) {
				createdWindow.dispose();
			}
			this.window = null;
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
		KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKey, ACTION_CLOSE);
		rootPane.getActionMap().put(ACTION_CLOSE, new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent event) {
				closeRangeSelectionWindow();
			}
		});
	}

	/**
	 * 撮影範囲選択を受け付ける画面を閉じる。
	 */
	private void closeRangeSelectionWindow() {
		if (!isShowing()) {
			return;
		}

		// 破棄前に選択状態を初期化する
		selectionPanel.resetSelection();

		// 破棄するウィンドウを退避した後、保持している参照を初期化する
		JWindow closingWindow = window;
		this.window = null;
		this.selectionPanel = null;

		closingWindow.setVisible(false); // 画面を非表示にする
		closingWindow.dispose(); // Swingのウィンドウリソースを破棄する
		listener.onClosed(); // リスナー側の設定値を破棄させる
	}

	/**
	 * 範囲選択画面を表示しているかを取得する。
	 *
	 * @return 画面を表示中ならtrue
	 */
	private boolean isShowing() {
		return window != null && window.isShowing();
	}
}
