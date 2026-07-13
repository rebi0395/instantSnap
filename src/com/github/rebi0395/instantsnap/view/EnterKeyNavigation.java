package com.github.rebi0395.instantsnap.view;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * 画面操作をエンターキーで行うためのクラス
 */
public class EnterKeyNavigation {

	/** インスタンス化しない */
	private EnterKeyNavigation() {
	}

	/**
	 * エンターキー押下のイベントを一括で登録する。
	 * @param root
	 */
	public static void enableEnterAction(Parent root) {

		root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			// エンター以外早期リターン
			if (event.getCode() != KeyCode.ENTER) {
				return;
			}
			Node nowFocus = root.getScene().getFocusOwner(); // フォーカスを取得

			// パターンマッチングswitch文で、instanceOfを内部的に行う
			switch (nowFocus) {
			case CheckBox checkBox -> checkCheckBox(event, checkBox);
			case ComboBox<?> comboBox -> openComboBox(event, comboBox);
			case TitledPane titledPane -> openAccordion(event, titledPane);
			case TabPane tab -> moveNextTab(event, tab);
			case RadioButton radio -> moveNextRadio(event, radio);
			default -> {
				// どの型にも該当しないとき、何もしない
			}
			}
		});
	}

	/**
	 * チェックボックスのチェックをON/OFFする
	 * @param event キーイベント
	 * @param checkbox 操作対象のチェックボックス
	 */
	private static void checkCheckBox(KeyEvent event, CheckBox checkbox) {
		checkbox.fire(); // イベントを発火
		event.consume();
	}

	/**
	 * コンボボックスの選択肢を表示する
	 * @param event キーイベント
	 * @param comboBox 操作対象のComboBox
	 */
	private static void openComboBox(KeyEvent event, ComboBox<?> comboBox) {
		// すでに表示している場合は、リターン
		if (comboBox.isShowing()) {
			return;
		}
		comboBox.show();
		event.consume();
	}

	/**
	 * アコーディオンを開閉する
	 * @param event キーイベント
	 * @param titledPane 操作対象のアコーディオン
	 */
	private static void openAccordion(KeyEvent event, TitledPane titledPane) {
		if (titledPane.isExpanded()) {
			// 開いている場合、閉じる
			titledPane.setExpanded(false);
		} else {
			// 開いていない場合、開ける
			titledPane.setExpanded(true);
		}
		event.consume();
	}

	/**
	 * 次のタブを選択する。シフト押下で前のタブを選択する
	 * @param event キーイベント
	 * @param tabPane 操作対象のタブ
	 */
	private static void moveNextTab(KeyEvent event, TabPane tabPane) {
		int tabCount = tabPane.getTabs().size();
		// タブが1以下の場合、リターン
		if (tabCount <= 1) {
			return;
		}

		int currentIndex = tabPane.getSelectionModel().getSelectedIndex();
		// 要素番号を+1することで、次を選択する
		int direction = 1;
		if (event.isShiftDown()) {
			// シフト押下で逆方向(-1)にする
			direction = -1;
		}
		// 最後のタブの次は、タブ数で割った余りが0になるため、先頭のタブに戻る
		// 計算結果が負数になっても正しいインデックスへ変換し、先頭と末尾を循環させる
		int nextIndex = Math.floorMod((currentIndex + direction), tabCount);
		tabPane.getSelectionModel().select(nextIndex);
		event.consume();
	}

	/**
	 * 次のラジオボタンを選択する。シフト押下で前を選択する
	 * @param event キーイベント
	 * @param radio 操作対象のラジオボタン
	 */
	private static void moveNextRadio(KeyEvent event, RadioButton radio) {
		ToggleGroup group = radio.getToggleGroup();
		// トグルグループ化されていない場合、早期リターン
		if (group == null) {
			return;
		}
		ObservableList<Toggle> toggles = group.getToggles();
		int index = toggles.indexOf(radio);

		// indexが不正値、選択肢が1以下の場合は早期リターン
		if (index < 0 || toggles.size() <= 1) {
			return;
		}

		// 方向を設定する
		int direction = 1;
		if (event.isShiftDown()) {
			// シフト押下で逆方向(-1)にする
			direction = -1;
		}

		for (int offset = 1; offset < toggles.size(); offset++) {
			// 計算結果が負数になっても正しいインデックスへ変換し、先頭と末尾を循環させる
			int nextIndex = Math.floorMod(index + (offset * direction), toggles.size());
			Toggle nextToggle = toggles.get(nextIndex);

			RadioButton nextRadioButton = (RadioButton) nextToggle;

			// 非活性、非表示の場合はスキップ
			if (nextRadioButton.isDisabled() || !nextRadioButton.isVisible()) {
				continue;
			}

			nextRadioButton.fire(); // マウスクリックと同じくActionEventも発火させる
			nextRadioButton.requestFocus(); // フォーカス移動
			event.consume(); // 標準イベント消費
			return; // 処理を終了
		}
	}
}
