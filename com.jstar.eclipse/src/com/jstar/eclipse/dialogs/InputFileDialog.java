/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.dialogs;


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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.jstar.eclipse.Activator;
import com.jstar.eclipse.exceptions.InputFileNotFoundException;
import com.jstar.eclipse.objects.InputFileKind;
import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.services.Utils;
import com.jstar.eclipse.services.JStar.PrintMode;

public class InputFileDialog extends Dialog {
	private JavaFile selectedFile;
	
	private Text specField;
	private Text logicField;
	private Text absField;
	private String specFieldValue;
	private String logicFieldValue;
	private String absFieldValue;
	private String jimpleFile;
	private PrintMode printMode;
	private Button quiet;
	private Button verbose;
	
	private Button specSource;
	private Button specSeparate;
	private Label specLabel;
	private Button specButton;
	private boolean separateSpec;
	private Text genSpecField;
	private Label genSpecLabel;
	
	private String OPEN_TEXT = "Open";
	private String ADD_TEXT = "Add";

	private Button logicButton;

	private Button absButton;

	public InputFileDialog(Shell parentShell, JavaFile selectedFile) {
		super(parentShell);
		this.selectedFile = selectedFile;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentComponent = (Composite)super.createDialogArea(parent);
		Composite component = new Composite(parentComponent, SWT.NONE);
		component.setLayout(new GridLayout());
		
		addSpecificationGroup(component);
		addInputFilesGroup(component);
		addModeGroup(component);
	    
	    setDefaultLocations();
	
		return parentComponent;
	}
	
	private void addModeGroup(Composite component) {
	    Group group = new Group(component, SWT.SHADOW_IN);
	    group.setText("Mode");
	    GridLayout gridLayout = new GridLayout();
	    gridLayout.horizontalSpacing = 10;
	    gridLayout.verticalSpacing = 10;
	    group.setLayout(gridLayout);
	    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    
	    quiet = new Button(group, SWT.RADIO);
	    quiet.setText("Run jStar in quiet mode");
	    quiet.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    verbose = new Button(group, SWT.RADIO);
	    verbose.setText("Run jStar in verbose mode");
	    verbose.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

	    setMode(selectedFile.getMode());
	}

