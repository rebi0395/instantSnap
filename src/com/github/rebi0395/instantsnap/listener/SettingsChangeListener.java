package com.github.rebi0395.instantsnap.listener;

/**
 * 設定ファイルの変更通知を受け取るリスナーです。
 */
public interface SettingsChangeListener {

	/**
	 * 設定ファイルの変更を監視スレッド上で通知します。
	 */
	void onSettingsFileChanged();
}
