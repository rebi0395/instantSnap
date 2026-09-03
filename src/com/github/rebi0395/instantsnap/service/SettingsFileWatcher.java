package com.github.rebi0395.instantsnap.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 設定ファイルの更新を監視し、常駐側へ変更を通知するクラスです。
 * JavaFX設定画面と常駐プロセスを直接依存させず、手動編集も同じ経路で検知します。
 */
public final class SettingsFileWatcher implements AutoCloseable {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SettingsFileWatcher.class);

	/** OSのファイル更新通知を受け取る監視サービス */
	private final WatchService watchService;

	/** 監視対象のファイル名 */
	private final Path watchedFileName;

	/** 設定ファイル更新時の通知先 */
	private final Runnable updateHandler;

	/** ファイル監視を行うデーモンスレッド */
	private final Thread watchThread;

	/** 監視を継続している場合true */
	private final AtomicBoolean running = new AtomicBoolean(true);

	/**
	 * 指定された設定ファイルの監視を開始します。
	 *
	 * @param settingsFile 監視対象の設定ファイル
	 * @param updateHandler 設定ファイル更新時に実行する処理
	 * @throws IOException 監視サービスを開始できない場合
	 */
	public SettingsFileWatcher(Path settingsFile, Runnable updateHandler) throws IOException {
		if (settingsFile == null) {
			throw new IllegalArgumentException("監視対象の設定ファイルはnullにできません");
		}
		if (updateHandler == null) {
			throw new IllegalArgumentException("設定更新時の処理はnullにできません");
		}

		Path absoluteSettingsFile = settingsFile.toAbsolutePath().normalize();
		Path watchDirectory = absoluteSettingsFile.getParent();
		if (watchDirectory == null) {
			throw new IOException("設定ファイルの親ディレクトリを解決できません: " + settingsFile);
		}

		this.watchedFileName = absoluteSettingsFile.getFileName();
		this.updateHandler = updateHandler;
		this.watchService = FileSystems.getDefault().newWatchService();
		watchDirectory.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);

		this.watchThread = new Thread(() -> watchLoop(), "instantsnap-settings-file-watcher");
		this.watchThread.setDaemon(true);
		this.watchThread.start();
	}

	/** OSから通知されたファイル更新を待ち、対象ファイルの場合だけ通知します。 */
	private void watchLoop() {
		while (running.get()) {
			WatchKey watchKey;
			try {
				watchKey = watchService.take();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			} catch (ClosedWatchServiceException e) {
				return;
			}

			boolean settingsChanged = false;
			for (WatchEvent<?> event : watchKey.pollEvents()) {
				if (event.kind() == OVERFLOW) {
					// 通知が欠落した可能性があるため、安全側として設定を再読込する。
					settingsChanged = true;
					continue;
				}

				Object context = event.context();
				if (context instanceof Path changedPath && watchedFileName.equals(changedPath.getFileName())) {
					settingsChanged = true;
				}
			}

			boolean watchKeyValid = watchKey.reset();
			if (settingsChanged && running.get()) {
				notifyUpdate();
			}
			if (!watchKeyValid) {
				log.warn("設定ファイルの監視先が無効になったため、監視を終了します");
				return;
			}
		}
	}

	/** 更新通知先の例外で監視スレッドが終了しないように呼び出します。 */
	private void notifyUpdate() {
		try {
			updateHandler.run();
		} catch (RuntimeException e) {
			log.error("設定ファイル更新後の処理に失敗しました", e);
		}
	}

	/** ファイル監視とデーモンスレッドを終了します。 */
	@Override
	public void close() {
		if (!running.compareAndSet(true, false)) {
			return;
		}

		try {
			watchService.close();
		} catch (IOException e) {
			log.warn("設定ファイル監視の終了に失敗しました", e);
		}
		watchThread.interrupt();
	}
}
