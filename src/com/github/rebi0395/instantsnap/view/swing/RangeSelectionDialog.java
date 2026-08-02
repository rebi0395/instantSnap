package com.github.rebi0395.instantsnap.view.swing;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
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
import com.github.rebi0395.instantsnap.common.OsUtil;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.BOOL;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

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

	/** 画面キャプチャからダイアログを除外する設定値 */
	private static final int WDA_EXCLUDEFROMCAPTURE = 0x00000011;

	/** 範囲決定通知リスナー */
	private final SelectionListener listener;

	/** 表示中の範囲選択ダイアログ */
	private JDialog dialog;

	/** 範囲選択の描画とマウス操作を行うパネル */
	private RangeSelectionPanel selectionPanel;

	/**
	 * コンストラクタ
	 * @param listener 範囲選択結果の通知先
	 */
	public RangeSelectionDialog(ConfigBean config, MessageKeyConverter converter, SelectionListener listener) {
		this.config = config;
		this.converter = converter;
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
			registerEscapeAction(createdDialog.getRootPane()); // ESCキー
			registerEnterAction(createdDialog.getRootPane()); // エンターキー

			// close処理から現在のダイアログとパネルを参照できるように保持する
			this.dialog = createdDialog;
			this.selectionPanel = createdPanel;

			if (OsUtil.isWindows()) {
				// user32を用いて、範囲選択ダイアログをキャプチャ対象から除外する
				excludeFromScreenCapture(createdDialog);
			}

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
	private void registerEnterAction(JRootPane rootPane) {
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
	 * 指定したダイアログをキャプチャ対象から除外する。
	 *
	 * @param targetDialog 除外対象のダイアログ
	 */
	private void excludeFromScreenCapture(JDialog targetDialog) {
		try {
			// SwingダイアログからWindowsのウィンドウハンドルを取得する
			Pointer windowPointer = Native.getComponentPointer(targetDialog);
			HWND windowHandle = new HWND(windowPointer);

			// 範囲選択ダイアログをキャプチャ対象から除外する
			BOOL result = User32Ex.INSTANCE.SetWindowDisplayAffinity(
					windowHandle,
					WDA_EXCLUDEFROMCAPTURE);

			if (!result.booleanValue()) {
				// 設定に失敗しても範囲選択自体は継続する
				log.warn(
						"範囲選択画面を撮影対象から除外できませんでした。errorCode={}",
						Native.getLastError());
			}
		} catch (RuntimeException | LinkageError e) {
			log.warn("範囲選択画面を撮影対象から除外できませんでした。", e);
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
	 * JNAを介して呼び出すWindowsのUser32 API
	 */
	private interface User32Ex extends StdCallLibrary {

		/** User32 APIを呼び出すためのインスタンス */
		User32Ex INSTANCE = Native.load(
				"user32",
				User32Ex.class,
				W32APIOptions.DEFAULT_OPTIONS);

		/**
		 * 指定したウィンドウの画面キャプチャに関する表示方法を設定する。
		 *
		 * @param window 設定対象のウィンドウハンドル
		 * @param affinity 画面キャプチャ時の表示方法
		 * @return 設定に成功した場合はTRUE、失敗した場合はFALSE
		 */
		BOOL SetWindowDisplayAffinity(HWND window, int affinity);
	}
}
