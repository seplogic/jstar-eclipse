/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.services;

import java.io.File;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.FileEditorInput;

import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.objects.JavaProject;

public class Utils {
	
	private static Utils instance;
	private static final String DEFAULT_TEXT_EDITOR = "org.eclipse.ui.DefaultTextEditor";
	
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

	public IFolder specifyJStarRootFolder(JavaProject javaProject) {
		final ViewerFilter directoryFilter = new ViewerFilter() {
	        public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
	                return ((IResource)element).getType() == IResource.FOLDER;
	        }
	    };
		
		final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getActiveWindow().getShell(), new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
		dialog.setTitle("jStar folder selection");
		dialog.setMessage("Select the folder where specifications and rules will be stored:");
		dialog.setInput(javaProject.getProject().getProject());
		dialog.setAllowMultiple(false);
		dialog.addFilter(directoryFilter);
		int returnValue = dialog.open();
		
		if (returnValue == Window.CANCEL) {
			return null;
		}
		
		return (IFolder)dialog.getFirstResult();
	}
	
	public void openFileInEditor(final IFile selectedFile) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();	
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(selectedFile.getName());
		final String descId = desc == null ? DEFAULT_TEXT_EDITOR : desc.getId();
		
		try {
			page.openEditor(new FileEditorInput(selectedFile), descId);
		} 
		catch (PartInitException pie) {
			pie.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		}
	}

}
