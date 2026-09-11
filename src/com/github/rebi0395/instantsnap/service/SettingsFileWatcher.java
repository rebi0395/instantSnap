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

import com.github.rebi0395.instantsnap.listener.SettingsChangeListener;

/**
 * 設定ファイルの作成・更新をOSの通知で待ち、変更を通知するクラスです。
 * 常駐プロセスとJavaFX設定プロセスで、それぞれ監視インスタンスを所有します。
 * <p>
 * ファイルそのものではなく親ディレクトリを監視するため、一時ファイルからの
 * 置き換え保存も検知します。削除だけでは通知せず、再作成された時点で通知します。
 * 監視先ディレクトリが無効になった場合は終了し、自動再登録は行いません。
 * </p>
 */
public final class SettingsFileWatcher implements AutoCloseable {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SettingsFileWatcher.class);

	/** OSのファイル更新通知を受け取る監視サービス */
	private final WatchService watchService;

	/** 監視対象のファイル名 */
	private final Path watchedFileName;

	/** 設定ファイル更新時の通知先 */
	private final SettingsChangeListener changeListener;

	/** ファイル監視を行うデーモンスレッド */
	private final Thread watchThread;

	/** 監視を継続している場合true */
	private final AtomicBoolean running = new AtomicBoolean(true);

	/**
	 * 指定された設定ファイルの監視を開始します。
	 * 生成直後から通知するため、通知先は生成前に初期化を済ませておきます。
	 *
	 * @param settingsFile 監視対象の設定ファイル
	 * @param changeListener 設定ファイルの変更通知を受け取るクラス
	 * @throws IOException 監視サービスを開始できない場合
	 * @throws IllegalArgumentException 設定ファイルまたは通知先がnullの場合
	 */
	public SettingsFileWatcher(Path settingsFile, SettingsChangeListener changeListener) throws IOException {
		if (settingsFile == null) {
			throw new IllegalArgumentException("監視対象の設定ファイルはnullにできません");
		}
		if (changeListener == null) {
			throw new IllegalArgumentException("設定更新の通知先はnullにできません");
		}

		Path absoluteSettingsFile = settingsFile.toAbsolutePath().normalize();
		Path watchDirectory = absoluteSettingsFile.getParent();
		if (watchDirectory == null) {
			throw new IOException("設定ファイルの親ディレクトリを解決できません: " + settingsFile);
		}

		this.watchedFileName = absoluteSettingsFile.getFileName();
		this.changeListener = changeListener;
		this.watchService = FileSystems.getDefault().newWatchService();
		// 置き換え保存ではファイル自体が変わるため、親ディレクトリを監視する。
		watchDirectory.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);

		this.watchThread = new Thread(() -> watchLoop(), "instantsnap-settings-file-watcher");
		this.watchThread.setDaemon(true);
		this.watchThread.start();
	}

	/**
	 * OSの変更通知を待ち、対象ファイルの変更がある場合だけ通知します。
	 * takeで待機するため、一定間隔でファイルを読み直す処理は行いません。
	 */
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

			// 今回取り出した通知をまとめ、次回のイベントを受け取れる状態に戻す。
			boolean watchKeyValid = watchKey.reset();
			if (settingsChanged && running.get()) {
				notifySettingsChanged();
			}
			if (!watchKeyValid) {
				log.warn("設定ファイルの監視先が無効になったため、監視を終了します");
				return;
			}
		}
	}

	/**
	 * 更新通知先の例外で監視スレッドが終了しないように呼び出します。
	 */
	private void notifySettingsChanged() {
		try {
			changeListener.onSettingsFileChanged();
		} catch (RuntimeException e) {
			log.error("設定ファイル更新後の処理に失敗しました", e);
		}
	}

	/**
	 * ファイル監視を終了し、待機中の監視スレッドを起こします。
	 * 複数回呼ばれても終了処理は一度だけ実行します。
	 * 既に通知先へ引き継いだ処理の完了は待ちません。
	 */
	@Override
	public void close() {
		if (!running.compareAndSet(true, false)) {
			return;
		}

		try {
			// WatchServiceを閉じると、takeの待機が解除されて監視ループを抜ける。
			watchService.close();
		} catch (IOException e) {
			log.warn("設定ファイル監視の終了に失敗しました", e);
		}
		watchThread.interrupt();
	}
}
