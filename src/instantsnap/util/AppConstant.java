package instantsnap.util;

/**
 * 定数管理クラス
 */
public final class AppConstant {
	
	// インスタンス化しない
	private AppConstant(){}
	
	// プロパティファイルのパス
	public static final String PROPERTIES_PASS = "config.properties";
	// プロパティの項目名 
	public static final String SAVE_PATH = "savePath";
	public static final String FORMAT = "format";
	public static final String TRIGGER = "trigger";
	public static final String OPENMODE = "openMode";
	// デフォルト値
	public static final String DEFAULT_SAVE_PATH = "./screenshots/";
	// 拡張子
	public static final String PNG = "png";
	public static final String JPG = "jpg";
	// 撮影トリガーキー
	public static final String SHIFT_PRINTSCREEN = "1";
	public static final String PRINTSCREEN = "2";
	// 撮影後、自動で画像/フォルダを開く
	public static final String DISOPEN = "0";
	public static final String AUTO_OPEN_IMAGE = "1";
	public static final String AUTO_OPEN_FOLDER = "2";
}
