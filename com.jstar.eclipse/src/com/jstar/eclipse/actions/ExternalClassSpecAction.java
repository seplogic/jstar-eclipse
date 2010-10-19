package com.jstar.eclipse.actions;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.jstar.eclipse.Activator;
import com.jstar.eclipse.objects.InputFileKind;
import com.jstar.eclipse.objects.JavaProject;
import com.jstar.eclipse.services.ConsoleService;
import com.jstar.eclipse.services.Utils;

public class ExternalClassSpecAction implements IObjectActionDelegate {
	
	private IWorkbenchPart workbenchPart;
	private ISelection selection;

	public ExternalClassSpecAction() {
		super();
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		workbenchPart = targetPart;
	}

	@Override
	public void run(IAction action) {
		final IJavaProject selectedProject = (IJavaProject) ((IStructuredSelection) this.selection).getFirstElement();
		final InputDialog dlg = new InputDialog(workbenchPart.getSite().getShell(), "External class specification", "Enter class name", "", null);
		
        if (dlg.open() == Window.OK) {
        	final String className = dlg.getValue();
        	
        	try {
        		final IPath sourcePath = new Path(StringUtils.replace(className, ".", File.separator));
				final IJavaElement element = selectedProject.findElement(sourcePath.addFileExtension("java"));
				
		        if (element == null ) {
		        	Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Could not find class " + className + " in the project.", null);
		        	ErrorDialog.openError(workbenchPart.getSite().getShell(), "jStar", "Cannot create specification file for a class " + className, status);
		        	
		        	return;
		        }
		        
		        final JavaProject project = new JavaProject(selectedProject);
		        
		        //TODO: check if exists
		        final IFolder jStarRootFolder = project.getJStarRootFolder();
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

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
	private void createInputFile(final IFolder jStarRootFolder, IPath inputFilePath, final String inputFileName, final InputFileKind kind) {
        IFile inputFile = jStarRootFolder.getFile(inputFilePath.append(inputFileName).addFileExtension(kind.getExtension()));
        
        if (!inputFile.exists()) {
        	inputFile = Utils.getInstance().createEmptyFile(jStarRootFolder, inputFilePath, inputFileName, kind);
        }
        
        Utils.getInstance().openFileInEditor(inputFile, true);
	}

}
