package com.github.rebi0395.instantsnap.controller;

import com.github.rebi0395.instantsnap.core.Option;
import com.github.rebi0395.instantsnap.logic.PropertiesEditor;
import com.github.rebi0395.instantsnap.logic.TriggerKeyManager;

public class MainController {
	
	private Option option;
	private PropertiesEditor editor;
	private TriggerKeyManager manager;
	
	public MainController(){
	}
	
	public void init() {
		this.option = new Option();
		this.editor = new PropertiesEditor(option);
		this.manager = new TriggerKeyManager(option); 
	}
}
