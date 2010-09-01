package com.jstar.eclipse.services;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.jstar.eclipse.objects.JavaFile;

public class Utils {
	
	private static Utils instance;
	
	private Utils() {
	}

	public static Utils getInstance() {
		if (instance == null) {
			instance = new Utils();
		}
		return instance;
	}
	
	public JavaFile getFileFromActiveEditor(IWorkbenchWindow window) {
		if (window == null) {
			window = getActiveWindow();
		}
		
		final IEditorInput editorInput = window.getActivePage().getActiveEditor().getEditorInput();
		final IFile selectedFile = (IFile) editorInput.getAdapter(IFile.class);

		if (selectedFile == null) {
			ConsoleService.getInstance().printErrorMessage("Cannot access source file.");
			throw new NullPointerException();
		}
		
		return new JavaFile(selectedFile);
	}
	
	public IWorkbenchWindow getActiveWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow(); 
	}

}
