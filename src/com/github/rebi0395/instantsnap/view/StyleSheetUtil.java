package com.github.rebi0395.instantsnap.view;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.Parent;
import javafx.scene.Scene;

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

		// rootが持つスタイルシート一覧にCSSのURLを追加し、UIにCSSを適用する
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
}