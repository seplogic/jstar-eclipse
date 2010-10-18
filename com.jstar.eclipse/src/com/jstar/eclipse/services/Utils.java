/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
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

import com.jstar.eclipse.exceptions.InputFileNotFoundException;
import com.jstar.eclipse.objects.InputFileKind;
import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.objects.JavaFilePersistentProperties;
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
	
	public IFile createEmptyFile(final JavaFile selectedFile, final InputFileKind inputFile) {
		byte[] bytes = "".getBytes();
		final InputStream source = new ByteArrayInputStream(bytes);
		
		return createFile(selectedFile, inputFile, source);
	}
	
	// TODO: refactor
	public IFile createFile(final JavaFile selectedFile, final InputFileKind inputFile, final InputStream source) {		
		IFile file = null;
		
		try {
			selectedFile.getInputFile(inputFile);
		}
		catch (InputFileNotFoundException ifnfe) {
			file = ifnfe.getInputFile();
		}
		
		if (file == null) {
			// File already exists
			return selectedFile.getInputFile(inputFile);
		}
	
		IFolder folder = selectedFile.getJavaProject().getJStarRootFolder();
		IPath path = file.getProjectRelativePath().removeFirstSegments(folder.getProjectRelativePath().segmentCount());
		file = createFile(folder, path.removeLastSegments(1), path.removeFileExtension().lastSegment(), inputFile, source);
		
		return file;
	}
	
	public IFile createEmptyFile(final IFolder jStarRootFolder, IPath inputFilePath, final String inputFileName, final InputFileKind kind) {
		byte[] bytes = "".getBytes();
		final InputStream source = new ByteArrayInputStream(bytes);
		
		return createFile(jStarRootFolder, inputFilePath, inputFileName, kind, source);
	}
	
	
	public IFile createFile(final IFolder jStarRootFolder, IPath inputFilePath, final String inputFileName, final InputFileKind kind, final InputStream source) {
		try {
			final IFolder folder = createFolder(jStarRootFolder, inputFilePath);	
			IFile inputFile = folder.getFile(new Path(inputFileName).addFileExtension(kind.getExtension()));
			inputFile.refreshLocal(0, null);
			
			if (inputFile.exists()) {
				// File already exists
				return inputFile;
			}
			
			inputFile.create(source, IResource.NONE, null);
			
			return inputFile;
		} 
		catch (CoreException ce) {
			ce.printStackTrace(ConsoleService.getInstance().getConsoleStream());
			throw new RuntimeException(ce.getMessage());
		}
	}
	
	public IFolder createFolder(final IFolder jStarRootFolder, IPath inputFilePath) {
		try {
			IFolder folder = jStarRootFolder;
			
			while (!inputFilePath.isEmpty()) {
				String path = inputFilePath.segment(0);
				inputFilePath = inputFilePath.removeFirstSegments(1);
				
				folder = folder.getFolder(path);		
				folder.refreshLocal(0, null);
				
				if (!folder.exists()) {
					folder.create(IResource.NONE, true, null);
				}
			}
			
			return folder;
		}
		catch (CoreException ce) {
			ce.printStackTrace(ConsoleService.getInstance().getConsoleStream());
			throw new RuntimeException(ce.getMessage());
		}
	}

	public void makeImportsReady(JavaFile selectedFile) {
		final File imports = new File(selectedFile.getGeneratedImports().getLocation().toOSString());
		
		try {
			FileReader fileReader = new FileReader(imports);
			BufferedReader input = new BufferedReader(fileReader);
			
			String line = null;
			while ((line = input.readLine()) != null) {
				makeImportReady(selectedFile, line);
			}
		} 
		catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		} 
		catch (IOException ioe) {
			ioe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		}
	}

	private void makeImportReady(final JavaFile selectedFile, final String importLine) throws IOException {
		final IPath sourcePath = new Path(StringUtils.replace(importLine, ".", File.separator));
		
		try {
			final IJavaElement element = selectedFile.getJavaProject().getProject().findElement(sourcePath.addFileExtension("java"));
			final IResource resource = element.getResource();
			
			if (resource == null) {
				checkFiles(selectedFile, sourcePath);
				return;
			}
			
			if (resource != null && resource instanceof IFile) {
			
				final boolean specInSource = JavaFilePersistentProperties.isSpecInSourceFile(resource);
				
				if (specInSource) {
					IFile file = (IFile) resource;
					checkGeneratedFiles(file, selectedFile, sourcePath);
					makeImportsReady(new JavaFile(file));
				}
				else {
					checkFiles(selectedFile, sourcePath);
				}
				
				return;
			} 
			
			throw new RuntimeException("Could not found spec file for the " + importLine);
			
		} 
		catch (JavaModelException jme) {
			jme.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		}
		
	}
	
	private void checkGeneratedFiles(IFile file, final JavaFile selectedFile, final IPath sourcePath) throws IOException {
		checkGeneratedFile(file);
		checkFile(selectedFile, sourcePath, InputFileKind.LOGIC);
		checkFile(selectedFile, sourcePath, InputFileKind.ABS);
	}
	
	private void checkGeneratedFile(final IFile javaFile) {
		AnnotationProcessingService.getInstance().processAnnotations(new JavaFile(javaFile));
	}

	private void checkFiles(final JavaFile selectedFile, final IPath sourcePath) throws IOException {
		checkFile(selectedFile, sourcePath, InputFileKind.SPEC);
		checkFile(selectedFile, sourcePath, InputFileKind.LOGIC);
		checkFile(selectedFile, sourcePath, InputFileKind.ABS);
	}

	private void checkFile(final JavaFile selectedFile, final IPath sourcePath, final InputFileKind kind) throws IOException {
		final IFolder jStarRootFolder = selectedFile.getJavaProject().getJStarRootFolder();
		final IFile file = jStarRootFolder.getFile(sourcePath.addFileExtension(kind.getExtension()));
		final IPath fileCopy = jStarRootFolder.getLocation().append(JavaProject.GENERATED).append(sourcePath).addFileExtension(kind.getExtension());
		
		if (file.exists()) {
			FileUtils.copyFile(new File(file.getLocation().toOSString()), new File(fileCopy.toOSString()));
		}
		else {
			throw new RuntimeException("Could not find the file" + file.getName());
		}
	}

}
