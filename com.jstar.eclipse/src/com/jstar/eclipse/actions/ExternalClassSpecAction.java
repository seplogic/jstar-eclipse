package com.jstar.eclipse.actions;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.jstar.eclipse.objects.JavaProject;
import com.jstar.eclipse.services.ResourceService;

public class ExternalClassSpecAction implements IObjectActionDelegate {
	private ISelection selection;

	public ExternalClassSpecAction() {
		super();
	}

	@Override
	public void run(IAction action) {
		final IJavaProject selectedProject = (IJavaProject) ((IStructuredSelection) this.selection).getFirstElement();
		final JavaProject project = new JavaProject(selectedProject);
		ResourceService.getInstance().addExternalClassSpec(project);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
}
