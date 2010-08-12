package com.jstar.eclipse.actions;

import java.io.File;
import java.util.List;

import com.jstar.eclipse.dialogs.InputFileDialog;
import com.jstar.eclipse.services.AnnotationProcessingService;
import com.jstar.eclipse.services.ConsoleService;
import com.jstar.eclipse.services.JStar;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class VerificationAction implements IObjectActionDelegate {
	
	private IWorkbenchPart workbenchPart; 
	private ISelection selection;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		workbenchPart = targetPart;
	}

	@Override
	public void run(IAction action) {
		try {
			JStar.getInstance().checkConfigurations();
		}
		catch (RuntimeException re) {
			re.printStackTrace();
			re.printStackTrace(ConsoleService.getInstance().getConsoleStream());
			return;
		}
		
		final IFile selectedFile = (IFile) ((IStructuredSelection) this.selection).getFirstElement();		
		
		final List<File> jimpleFiles = JStar.getInstance().convertToJimple(selectedFile);
				
		final InputFileDialog dialog = new InputFileDialog(workbenchPart.getSite().getShell(), selectedFile, jimpleFiles);
		
		dialog.setBlockOnOpen(true);
        final int returnValue = dialog.open();
        
        if (returnValue == IDialogConstants.OK_ID) {
        	
        	String spec;
        	
        	if (dialog.isSeparateSpec()) {
        		spec = dialog.getSpecFieldValue();
        	}
        	else {       		
        		spec = AnnotationProcessingService.getInstance().processAnnotations(selectedFile).getAbsolutePath();
        	}
        	
			try {
				Process pr = JStar.getInstance().executeJStar(
						selectedFile,
						spec, 
						dialog.getLogicFieldValue(),
						dialog.getAbsFieldValue(),
						dialog.getJimpleFile(),
						dialog.getPrintMode());
				ConsoleService.getInstance().printToConsole(selectedFile, pr);
			} 
			catch (Exception exc) {
				exc.printStackTrace();
				exc.printStackTrace(ConsoleService.getInstance().getConsoleStream());
			} 	
        }
	}
	


	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	

}
