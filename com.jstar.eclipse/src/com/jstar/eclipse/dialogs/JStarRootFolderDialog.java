package com.jstar.eclipse.dialogs;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.NewFolderDialog;

public class JStarRootFolderDialog extends ElementTreeSelectionDialog {
	
	private List<IFolder> foldersToDelete = new LinkedList<IFolder>();

	public List<IFolder> getFoldersToDelete() {
		return foldersToDelete;
	}

	public JStarRootFolderDialog(final Shell parent, final ILabelProvider labelProvider, final ITreeContentProvider contentProvider) {
		super(parent, labelProvider, contentProvider);
	}
	
	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite parentComponent = (Composite)super.createDialogArea(parent);
		final Composite component = new Composite(parentComponent, SWT.NONE);
		component.setLayout(new GridLayout());
		
		final Button button = new Button(parentComponent, SWT.PUSH);
		button.setText("Create New Folder...");
		final TreeViewer viewer = getTreeViewer();
		
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				IContainer container = null;
				
				if (selection.size() > 0) {
					final Object object = selection.getFirstElement();
					
					if (object instanceof IContainer) {
						container = (IContainer) object;
					}
				}
				
				if (container != null) {
					final NewFolderDialog dialog = new NewFolderDialog(getShell(), container);
					
					if (dialog.open() == Window.OK) {
						final Object objectToCreate = dialog.getResult()[0];
						
						if (objectToCreate instanceof IFolder) {
							final IFolder folderToCreate = (IFolder) objectToCreate;
							viewer.refresh(container);
							viewer.setSelection(new StructuredSelection(folderToCreate));
							foldersToDelete.add(folderToCreate);
						}
					}
				}
			}
		});
		
		return parentComponent;
	}
}
