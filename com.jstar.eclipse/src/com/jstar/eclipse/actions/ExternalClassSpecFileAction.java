package com.jstar.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.services.ResourceService;

public class ExternalClassSpecFileAction implements IObjectActionDelegate {
	private ISelection selection;

	public ExternalClassSpecFileAction() {
		super();
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	@Override
	public void run(IAction action) {
		final IFile selectedFile = (IFile) ((IStructuredSelection) this.selection).getFirstElement();
		final JavaFile javaFile = new JavaFile(selectedFile);
		ResourceService.getInstance().addExternalClassSpec(javaFile.getJavaProject());
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
