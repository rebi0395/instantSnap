package com.github.rebi0395.instantsnap.view;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * マウスドラッグによる範囲選択の選択を行う透明ウィンドウを表示するクラス
 */
public class RangeSelectionWindow {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(RangeSelectionWindow.class);

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

	/**
	 * 範囲選択画面を表示する
	 * 
	 * @param listener 範囲選択結果の通知先
	 */
	public void show(RangeSelectionListener listener) {

		showWindow(listener);
	}

	/**
	 * 範囲選択画面を作成し、表示する
	 * @param listener
	 */
	private void showWindow(RangeSelectionListener listener) {
		// モニター情報を取得する
		resolveGraphicsConfiguration();

	}

	/**
	 * マウスポインターの属するモニター情報を取得する
	 * @return マウスポインターの属するモニター情報
	 */
	public GraphicsConfiguration resolveGraphicsConfiguration() {
		PointerInfo pointer = MouseInfo.getPointerInfo(); // マウスポインターの情報取得
		if (pointer == null || pointer.getDevice() == null) {
			// マウスポインターからモニターを特定出来なかった場合
			log.warn("マウスポインターの位置からモニターを特定できませんでした");
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			return ge.getDefaultScreenDevice().getDefaultConfiguration(); // メインモニターを返す
		}
		return pointer.getDevice().getDefaultConfiguration(); // マウスポインターの属するモニターを返す
	}
}
