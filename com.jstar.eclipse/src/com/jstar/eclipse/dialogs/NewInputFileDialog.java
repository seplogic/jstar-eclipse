package com.jstar.eclipse.dialogs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
import com.jstar.eclipse.exceptions.InputFileNotFoundException;
import com.jstar.eclipse.objects.InputFileKind;
import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.services.ConsoleService;

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
		browseButton.setText("Browse...");
		
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String filePath = loadFile(getShell());
				
				if (StringUtils.isNotBlank(filePath)) {		
					final File file = new File(filePath);
					
					if (file.exists()) {	
						try {
							inputFile = createFile(inputFileKind, new FileInputStream(file));
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
				byte[] bytes = "".getBytes();
				final InputStream source = new ByteArrayInputStream(bytes);
				inputFile = createFile(inputFileKind, source);	
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
	
	private IFile createFile(final InputFileKind inputFile, final InputStream source) {		
		IFile file = null;
		
		try {
			selectedFile.getInputFile(inputFile);
		}
		catch (InputFileNotFoundException ifnfe) {
			file = ifnfe.getInputFile();
		}
		
		if (file == null) {
			// File already exists
			return selectedFile.getInputFile(inputFile);
		}
		
		try {
			file.create(source, IResource.NONE, null);
			return file;
		} 
		catch (CoreException ce) {
			ce.printStackTrace(ConsoleService.getInstance().getConsoleStream());
			throw new RuntimeException(ce.getMessage());
		}
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
