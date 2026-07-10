package com.github.rebi0395.instantsnap.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rebi0395.instantsnap.model.enums.ThemeType;

import javafx.scene.Parent;
import javafx.scene.Scene;

import static com.github.rebi0395.instantsnap.common.AppConstant.THEME_CSS_PREFIX;

/**
 * CSSの適用処理を行うユーティリティクラス
 */
public final class StyleSheetUtil {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(StyleSheetUtil.class);

	/**
	 * インスタンス化を禁止する
	 */
	private StyleSheetUtil() {
	}

	/**
	 * CSSを適用する
	 *
	 * @param scene CSSを適用するシーン要素
	 * @param cssPath 適用するCSSファイルのパス
	 */
	public static void applyStyleSheet(Scene scene, String cssPath) {
		URL cssUrl = StyleSheetUtil.class.getResource(cssPath);

		if (cssUrl == null) {
			log.warn("CSSファイルが見つかりません: {}", cssPath);
			return;
		}

		// sceneが持つスタイルシート一覧にCSSのURLを追加し、UIにCSSを適用する
		scene.getStylesheets().add(cssUrl.toExternalForm());
	}

	/**
	 * CSSを適用する
	 *
	 * @param root CSSを適用するルート要素
	 * @param cssPath 適用するCSSファイルのパス
	 */
	public static void applyStyleSheet(Parent root, String cssPath) {
		URL cssUrl = StyleSheetUtil.class.getResource(cssPath);

		if (cssUrl == null) {
			log.warn("CSSファイルが見つかりません: {}", cssPath);
			return;
		}

		// rootが持つスタイルシート一覧にCSSのURLを追加し、UIにCSSを適用する
		root.getStylesheets().add(cssUrl.toExternalForm());
	}

	/**
	 * テーマCSSを切り替える。
	 * <p>
	 * テーマはCSSファイルとrootのstyleClassをセットで切り替えるため、
	 * 既存のテーマCSSとテーマ用styleClassを外してから、新しいテーマを適用する。
	 * </p>
	 *
	 * @param root テーマを適用するルート要素
	 * @param themeType 適用するテーマ
	 */
	public static void changeTheme(Parent root, ThemeType themeType) {
		// テーマCSSはファイル名を theme- から始める命名規則にしているため、
		// 読み込み済みCSSのうちテーマ用CSSだけを削除する。
		List<String> removeStyleSheets = new ArrayList<>();
		for (String styleSheetPath : root.getStylesheets()) {
			String fileName = styleSheetPath.substring(styleSheetPath.lastIndexOf('/') + 1);
			if (fileName.startsWith(THEME_CSS_PREFIX)) {
				removeStyleSheets.add(styleSheetPath);
			}
		}
		root.getStylesheets().removeAll(removeStyleSheets);

		// テーマ用styleClassも theme- から始める命名規則にしているため、
		// rootに付いているテーマ用styleClassだけを削除する。
		List<String> removeStyleClasses = new ArrayList<>();
		for (String styleClass : root.getStyleClass()) {
			if (styleClass.startsWith(THEME_CSS_PREFIX)) {
				removeStyleClasses.add(styleClass);
			}
		}
		root.getStyleClass().removeAll(removeStyleClasses);

		if (themeType == ThemeType.DEFAULT) {
			return;
		}

		applyTheme(root, themeType);
	}

	/**
	 * テーマCSSを適用する。
	 * CSSファイルを読み込むだけでなく、rootにテーマ用styleClassを付与する。
	 *
	 * @param root テーマを適用するルート要素
	 * @param themeType 適用するテーマ
	 */
	private static void applyTheme(Parent root, ThemeType themeType) {
		if (themeType == ThemeType.DEFAULT) {
			return;
		}

		applyStyleSheet(root, themeType.getCssFilePath());

		if (!root.getStyleClass().contains(themeType.getStyleClass())) {
			root.getStyleClass().add(themeType.getStyleClass());
		}
	}
}
