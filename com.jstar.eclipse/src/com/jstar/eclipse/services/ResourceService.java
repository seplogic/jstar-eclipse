package com.jstar.eclipse.services;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import com.jstar.eclipse.Activator;
import com.jstar.eclipse.exceptions.NoJStarRootFolderException;
import com.jstar.eclipse.objects.InputFileKind;
import com.jstar.eclipse.objects.JavaProject;

public class ResourceService {
	
	private static ResourceService instance;
	
	private ResourceService() {
	}

	public static ResourceService getInstance() {
		if (instance == null) {
			instance = new ResourceService();
		}
		return instance;
	}
	
	public void addExternalClassSpec(final JavaProject project) {		
		IFolder jStarRootFolder;
		
		try {
			 jStarRootFolder = project.getJStarRootFolder();
		}
		catch (NoJStarRootFolderException njsrfe) {
			jStarRootFolder = Utils.getInstance().specifyJStarRootFolder(project);
			
			if (jStarRootFolder != null) {
				project.setJStarRootFolder(jStarRootFolder);
			}
			else {	
				return;
			}
		}
		
		final IInputValidator validator = new IInputValidator() {
	        public String isValid(String newText) {
	        	if (StringUtils.isEmpty(newText)) {
	        		return "Enter class name, e.g. java.lang.Object";
	        	}
	          	else return null;
	        }
	      };
		
		final InputDialog dialog = new InputDialog(Utils.getInstance().getActiveWindow().getShell(), 
				"External class specification",
				"An empty spec, logic and abs file will be created for your class if they do not already exist in this project.",
				"java.lang.Object", 
				validator
		);
		
        if (dialog.open() == Window.OK) {
        	final String className = dialog.getValue();
        	
        	try {
        		final IPath sourcePath = new Path(StringUtils.replace(className, ".", File.separator));
				final IJavaElement element = project.getProject().findElement(sourcePath.addFileExtension("java"));
				
		        if (element == null ) {
		        	Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Could not find class " + className + " in the project.", null);
		        	ErrorDialog.openError(Utils.getInstance().getActiveWindow().getShell(), "jStar", "Cannot create specification file for a class " + className, status);
		        	
		        	return;
		        }
		        
		        final IPath inputFilePath = sourcePath.removeLastSegments(1);
		        final String inputFileName = sourcePath.lastSegment();
		        
		        createInputFile(jStarRootFolder, inputFilePath, inputFileName, InputFileKind.SPEC);
		        createInputFile(jStarRootFolder, inputFilePath, inputFileName, InputFileKind.LOGIC);
		        createInputFile(jStarRootFolder, inputFilePath, inputFileName, InputFileKind.ABS);
		    } 
        	catch (JavaModelException jme) {
				jme.printStackTrace(ConsoleService.getInstance().getConsoleStream());
			}
        }
	}

	private void createInputFile(final IFolder jStarRootFolder, IPath inputFilePath, final String inputFileName, final InputFileKind kind) {
        IFile inputFile = jStarRootFolder.getFile(inputFilePath.append(inputFileName).addFileExtension(kind.getExtension()));
        
        if (!inputFile.exists()) {
        	inputFile = Utils.getInstance().createEmptyFile(jStarRootFolder, inputFilePath, inputFileName, kind);
        }
        
        Utils.getInstance().openFileInEditor(inputFile, true);
	}
}
