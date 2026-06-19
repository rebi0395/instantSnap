package com.github.rebi0395.instantsnap.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoundPlayer implements AutoCloseable {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(SoundPlayer.class);

	/** シャッター音のパス */
	private static final String SHUTTER_SOUND_PATH = "/sounds/camera_shutter.wav";

	/** 通知音のパス */
	private static final String NOTIFICATION_SOUND_PATH = "/sounds/notification.wav";

	/** シャッター音のClip */
	private Clip shutterClip;

	/** 通知音のClip */
	private Clip notificationClip;

	/** 音声Clip一覧 */
	private List<Clip> clipList = new ArrayList<>();

	/** コンストラクタ 音声をClip形式でロードする */
	public SoundPlayer() {
		shutterClip = loadClip(SHUTTER_SOUND_PATH);
		notificationClip = loadClip(NOTIFICATION_SOUND_PATH);
	}

	/**
	 * カメラのシャッター音を再生する
	 */
	public void playShutterSound() {
		play(shutterClip);
	}
	
	/**
	 * カメラのシャッター音を再生する
	 */
	public void playNotificationSound() {
		play(notificationClip);
	}

	/**
	 * 音声リソースを開放する
	 */
	@Override
	public void close() {
		for (Clip clip : clipList) {
			if (clip != null && clip.isOpen()) {
				clip.close();
			}
		}
		clipList.clear();
		log.info("音声リソースの開放に成功しました");
	}

	/**
	 * 指定された音声ファイルを再生する
	 * @param clip 再生する音声ファイルのclip
	 */
	private synchronized void play(Clip clip) {
		if (clip == null || !clip.isOpen()) {
			// clipがなければなにもしない
			return;
		}

		try {
			if (clip.isRunning()) {
				// もしclipが再生中なら停止する
				clip.stop();
			}
			clip.setFramePosition(0); // 開始位置に戻す
			clip.start(); // 再生

		} catch (Exception e) {
			log.warn("音声の再生に失敗しました", e);
		}
	}

	/**
	 * 音声ファイルをClip形式で事前ロードしておくメソッド
	 * @param path ファイルのパス
	 * @return clipオブジェクト
	 */
	private Clip loadClip(String path) {
		URL url = SoundPlayer.class.getResource(path); // 音声ファイルの場所情報をパスから取得
		if (url == null) {
			log.warn("音声ファイルが見つかりません：{}", path);
			return null;
		}
		try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(url)) {
			Clip clip = AudioSystem.getClip(); // クリップ取得
			clip.open(audioInputStream); // audioInputStreamを紐付け、音声操作を可能にする
			addList(clip); // クリップリストに加える
			return clip;

		} catch (Exception e) {
			log.warn("音声ファイルの読み込みに失敗しました:{}", path, e);
			return null;
		}
	}

	/**
	 * clipをclip一覧に加える
	 * @param clip
	 */
	private void addList(Clip clip) {
		if (clip != null) {
			clipList.add(clip);
		}
	}
}
