/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.services.Utils;
import com.jstar.eclipse.services.VerificationService;

public class ContextMenuVerificationConfigAction implements IObjectActionDelegate {
	
	private IWorkbenchPart workbenchPart; 
	private ISelection selection;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		workbenchPart = targetPart;
	}

	@Override
	public void run(IAction action) {	
		final IFile selectedFile = (IFile) ((IStructuredSelection) this.selection).getFirstElement();
		Utils.getInstance().openFileInEditor(selectedFile, false);
		VerificationService.getInstance().verifyConfig(new JavaFile(selectedFile), workbenchPart.getSite().getShell());	
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
	
}
