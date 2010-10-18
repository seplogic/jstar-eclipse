package com.jstar.eclipse.dialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import com.jstar.eclipse.Activator;
import com.jstar.eclipse.objects.InputFileKind;
import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.services.ConsoleService;
import com.jstar.eclipse.services.Utils;

public class NewInputFileDialog extends Dialog {
	
	private JavaFile selectedFile;
	private InputFileKind inputFileKind;
	private IFile inputFile;

	private Button newButton;
	private Button browseButton;

	protected NewInputFileDialog(Shell parentShell, JavaFile selectedFile, InputFileKind kind) {
		super(parentShell);
		this.selectedFile = selectedFile;
		this.inputFileKind = kind;
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		return null;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentComponent = (Composite)super.createDialogArea(parent);
		Composite component = new Composite(parentComponent, SWT.NONE);
		component.setLayout(new GridLayout());
		
		addNewFileButton(component);		
		addBrowseButton(component);
	
		return parentComponent;
	}

	private void addBrowseButton(Composite component) {
		browseButton = new Button(component, SWT.PUSH);
		browseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		browseButton.setText("Import...");
		
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String filePath = loadFile(getShell());
				
				if (StringUtils.isNotBlank(filePath)) {		
					final File file = new File(filePath);
					
					if (file.exists()) {	
						try {
							inputFile = Utils.getInstance().createFile(selectedFile, inputFileKind, new FileInputStream(file));
							setReturnCode(IDialogConstants.OK_ID);
							close();
						} catch (FileNotFoundException fnfe) {
							fnfe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
						}	
					}
				}
			}
		});
	}

	private void addNewFileButton(Composite component) {
		newButton = new Button(component, SWT.PUSH);
		newButton.setText("Create an empty file");
		
		newButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				inputFile = Utils.getInstance().createEmptyFile(selectedFile, inputFileKind);	
				setReturnCode(IDialogConstants.OK_ID);
				close();
			}
		});
	}
	
	@Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("jStar input file");
        shell.setImage(Activator.image);
    }

	public IFile getInputFile() {		
		return inputFile;
	}
	
    private String loadFile (final Shell shell) {
        final FileDialog fd = new FileDialog(shell, SWT.OPEN);
        fd.setText("Open");      
        return fd.open();
    }
}
