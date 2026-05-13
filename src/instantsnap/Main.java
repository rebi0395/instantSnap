package instantsnap;

import instantsnap.core.Option;
import instantsnap.logic.KeyManager;
import instantsnap.logic.PropertiesEditor;

/**
 * 起動クラス
 */
public class Main {
	
	/**
	 * 起動メソッド
	 * @param args
	 */
	public static void main(String[] args) {

		Option option = new Option();
		PropertiesEditor editor = new PropertiesEditor(option);
		KeyManager keyManager = new KeyManager(option); 
		
    }
}
