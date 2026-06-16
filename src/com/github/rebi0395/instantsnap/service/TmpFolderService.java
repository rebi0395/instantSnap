package com.github.rebi0395.instantsnap.service;

import static com.github.rebi0395.instantsnap.common.AppConstant.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 一時保存フォルダ内のファイルを削除するクラス
 */
public class TmpFolderService {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(TmpFolderService.class);

	/**
	 * tmpディレクトリを削除します。
	 * tmpディレクトリが存在しない場合は何もしません。
	 */
	public void cleanTmpDirectory() {
		Path tmpDir = Path.of(TMP_SAVE_DIR);

		if (Files.notExists(tmpDir)) {
			return;
		}

		try (Stream<Path> paths = Files.walk(tmpDir)) {
			// 親フォルダより子ファイルを先にリストに格納する
			List<Path> pathList = paths.sorted(Comparator.reverseOrder()).toList();

			for (Path path : pathList) {
				delete(path);
			}
			log.info("一時保存先ディレクトリを削除しました。 path={}", tmpDir);

		} catch (IOException e) {
			log.warn("一時保存先ディレクトリの削除に失敗しました。 path={}", tmpDir, e);
		}
	}

	/**
	 * ファイルまたはディレクトリを削除します。
	 * 削除に失敗してもアプリは継続します。
	 */
	private void delete(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			log.warn("一時ファイルの削除に失敗しました。 path={}", path, e);
		}
	}
}