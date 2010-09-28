/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.preferences;

import java.io.File;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.jface.preference.IPreferenceStore;

import com.jstar.eclipse.Activator;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	public static final String JSTAR_LOGIC_LIBRARY_PREFERENCE = "logicLibraryPreference";

	public static final String JSTAR_ABS_LIBRARY_PREFERENCE = "absLibraryPreference";

	public static final String JSTAR_SPECS_LIBRARY_PREFERENCE = "specsLibraryPreference";

	public static final String JSTAR_PATH = "jStarPathPreference";
	
	public static final String SOOT_CLASSPATH_RT = "sootClasspathRtPreference";
	
	public static final String SOOT_CLASSPATH_CLASSES = "sootClasspathClassesPreference";
	
	public static final String SOOT_CLASSPATH_UI = "sootClasspathUiPreference";
	
	public final static String VERIFY_AFTER_SAVING = "verifyAfterSavingPreference";
	
	public final static String JSTAR_LOGIC_LIBRARY = "JSTAR_LOGIC_LIBRARY";
	
	public final static String JSTAR_ABS_LIBRARY = "JSTAR_ABS_LIBRARY";
	
	public final static String JSTAR_SPECS_LIBRARY = "JSTAR_SPECS_LIBRARY";
	
	public static String getJStarExecutable() {
		return getStore().getString(PreferenceConstants.JSTAR_PATH);
	}
	
	public static String getJStarLogicLibrary() {
		return getStore().getString(PreferenceConstants.JSTAR_LOGIC_LIBRARY_PREFERENCE);
	}
	
	public static String getJStarAbsLibrary() {
		return getStore().getString(PreferenceConstants.JSTAR_ABS_LIBRARY_PREFERENCE);
	}
	
	public static String getJStarSpecLibrary() {
		return getStore().getString(PreferenceConstants.JSTAR_SPECS_LIBRARY_PREFERENCE);
	}
	
	public static String getSootClassPathRt() {
		return getStore().getString(PreferenceConstants.SOOT_CLASSPATH_RT);
	}
	
	public static String getSootClassPathClasses() {
		return getStore().getString(PreferenceConstants.SOOT_CLASSPATH_CLASSES);
	}
	
	public static String getSootClassPathUi() {
		return getStore().getString(PreferenceConstants.SOOT_CLASSPATH_UI);
	}
	
	public static String getSootClassPath() {
		if (SystemUtils.IS_OS_MAC) {
			return getSootClassPathClasses() + File.pathSeparator + getSootClassPathUi();
		}
		else {
			return getSootClassPathRt();
		}
	}
	
	public static boolean verifyAfterSaving() {
		return getStore().getBoolean(PreferenceConstants.VERIFY_AFTER_SAVING);
	}
	
	private static IPreferenceStore getStore() {
		return Activator.getDefault().getPreferenceStore();
	}
}
