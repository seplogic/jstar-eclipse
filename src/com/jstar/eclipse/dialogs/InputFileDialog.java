package com.jstar.eclipse.dialogs;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.jstar.eclipse.Activator;
import com.jstar.eclipse.services.JStar;
import com.jstar.eclipse.services.JStar.PrintMode;

public class InputFileDialog extends Dialog {
	
	private final String INPUT_FILES = "input_files";
	private final String SPECS = "specs";
	private final String SPEC_EXT = ".spec";
	private final String LOGIC = "logic";
	private final String ABS = "abs";
	
	private IFile selectedFile;
	private Text specField;
	private Text logicField;
	private Text absField;
	private String specFieldValue;
	private String logicFieldValue;
	private String absFieldValue;
	private PrintMode printMode;
	private Button quiet;
	private Button verbose;
	
    private Image image = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "jStar_square.gif").createImage();


	public InputFileDialog(Shell parentShell, IFile selectedFile) {
		super(parentShell);
		this.selectedFile = selectedFile;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentComponent = (Composite)super.createDialogArea(parent);
		Composite component = new Composite(parentComponent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		component.setLayout(layout);
		
		GridData horizontalSpan3 = new GridData();
		horizontalSpan3.horizontalSpan = 3;
		
		Label selectLabel = new Label(component, SWT.NONE);
		selectLabel.setText("Select input files:");
		selectLabel.setLayoutData(horizontalSpan3);
		
		GridData gridData = new GridData();
		gridData.widthHint = 400;
		
		Label specLabel = new Label(component, SWT.NONE);
		specLabel.setText("Pre/post condition specification");

		specField = new Text(component, SWT.BORDER);
		specField.setLayoutData(gridData);
		
		final String fileLocation = new File(selectedFile.getLocation().toString()).getParentFile().getAbsolutePath();
		
		Button specButton = new Button(component, SWT.PUSH);
		specButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				specField.setText(loadFile(getShell(), fileLocation));
			}
		});
		specButton.setText("Browse");
		
		Label logicLabel = new Label(component, SWT.NONE);
		logicLabel.setText("Logic rules");
		
		logicField = new Text(component, SWT.BORDER);
		logicField.setLayoutData(gridData);
		
		Button logicButton = new Button(component, SWT.PUSH);
		logicButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				logicField.setText(loadFile(getShell(), fileLocation));
			}
		});
		logicButton.setText("Browse");
		
		Label absLabel = new Label(component, SWT.NONE);
		absLabel.setText("Abstraction rules");
		
		absField = new Text(component, SWT.BORDER);
		absField.setLayoutData(gridData);
		
		Button absButton = new Button(component, SWT.PUSH);
		absButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				absField.setText(loadFile(getShell(), fileLocation));
			}
		});
		absButton.setText("Browse");
		
		Label separator = new Label(component, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = 10;
		gd.horizontalSpan = 3;
		separator.setLayoutData(gd);
		
	    Group group1 = new Group(component, SWT.SHADOW_IN);
	    group1.setText("Select mode:");
	    group1.setLayout(new RowLayout(SWT.VERTICAL));
	    quiet = new Button(group1, SWT.RADIO);
	    quiet.setText("quiet");
	    verbose = new Button(group1, SWT.RADIO);
	    verbose.setText("verbose");
	    quiet.setSelection(true);
	    
	    setDefaultLocations(fileLocation);
	
		return parentComponent;
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
		
		final File defaultSpecFileClassSpec = new File(inputFileDirectory(fileLocation) + removeFileExtension(selectedFile.getName()) + SPEC_EXT);
		
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
		return fileLocation + File.separator + INPUT_FILES + File.separator;
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
		setPrintMode(getMode());
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

}