	// TODO: refactor
	private void addSpecificationGroup(Composite component) {
	    Group group = new Group(component, SWT.SHADOW_IN);
	    group.setText("Specification");
	    GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 3;
	    gridLayout.horizontalSpacing = 10;
	    gridLayout.verticalSpacing = 10;
	    group.setLayout(gridLayout);
	    
	    specSource = new Button(group, SWT.RADIO);
	    specSource.setText("Specification is included in the source file");
	    specSource.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
	    
	    specSource.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setEnabledSpecSeparate(false);
				setEnabledSpecInSource(true);
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				setEnabledSpecSeparate(false);
				setEnabledSpecInSource(true);
			}
	    });
	    
		GridData gd = new GridData();
		gd.widthHint = 400;
		gd.horizontalSpan = 2;
		
		genSpecLabel = new Label(group, SWT.NONE);
		genSpecLabel.setText("Generated Specification File:");

		genSpecField = new Text(group, SWT.BORDER);
		genSpecField.setEditable(false);
		genSpecField.setLayoutData(gd);
		genSpecField.setText(selectedFile.getGeneratedSpec().getProjectRelativePath().toOSString());
	    
	    specSeparate = new Button(group, SWT.RADIO);
	    specSeparate.setText("Specification is in separate file");
	    specSeparate.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
	    
	    specSeparate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setEnabledSpecSeparate(true);
				setEnabledSpecInSource(false);
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				setEnabledSpecSeparate(true);
				setEnabledSpecInSource(false);
			}
	    });
	    
		GridData gridData = new GridData();
		gridData.widthHint = 400;
		
		specLabel = new Label(group, SWT.NONE);
		specLabel.setText("Specification File:");

		specField = new Text(group, SWT.BORDER);
		specField.setEditable(false);
		specField.setLayoutData(gridData);
		
		boolean specExists = true;
		
		try {
			selectedFile.getSpecFile();
		}
		catch (InputFileNotFoundException ifnfe) {
			specExists = false;
		}
				
		specButton = new Button(group, SWT.PUSH);
		specButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (specButton.getText().equals(OPEN_TEXT)) {
					Utils.getInstance().openFileInEditor(selectedFile.getSpecFile(), true);
					setReturnCode(IDialogConstants.CANCEL_ID);
					close();
				}
				else {			
					final NewInputFileDialog dialog = new NewInputFileDialog(getShell(), selectedFile, InputFileKind.SPEC);
					dialog.setBlockOnOpen(true);
					final int returnValue = dialog.open();
					
					if (returnValue == IDialogConstants.OK_ID) {
						specField.setText(dialog.getInputFile().getProjectRelativePath().toOSString());
						specButton.setText(OPEN_TEXT);
					}
				}
			}
		});
		
		if (specExists) {
			specButton.setText(OPEN_TEXT);
		}
		else {
			specButton.setText(ADD_TEXT);
		}
		
	    if (selectedFile.isSpecInSource()) {
	    	specSource.setSelection(true);
	    	setEnabledSpecSeparate(false);
	    }
	    else {
	    	specSeparate.setSelection(true);
	    	setEnabledSpecSeparate(true);
	    }
	}
	
	private void setEnabledSpecSeparate(boolean enabled) {
		specField.setEnabled(enabled);
		specLabel.setEnabled(enabled);
		specButton.setEnabled(enabled);
	}
	
	private void setEnabledSpecInSource(boolean enabled) {
		genSpecField.setEnabled(enabled);
		genSpecLabel.setEnabled(enabled);
	}
	
	// TODO: refactor
	private void addInputFilesGroup(Composite component) {
	    Group group = new Group(component, SWT.SHADOW_IN);
	    group.setText("Input Files");
	    GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 3;
	    gridLayout.horizontalSpacing = 10;
	    gridLayout.verticalSpacing = 10;
	    group.setLayout(gridLayout);
	    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));	
		
		Label logicLabel = new Label(group, SWT.NONE);
		logicLabel.setText("Logic rules:");
		
		logicField = new Text(group, SWT.BORDER);
		logicField.setEditable(false);
		logicField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		logicButton = new Button(group, SWT.PUSH);
		boolean logicExists = true;
		
		try {
			selectedFile.getLogicFile();
		}
		catch (InputFileNotFoundException ifnfe) {
			logicExists = false;
		}
		
		logicButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (logicButton.getText().equals(OPEN_TEXT)) {
					Utils.getInstance().openFileInEditor(selectedFile.getLogicFile(), true);
					setReturnCode(IDialogConstants.CANCEL_ID);
					close();
				}				
				else {
					final NewInputFileDialog dialog = new NewInputFileDialog(getShell(), selectedFile, InputFileKind.LOGIC);
					dialog.setBlockOnOpen(true);
					final int returnValue = dialog.open();
					
					if (returnValue == IDialogConstants.OK_ID) {
						logicField.setText(dialog.getInputFile().getProjectRelativePath().toOSString());
						logicButton.setText(OPEN_TEXT);
					}
				}
			}
		});
		
		if (logicExists) {
			logicButton.setText(OPEN_TEXT);
		}
		else {
			logicButton.setText(ADD_TEXT);
		}
		
		Label absLabel = new Label(group, SWT.NONE);
		absLabel.setText("Abstraction rules:");
		
		absField = new Text(group, SWT.BORDER);
		absField.setEditable(false);
		absField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		boolean absExists = true;
		
		try {
			selectedFile.getAbsFile();
		}
		catch (InputFileNotFoundException ifnfe) {
			absExists = false;
		}
		
		absButton = new Button(group, SWT.PUSH);
		absButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (absButton.getText().equals(OPEN_TEXT)) {
					Utils.getInstance().openFileInEditor(selectedFile.getAbsFile(), true);
					setReturnCode(IDialogConstants.CANCEL_ID);
					close();
				}
				else {
					final NewInputFileDialog dialog = new NewInputFileDialog(getShell(), selectedFile, InputFileKind.ABS);
					dialog.setBlockOnOpen(true);
					final int returnValue = dialog.open();
					
					if (returnValue == IDialogConstants.OK_ID) {
						absField.setText(dialog.getInputFile().getProjectRelativePath().toOSString());
						absButton.setText(OPEN_TEXT);
					}
				}
				
			}
		});
		
		if (absExists) {
			absButton.setText(OPEN_TEXT);
		}
		else {
			absButton.setText(ADD_TEXT);
		}
	}
	
	private void setDefaultLocations() {		
		specField.setText(getInputFileLocation(InputFileKind.SPEC));
		logicField.setText(getInputFileLocation(InputFileKind.LOGIC));
		absField.setText(getInputFileLocation(InputFileKind.ABS));
	}
	
	private String getInputFileLocation(final InputFileKind inputFile) {
		try {
			return selectedFile.getInputFile(inputFile).getProjectRelativePath().toOSString();
		}
		catch (InputFileNotFoundException ifnfe) {
			return "";
		}
	}

	private PrintMode getMode() {
		if (verbose.getSelection()) {
			return PrintMode.VERBOSE;
		}
	
		return PrintMode.QUIET;
	}
	
	private void setMode(PrintMode mode) {
	    if (mode.equals(PrintMode.QUIET)) {
			quiet.setSelection(true);
		}
		else {
			verbose.setSelection(true);
		}
	}
	
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("jStar verification");
        shell.setImage(Activator.image);
    }
    
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.RIGHT;

		parent.setLayoutData(gridData);

		Button button = createButton (parent, IDialogConstants.OK_ID, "Verify", true);
		button.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						setReturnCode(IDialogConstants.OK_ID);
						close();
					}
				});
		Button button1 = createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
		button1.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						setReturnCode(IDialogConstants.CANCEL_ID);
						close();
					}
				});
	}

	@Override
	protected void okPressed() {	
		final PrintMode mode = getMode();
		setPrintMode(mode);
		selectedFile.setMode(mode);

		
		if (specSource.getSelection()) {
			separateSpec = false;
		}
		else {
			separateSpec = true;
		}
		
		selectedFile.setSpecInSource(!separateSpec);
		
		super.okPressed();
	}
	
	public Text getSpecField() {
		return specField;
	}

	public Text getLogicField() {
		return logicField;
	}

	public Text getAbsField() {
		return absField;
	}

	public void setSpecFieldValue(String specFieldValue) {
		this.specFieldValue = specFieldValue;
	}
	
	public String getSpecFieldValue() {
		return specFieldValue;
	}

	public void setLogicFieldValue(String logicFieldValue) {
		this.logicFieldValue = logicFieldValue;
	}

	public String getLogicFieldValue() {
		return logicFieldValue;
	}

	public void setAbsFieldValue(String absFieldValue) {
		this.absFieldValue = absFieldValue;
	}

	public String getAbsFieldValue() {
		return absFieldValue;
	}

	public void setPrintMode(PrintMode printMode) {
		this.printMode = printMode;
	}

	public PrintMode getPrintMode() {
		return printMode;
	}

	public String getJimpleFile() {
		return jimpleFile;
	}
	
	public void setJimpleFile(String jimpleFile) {
		this.jimpleFile = jimpleFile;
	}

	public boolean isSeparateSpec() {
		return separateSpec;
	}
}
