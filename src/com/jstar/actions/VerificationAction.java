package com.jstar.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.jstar.dialogs.FileDialog;

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
		FileDialog dialog = new FileDialog(workbenchPart.getSite().getShell(), selectedFile);
		dialog.setBlockOnOpen(true);
        dialog.open();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
