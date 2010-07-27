package com.jstar.eclipse.actions;

import com.jstar.eclipse.dialogs.InputFileDialog;
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
		final IFile selectedFile = (IFile) ((IStructuredSelection) this.selection).getFirstElement();		
		final InputFileDialog dialog = new InputFileDialog(workbenchPart.getSite().getShell(), selectedFile);
		dialog.setBlockOnOpen(true);
        final int returnValue = dialog.open();
        
        if (returnValue == IDialogConstants.OK_ID) {
			try {
				Process pr = JStar.getInstance().executeJStar(
						selectedFile,
						dialog.getSpecFieldValue(), 
						dialog.getLogicFieldValue(),
						dialog.getAbsFieldValue(),
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
