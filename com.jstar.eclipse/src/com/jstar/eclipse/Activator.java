package com.jstar.eclipse;

import java.io.File;

import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.jstar.eclipse.listeners.SaveListener;
import com.jstar.eclipse.preferences.PreferenceConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.jstar.eclipse";
	public static final String PROCESSOR_PATH = "lib" + File.separator + "jstar_processing.jar";
	public static final String COMMONS_IO_1_4_PATH = "lib" + File.separator + "commons-io-1.4" + File.separator + "commons-io-1.4.jar";

	private static Activator plugin;
	private IExecutionListener saveListener;
	
	/**   
	 * The constructor
	 */
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		if (PreferenceConstants.verifyAfterSaving()) {
			addSaveListener();
		}
	}

	public void stop(BundleContext context) throws Exception {
		removeSaveListener();
		
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}
	
	private ICommandService getCommandService() {
		return (ICommandService) getDefault().getWorkbench().getService(ICommandService.class);
	}
	
	public void addSaveListener() {
		if (saveListener == null) {
			saveListener = new SaveListener();
			getCommandService().addExecutionListener(saveListener);	
		}
	}
	
	public void removeSaveListener() {
		if (saveListener != null) {
			getCommandService().removeExecutionListener(saveListener);
			saveListener = null;
		}
	}

}
