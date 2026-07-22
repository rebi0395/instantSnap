package com.github.rebi0395.instantsnap.view.presenter;

import java.awt.Rectangle;

public class RangeSelectionPresenter {

	/**
	 * 範囲選択結果を受け取るリスナー。
	 */
	public interface RangeSelectionListener {

		/**
		 * 撮影範囲が選択されたときに呼び出される。
		 *
		 * @param selectedArea 選択された画面上の範囲
		 */
		void onSelected(Rectangle selectedArea);

		/**
		 * 範囲選択がキャンセルされたときに呼び出される。
		 */
		void onCancelled();
	}
	
	
}
