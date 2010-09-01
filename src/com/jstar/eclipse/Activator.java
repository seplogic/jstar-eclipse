package com.jstar.eclipse;

import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.jstar.eclipse.listeners.SaveListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.jstar.eclipse";

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
		
		saveListener = new SaveListener();
		getCommandService().addExecutionListener(saveListener);	
	}

	public void stop(BundleContext context) throws Exception {
		getCommandService().removeExecutionListener(saveListener);
		
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}
	
	private ICommandService getCommandService() {
		return (ICommandService) getDefault().getWorkbench().getService(ICommandService.class);
	}

}
