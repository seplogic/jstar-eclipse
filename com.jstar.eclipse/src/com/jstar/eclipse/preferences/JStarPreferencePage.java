package com.jstar.eclipse.preferences;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import com.jstar.eclipse.Activator;

public class JStarPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public JStarPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	

	public void createFieldEditors() {
		addField(new FileFieldEditor(PreferenceConstants.JSTAR_PATH, "&jStar executable:", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.JSTAR_LOGIC_LIBRARY_PREFERENCE, "&jStar Logic Library:", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.JSTAR_ABS_LIBRARY_PREFERENCE, "&jStar Abs Library:", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(PreferenceConstants.JSTAR_SPECS_LIBRARY_PREFERENCE, "&jStar Specs Library:", getFieldEditorParent()));
		
		if (SystemUtils.IS_OS_MAC) {
			addField(new FileFieldEditor(PreferenceConstants.SOOT_CLASSPATH_CLASSES, "&classes.jar:", getFieldEditorParent()));
			addField(new FileFieldEditor(PreferenceConstants.SOOT_CLASSPATH_UI, "&ui.jar:", getFieldEditorParent()));
		}
		else {
			addField(new FileFieldEditor(PreferenceConstants.SOOT_CLASSPATH_RT, "&rt.jar:", getFieldEditorParent()));
		}
		
		addField(new BooleanFieldEditor(PreferenceConstants.VERIFY_AFTER_SAVING, "Verify after saving the file", getFieldEditorParent()));
	}


	public void init(IWorkbench workbench) {
	}	
	
	public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getProperty().equals(FieldEditor.VALUE)) {
        	Object source = event.getSource();
        	if (source instanceof BooleanFieldEditor) {
        		if (((BooleanFieldEditor) source).getPreferenceName().equals(PreferenceConstants.VERIFY_AFTER_SAVING)) {
        			if (event.getNewValue().equals(Boolean.TRUE)) {
        				Activator.getDefault().addSaveListener();
        			}
        			else {
        				Activator.getDefault().removeSaveListener();
        			}
        		}
        	}
        	source.toString();
        }        
	}
	
}