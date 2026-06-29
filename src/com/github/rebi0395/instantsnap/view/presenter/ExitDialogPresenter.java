package com.github.rebi0395.instantsnap.view.presenter;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.awt.Toolkit;
import java.io.IOException;

import com.github.rebi0395.instantsnap.common.MessageKeyConstant;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.exception.ServiceException;
import com.github.rebi0395.instantsnap.view.controller.ExitDialogController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * アプリ終了ダイアログの表示制御を行うクラス
 */
public class ExitDialogPresenter {

	/** メッセージキーをメッセージに変換するクラス */
	private MessageKeyConverter converter;

	/** コンストラクタ */
	public ExitDialogPresenter(MessageKeyConverter converter) {
		this.converter = converter;
	}
	
	/**
	 * 終了確認ダイアログを表示する
	 *
	 * @param owner 親ウィンドウ
	 * @throws ServiceException 
	 */
	public boolean show(Stage owner) throws ServiceException {
		try {
			ExitDialogController exitController = new ExitDialogController(converter);

			// FXMLローダーをロード
			FXMLLoader exitLoader = new FXMLLoader(getClass().getResource(FXML_EXIT_DIALOG));
			exitLoader.setController(exitController);

			Parent exitRoot = exitLoader.load();

			// ダイアログの設定
			Stage dialog = new Stage();
			dialog.initOwner(owner);
			dialog.initModality(Modality.APPLICATION_MODAL); // モーダルとして設定
			dialog.setResizable(false); // サイズ変更できないように設定
			dialog.initStyle(StageStyle.UTILITY); // バックグラウンドの操作を無効化
			dialog.setScene(new Scene(exitRoot));
			dialog.setTitle(converter.getMessage(MessageKeyConstant.DIALOG_EXIT_TITLE));

			Toolkit.getDefaultToolkit().beep();

			// 応答を待つ
			dialog.showAndWait();

			return exitController.getResult();

		} catch (IOException e) {
			throw new ServiceException("終了確認ダイアログの表示に失敗しました" , e);
		}
	}
}
