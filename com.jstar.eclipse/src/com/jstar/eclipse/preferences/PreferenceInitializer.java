/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.preferences;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.jstar.eclipse.Activator;
import com.jstar.eclipse.services.Utils;

/**
 * Class used to initialise default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore preferences = Activator.getDefault().getPreferenceStore();
		if (SystemUtils.IS_OS_MAC) {
			preferences.setDefault(PreferenceConstants.SOOT_CLASSPATH_CLASSES, Utils.getInstance().getClassesJar().getAbsolutePath());
			preferences.setDefault(PreferenceConstants.SOOT_CLASSPATH_UI, Utils.getInstance().getUIJar().getAbsolutePath());
		}
		else {
			preferences.setDefault(PreferenceConstants.SOOT_CLASSPATH_RT, Utils.getInstance().getRtJar().getAbsolutePath());
		}
	}

}
