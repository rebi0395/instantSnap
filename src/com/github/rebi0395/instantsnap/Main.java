package com.github.rebi0395.instantsnap;

import com.github.rebi0395.instantsnap.core.Option;
import com.github.rebi0395.instantsnap.logic.KeyManager;
import com.github.rebi0395.instantsnap.logic.PropertiesEditor;

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
