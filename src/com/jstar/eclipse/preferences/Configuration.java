package com.jstar.eclipse.preferences;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import com.jstar.eclipse.Activator;

public class Configuration
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public Configuration() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	

	public void createFieldEditors() {
		addField(new DirectoryFieldEditor(PreferenceConstants.JSTAR_PATH, "&jStar executable:", getFieldEditorParent()));
		addField(new FileFieldEditor(PreferenceConstants.SPEC_ANNOTATIONS_PREFERENCE, "Annotation library location:", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.JSTAR_LOGIC_LIBRARY_PREFERENCE, "&jStar Logic Library:", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.JSTAR_ABS_LIBRARY_PREFERENCE, "&jStar Abs Library:", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.JSTAR_SPECS_LIBRARY_PREFERENCE, "&jStar Specs Library:", getFieldEditorParent()));
		
		if (SystemUtils.IS_OS_WINDOWS) {
			addField(new DirectoryFieldEditor(PreferenceConstants.CYGWIN_PATH, "&cygwin path:", getFieldEditorParent()));
		}
		
		addField(new FileFieldEditor(PreferenceConstants.SOOT_CLASSPATH_RT, "&rt.jar:", getFieldEditorParent()));
	}


	public void init(IWorkbench workbench) {
	}
	
}