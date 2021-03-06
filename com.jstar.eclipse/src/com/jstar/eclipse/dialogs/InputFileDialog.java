/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.dialogs;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.jstar.eclipse.Activator;
import com.jstar.eclipse.exceptions.InputFileNotFoundException;
import com.jstar.eclipse.objects.InputFileKind;
import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.services.ConsoleService;
import com.jstar.eclipse.services.JStar.DebugMode;
import com.jstar.eclipse.services.Utils;
import com.jstar.eclipse.services.JStar.PrintMode;

public class InputFileDialog extends Dialog {
	private static final String PARSING = "Parsing";
	private static final String SYMBOLIC = "Symbolic execution";
	private static final String CORE = "Core flowgraph";
	private static final String SMT = "Smt solver";

	private JavaFile selectedFile;
	
	private Text specField;
	private Text logicField;
	private Text absField;
	private String specFieldValue;
	private String logicFieldValue;
	private String absFieldValue;
	private String jimpleFile;
	private PrintMode printMode;
	private String debugMode;
	private Button quiet;
	private Button verbose;
	private Button parsing;
	private Button symbolic;
	private Button core;
	private Button smt;
	
	private Button specSource;
	private Button specSeparate;
	private Label specLabel;
	private Button specButton;
	private boolean separateSpec;
	private Text genSpecField;
	private Label genSpecLabel;
	
	private String OPEN_TEXT = "Open";
	private String ADD_TEXT = "New";

	private Button logicButton;

	private Button absButton;

	private Button specImportButton;

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
		addDebugModes(component);
	    
	    setDefaultLocations();
	
		return parentComponent;
	}
	
	private void addDebugModes(Composite component) {
		Group group = new Group(component, SWT.SHADOW_IN);
		group.setText("Debug modes");
		GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 10;
	    gridLayout.verticalSpacing = 10;
	    group.setLayout(gridLayout);
	    group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    
	    parsing = new Button(group, SWT.CHECK);
	    parsing.setText(PARSING);
	    parsing.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    
	    symbolic = new Button(group, SWT.CHECK);
	    symbolic.setText(SYMBOLIC);
	    symbolic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    
	    core = new Button(group, SWT.CHECK);
	    core.setText(CORE);
	    core.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    
	    smt = new Button(group, SWT.CHECK);
	    smt.setText(SMT);
	    smt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	    
	    setDebugModes(selectedFile.getDebugModes());
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
	    gridLayout.numColumns = 4;
	    gridLayout.horizontalSpacing = 10;
	    gridLayout.verticalSpacing = 10;
	    group.setLayout(gridLayout);
	    
	    specSource = new Button(group, SWT.RADIO);
	    specSource.setText("Specification is included in the source file");
	    specSource.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1));
	    
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
	    specSeparate.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1));
	    
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
					final IFile inputFile = Utils.getInstance().createEmptyFile(selectedFile, InputFileKind.SPEC);
					specField.setText(inputFile.getProjectRelativePath().toOSString());
					specButton.setText(OPEN_TEXT);
					specButton.pack();
				}
			}
		});
		
		specImportButton = new Button(group, SWT.PUSH);
		specImportButton.setText("Import...");
		specImportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final IFile inputFile = importInputFile(InputFileKind.SPEC);
				
				if (inputFile != null) {
					specField.setText(inputFile.getProjectRelativePath().toOSString());
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
	    	setEnabledSpecInSource(true);
	    }
	    else {
	    	specSeparate.setSelection(true);
	    	setEnabledSpecSeparate(true);
	    	setEnabledSpecInSource(false);
	    }
	}
	
	private IFile importInputFile(final InputFileKind inputFileKind) {
		final String filePath = loadFile();
		
		if (StringUtils.isNotBlank(filePath)) {		
			final File file = new File(filePath);
			
			if (file.exists()) {	
				try {
					return Utils.getInstance().createFile(selectedFile, inputFileKind, new FileInputStream(file), true);
				} 
				catch (FileNotFoundException fnfe) {
					fnfe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
				}	
			}
		}
		
		return null;
	}
	
	private void setEnabledSpecSeparate(boolean enabled) {
		specField.setEnabled(enabled);
		specLabel.setEnabled(enabled);
		specButton.setEnabled(enabled);
		specImportButton.setEnabled(enabled);
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
	    gridLayout.numColumns = 4;
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
					final IFile inputFile = Utils.getInstance().createEmptyFile(selectedFile, InputFileKind.LOGIC);
					logicField.setText(inputFile.getProjectRelativePath().toOSString());
					logicButton.setText(OPEN_TEXT);	
					logicButton.pack();
				}
			}
		});
		
		final Button logicImportButton = new Button(group, SWT.PUSH);
		logicImportButton.setText("Import...");
		logicImportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final IFile inputFile = importInputFile(InputFileKind.LOGIC);
				
				if (inputFile != null) {
					logicField.setText(inputFile.getProjectRelativePath().toOSString());
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
					final IFile inputFile = Utils.getInstance().createEmptyFile(selectedFile, InputFileKind.ABS);	
					absField.setText(inputFile.getProjectRelativePath().toOSString());
					absButton.setText(OPEN_TEXT);
					absButton.pack();
				}
				
			}
		});
		
		final Button absImportButton = new Button(group, SWT.PUSH);
		absImportButton.setText("Import...");
		absImportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final IFile inputFile = importInputFile(InputFileKind.ABS);
				
				if (inputFile != null) {
					absField.setText(inputFile.getProjectRelativePath().toOSString());
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
	
	private Set<DebugMode> getDebugModes() {
		final Set<DebugMode> debugModes = new HashSet<DebugMode>();
		
		if (parsing.getSelection()) {
			debugModes.add(DebugMode.PARSING);
		}
		
		if (symbolic.getSelection()) {
			debugModes.add(DebugMode.SYMBOLIC);
		}
		
		if (core.getSelection()) {
			debugModes.add(DebugMode.CORE);
		}
		
		if (smt.getSelection()) {
			debugModes.add(DebugMode.SMT);
		}
	
		return debugModes;
	}
	
	private void setDebugModes(Set<DebugMode> modes) {
		for (final DebugMode mode : modes) {
			checkDebugMode(mode);
		}
	}
	
    private void checkDebugMode(DebugMode mode) {
    	switch (mode) {
		case PARSING:
			parsing.setSelection(true);
			break;
		case SYMBOLIC:
			symbolic.setSelection(true);
			break;
		case CORE:
			core.setSelection(true);
			break;
		case SMT:
			smt.setSelection(true);
			break;
		default:
			break;
		}
	}

	private String loadFile () {
        final FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
        fd.setText("Open");      
        return fd.open();
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
		final Set<DebugMode> debugModes = getDebugModes();
		saveDebugMode(debugModes);
		selectedFile.setDebugModes(debugModes);

		
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
	
	public void saveDebugMode(Set<DebugMode> debugModes) {
		StringBuilder debugMode = new StringBuilder();
		
		for (final DebugMode mode : debugModes) {
			debugMode.append(mode.getCmdOption());
		}
		
		this.debugMode = debugMode.toString();
	}
	
	public String retrieveDebugMode() {
		return debugMode;
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
