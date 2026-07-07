package com.github.rebi0395.instantsnap.app;

import java.awt.Toolkit;

import javax.swing.JOptionPane;

import com.github.rebi0395.instantsnap.common.DpiUtil;
import com.github.rebi0395.instantsnap.common.MessageKeyConstant;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.repository.PropertiesRepository;

/**
 * 起動クラス
 */
public class Main {

	/** 二重起動防止のためのインスタンス */
	private static SingleInstanceManager instanceManager;

	/**
	 * 起動メソッド
	 * @param args
	 */
	public static void main(String[] args) {

		instanceManager = new SingleInstanceManager();
		LanguageType language = PropertiesRepository.readLanguageOnly();
		MessageKeyConverter converter = new MessageKeyConverter(language);

		try {
			// 排他ロックを試みる
			boolean lockResult = instanceManager.tryLock();

			// ロックに成功した場合、通常起動
			if (lockResult) {
				DpiUtil.enablePerMonitorV2(); // DPIスケーリングの対応
				InstantSnapApp.launch(InstantSnapApp.class, args); // JavaFXの起動

				// ロックに失敗した場合、起動しない
			} else {
				// 音を出す
				Toolkit.getDefaultToolkit().beep();
				// swingを用いて、二重起動防止ダイアログを表示
				JOptionPane.showMessageDialog(
						null, // 依存関係はないため、null
						converter.getMessage(MessageKeyConstant.DIALOG_SINGLE_INSTANCE_MESSAGE), // メッセージ
						converter.getMessage(MessageKeyConstant.DIALOG_SINGLE_INSTANCE_TITLE), // タイトル
						JOptionPane.INFORMATION_MESSAGE); // メッセージタイプ
			}

			// ロック処理で例外が発生した場合
		} catch (Exception e) {
			// 音を出す
			Toolkit.getDefaultToolkit().beep();
			// swingを用いて、起動失敗ダイアログを表示
			JOptionPane.showMessageDialog(
					null,
					converter.getMessage(MessageKeyConstant.DIALOG_STARTUP_ERROR),
					converter.getMessage(MessageKeyConstant.DIALOG_SINGLE_INSTANCE_TITLE),
					JOptionPane.ERROR_MESSAGE);
		} finally {
			if (instanceManager != null) {
				instanceManager.close();
			}
		}
	}
}
