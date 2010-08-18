package com.jstar.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;

import com.jstar.eclipse.services.ConsoleService;

public class ToolbarVerificationAction extends VerificationAction implements
		IWorkbenchWindowPulldownDelegate {
	
	private IWorkbenchWindow window;
	private Menu menu;

	@Override
	public Menu getMenu(Control parent) {
		if (menu == null) {
			menu = createMenu(parent);
		}
		
		return menu;
	}
	
	private IFile getSelectedFile() {
		IEditorInput editorInput = window.getActivePage().getActiveEditor().getEditorInput();

		IFile selectedFile = (IFile) editorInput.getAdapter(IFile.class);

		if (selectedFile == null) {
			ConsoleService.getInstance().printErrorMessage("Cannot access source file.");
			throw new NullPointerException();
		}
		
		return selectedFile;
	}

	private Menu createMenu(Control parent) {
		final Menu menu = new Menu(parent);
		final MenuItem run = new MenuItem(menu, SWT.PUSH);
		run.setText("Verify with jStar");
		run.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				verify(getSelectedFile(), window.getShell());
			}
		});
		
		final MenuItem config = new MenuItem(menu, SWT.PUSH);
		config.setText("Verify with jStar Configutations...");
		config.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				verifyConfig(getSelectedFile(), window.getShell());
			}
		});
		
		return menu;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void run(IAction action) {
		verify(getSelectedFile(), window.getShell());
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
