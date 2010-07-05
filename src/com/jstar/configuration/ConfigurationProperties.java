package com.jstar.configuration;

import java.io.File;
import java.util.ResourceBundle;

public class ConfigurationProperties {
	
	private static ResourceBundle configuration = ResourceBundle.getBundle("com.jstar.configuration.Configuration");
	public final static String JSTAR_LOGIC_LIBRARY = "JSTAR_LOGIC_LIBRARY";
	public final static String JSTAR_ABS_LIBRARY = "JSTAR_ABS_LIBRARY";
	public final static String JSTAR_SPECS_LIBRARY = "JSTAR_SPECS_LIBRARY";

	public static String getJStarExecutable() {
		return getJStarPath() + File.separator + "jStar";
	}
	
	public static String getJStarPath() {
		return configuration.getString("jStar.path");
	}
	
	public static String getCygwinPath() {
		return configuration.getString("cygwin.path");
	}
	
	public static String getJStarLogicLibrary() {
		return configuration.getString(JSTAR_LOGIC_LIBRARY);
	}
	
	public static String getJStarAbsLibrary() {
		return configuration.getString(JSTAR_ABS_LIBRARY);
	}
	
	public static String getJStarSpecLibrary() {
		return configuration.getString(JSTAR_SPECS_LIBRARY);
	}
	
	public static String getSootClassPathRt() {
		return configuration.getString("soot.class.path.rt");
	}

}
