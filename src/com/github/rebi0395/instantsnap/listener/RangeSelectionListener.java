package com.github.rebi0395.instantsnap.listener;

import java.awt.Rectangle;

/**
 * 範囲選択画面の表示状態と選択結果を受け取るリスナーです。
 */
public interface RangeSelectionListener {

	/**
	 * 範囲選択画面が表示されたことを通知します。
	 */
	void onShown();

	/**
	 * 範囲選択画面が閉じられたことを通知します。
	 */
	void onClosed();

	/**
	 * 撮影範囲が更新されたことを通知します。
	 *
	 * @param selectedArea 更新後の撮影範囲
	 */
	void updateRangeArea(Rectangle selectedArea);
}
