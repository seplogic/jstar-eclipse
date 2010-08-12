package com.jstar.eclipse.dialogs;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.jstar.eclipse.Activator;
import com.jstar.eclipse.services.AnnotationProcessingService;
import com.jstar.eclipse.services.JStar;
import com.jstar.eclipse.services.JStar.PrintMode;

public class InputFileDialog extends Dialog {
	
	private final String SPECS = "specs";
	private final String LOGIC = "logic";
	private final String ABS = "abs";
	
	private IFile selectedFile;
	private List<File> jimpleFiles;
	
	private Text specField;
	private Text logicField;
	private Text absField;
	private Combo jimpleFileField;
	private String specFieldValue;
	private String logicFieldValue;
	private String absFieldValue;
	private String jimpleFile;
	private PrintMode printMode;
	private Button quiet;
	private Button verbose;
	
    private Image image = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "jStar_square.gif").createImage();
	private Button specSource;
	private Button specSeparate;
	private Label specLabel;
	private Button specButton;
	private boolean separateSpec;


	public InputFileDialog(Shell parentShell, IFile selectedFile, List<File> jimpleFiles) {
		super(parentShell);
		this.selectedFile = selectedFile;
		this.jimpleFiles = jimpleFiles;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentComponent = (Composite)super.createDialogArea(parent);
		Composite component = new Composite(parentComponent, SWT.NONE);
		component.setLayout(new GridLayout());
		
		addSpecificationGroup(component);
		addInputFilesGroup(component);
		addModeGroup(component);
	    
	    setDefaultLocations(getSelectedFileLocation());
	
		return parentComponent;
	}
	
	private String getSelectedFileLocation() {
		return new File(selectedFile.getLocation().toString()).getParentFile().getAbsolutePath();
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
	    
	    quiet.setSelection(true);
	}

	
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
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				setEnabledSpecSeparate(false);
			}
	    });
	    
	    specSeparate = new Button(group, SWT.RADIO);
	    specSeparate.setText("Specification is in separate file");
	    specSeparate.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
	    
	    specSeparate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setEnabledSpecSeparate(true);
			}
	    });
	    
		GridData gridData = new GridData();
		gridData.widthHint = 400;
		
		specLabel = new Label(group, SWT.NONE);
		specLabel.setText("Specification File:");

		specField = new Text(group, SWT.BORDER);
		specField.setLayoutData(gridData);
				
		specButton = new Button(group, SWT.PUSH);
		specButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				specField.setText(loadFile(getShell(), getSelectedFileLocation()));
			}
		});
		specButton.setText("Browse");
	}
	
	private void setEnabledSpecSeparate(boolean enabled) {
		specField.setEnabled(enabled);
		specLabel.setEnabled(enabled);
		specButton.setEnabled(enabled);
	}
	
	private void addInputFilesGroup(Composite component) {
	    Group group = new Group(component, SWT.SHADOW_IN);
	    group.setText("Input Files");
	    GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 3;
	    gridLayout.horizontalSpacing = 10;
	    gridLayout.verticalSpacing = 10;
	    group.setLayout(gridLayout);
	    
	    if (jimpleFiles.size() == 1) {	
			jimpleFile = jimpleFiles.get(0).getAbsolutePath();
		}
		else {
			Label jimpleLabel = new Label(group, SWT.NONE);
			jimpleLabel.setText("Jimple file");
			
			GridData jimpleData = new GridData();
			jimpleData.horizontalSpan = 2;
			
			jimpleFileField = new Combo(group, SWT.READ_ONLY);
			jimpleFileField.setLayoutData(jimpleData);
					
			for (final File file : jimpleFiles) {
				final String fileName = JStar.getInstance().removeFileExtension(file.getName());
				jimpleFileField.add(fileName);
				jimpleFileField.setData(fileName, file.getAbsolutePath());
			}
			
			jimpleFileField.select(0);
		}
		
		GridData gridData = new GridData();
		gridData.widthHint = 400;		
		
		Label logicLabel = new Label(group, SWT.NONE);
		logicLabel.setText("Logic rules");
		
		logicField = new Text(group, SWT.BORDER);
		logicField.setLayoutData(gridData);
		
		Button logicButton = new Button(group, SWT.PUSH);
		logicButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				logicField.setText(loadFile(getShell(), getSelectedFileLocation()));
			}
		});
		logicButton.setText("Browse");
		
		Label absLabel = new Label(group, SWT.NONE);
		absLabel.setText("Abstraction rules");
		
		absField = new Text(group, SWT.BORDER);
		absField.setLayoutData(gridData);
		
		Button absButton = new Button(group, SWT.PUSH);
		absButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				absField.setText(loadFile(getShell(), getSelectedFileLocation()));
			}
		});
		absButton.setText("Browse");
	}
	
	private void setDefaultLocations(final String fileLocation) {
		setDefaultSpec(fileLocation);
		setDefaultLogic(fileLocation);
		setDefaultAbs(fileLocation);
	}
	
	private void setDefaultAbs(String fileLocation) {
		final File defaultAbsFile = new File(inputFileDirectory(fileLocation) + ABS);
		
		if (defaultAbsFile.exists()) {
			absField.setText(defaultAbsFile.getAbsolutePath());
			return;
		}
		
		absField.setText(JStar.getInstance().getAbsFile());			
	}

	private void setDefaultLogic(final String fileLocation) {
		final File defaultLogicFile = new File(inputFileDirectory(fileLocation) + LOGIC);
		
		if (defaultLogicFile.exists()) {
			logicField.setText(defaultLogicFile.getAbsolutePath());
			return;
		}
		
		logicField.setText(JStar.getInstance().getLogicFile());	
	}

	private void setDefaultSpec(final String fileLocation) {
		final File defaultSpecFileSpecs = new File(inputFileDirectory(fileLocation) + SPECS);
		
		if (defaultSpecFileSpecs.exists()) {
			specField.setText(defaultSpecFileSpecs.getAbsolutePath());
			return;
		}
		
		final File defaultSpecFileClassSpec = new File(inputFileDirectory(fileLocation) + removeFileExtension(selectedFile.getName()) + AnnotationProcessingService.SPEC_EXT);
		
		if (defaultSpecFileClassSpec.exists()) {
			specField.setText(defaultSpecFileClassSpec.getAbsolutePath());
			return;
		}
		
		specField.setText(JStar.getInstance().getSpecFile());
	}
	
	private String removeFileExtension(final String fileName) {
		int dot = fileName.lastIndexOf('.');
		return fileName.substring(0, dot);
	}
	
	private String inputFileDirectory(final String fileLocation) {
		return fileLocation + File.separator + AnnotationProcessingService.INPUT_FILES + File.separator;
	}

	private PrintMode getMode() {
		if (verbose.getSelection()) {
			return PrintMode.VERBOSE;
		}
	
		return PrintMode.QUIET;
	}
	
    private String loadFile (Shell shell, String path) {
        FileDialog fd = new FileDialog(shell, SWT.OPEN);
        fd.setText("Open");
        fd.setFilterPath(path);
        return fd.open();
    }
	
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("jStar verification");
        shell.setImage(image);

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
		setSpecFieldValue(specField.getText());
		setLogicFieldValue(logicField.getText());
		setAbsFieldValue(absField.getText());
		
		if (jimpleFileField != null) {
			setJimpleFile((String)jimpleFileField.getData(jimpleFileField.getItem(jimpleFileField.getSelectionIndex())));
		}
		
		setPrintMode(getMode());
		
		if (specSource.getSelection()) {
			separateSpec = false;
		}
		else {
			separateSpec = true;
		}
		
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
