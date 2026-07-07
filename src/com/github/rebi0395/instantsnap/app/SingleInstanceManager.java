package com.github.rebi0395.instantsnap.app;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.github.rebi0395.instantsnap.exception.ServiceException;

/**
 * アプリケーションの二重起動を防止するクラス
 */
public class SingleInstanceManager implements AutoCloseable {

	/** ロックファイル */
	private static final Path LOCK_FILE = Path.of("./instantsnap.lock");

	/** ロックファイルのチャネル */
	private FileChannel channel;

	/** ロック */
	private FileLock lock;

	/**
	 * 二重起動防止用のロック取得を試みる
	 * 
	 * @return ロックを取得した場合true、すでに起動中であればfalse
	 * @throws ServiceException ロックファイルの準備に失敗した場合
	 */
	public boolean tryLock() throws ServiceException {
		try {
			// ロックファイルを書き込み可能な状態で開く。なければ作成する。
			channel = FileChannel.open(LOCK_FILE, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			// ロックの獲得を試みる
			lock = channel.tryLock();
			// ロックに失敗した場合は、チャネルを閉じてfalseを返す
			if (lock == null) {
				close();
				return false;
			}
			return true;

		} catch (OverlappingFileLockException e) {
			close();
			return false;

		} catch (IOException e) {
			close();
			throw new ServiceException("二重起動防止用ロックファイルの準備に失敗しました", e);
		}
	}

	/**
	 * ロックを開放する
	 */
	@Override
	public void close() {
		// ロックを開放する
		try {
			if (lock != null && lock.isValid()) {
				lock.release();
			}
		} catch (IOException e) {
			// 終了処理のため、何もしない
		} finally {
			lock = null;
		}
		// チャネルを閉じる
		try {
			if (channel != null && channel.isOpen()) {
				channel.close();
			}
		} catch (IOException e) {
			// 終了処理のため、何もしない
		} finally {
			channel = null;
		}
	}
}
