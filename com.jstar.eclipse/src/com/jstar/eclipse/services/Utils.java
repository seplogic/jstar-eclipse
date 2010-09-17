package com.jstar.eclipse.services;

import java.io.File;

import org.apache.commons.lang.SystemUtils;
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
	
	public File getJRELibLocation() {
		final StringBuilder location = new StringBuilder(System.getProperty("java.home"));
		location.append(File.separator);
		
		if (SystemUtils.IS_OS_MAC) {
			location.append("..").append(File.separator).append("Classes").append(File.separator);
		}
		else {
			location.append("lib").append(File.separator);
		}
		
		File locationFile = new File(location.toString());
		
		if (locationFile.isDirectory()) {
			return locationFile;
		}
		
		return null;		
	}
	
	//Windows and Linux
	public File getRtJar() {
		return getJar("rt.jar");
	}
	
	// Mac
	public File getClassesJar() {
		return getJar("classes.jar");
	}
	
	// Mac
	public File getUIJar() {
		return getJar("ui.jar");
	}
	
	private File getJar(String fileName) {
		File libLocation = getJRELibLocation();
				
		if (libLocation == null) {
			return null;
		}
		
		File jar = new File(libLocation.getAbsolutePath() + File.separator + fileName);
		
		if (jar.exists()) {
			return jar;
		}
		
		return null;
	}

}
