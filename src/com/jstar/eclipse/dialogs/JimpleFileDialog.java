package com.jstar.eclipse.dialogs;

import java.io.File;
import java.util.List;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.jstar.eclipse.Activator;
import com.jstar.eclipse.services.JStar;

public class JimpleFileDialog extends Dialog {
	
	private List<File> jimpleFiles;
	private Combo jimpleFileField;
	private String jimpleFile;	
	private Image image = Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons" + File.separator + "jStar_square.gif").createImage();

	public JimpleFileDialog(Shell parentShell, List<File> jimpleFiles) {
		super(parentShell);
		this.jimpleFiles = jimpleFiles;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentComponent = (Composite)super.createDialogArea(parent);
		Composite component = new Composite(parentComponent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		component.setLayout(gridLayout);

		Label jimpleLabel = new Label(component, SWT.NONE);
		jimpleLabel.setText("Select Jimple file");
		
		final GridData gridData = new GridData();
		gridData.horizontalIndent = 10;
		jimpleFileField = new Combo(component, SWT.READ_ONLY);
		jimpleFileField.setLayoutData(gridData);
				
		for (final File file : jimpleFiles) {
			final String fileName = JStar.getInstance().removeFileExtension(file.getName());
			jimpleFileField.add(fileName);
			jimpleFileField.setData(fileName, file.getAbsolutePath());
		}
		
		jimpleFileField.select(0);
		
		return parentComponent;
	}
	
	@Override
	protected void okPressed() {	
		setJimpleFile((String)jimpleFileField.getData(jimpleFileField.getItem(jimpleFileField.getSelectionIndex())));	
		super.okPressed();
	}
	
	public String getJimpleFile() {
		return jimpleFile;
	}
	
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("jStar verification");
        shell.setImage(image);
    }
	
	public void setJimpleFile(String jimpleFile) {
		this.jimpleFile = jimpleFile;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = SWT.CENTER;

		parent.setLayoutData(gridData);

		Button button = createButton (parent, IDialogConstants.OK_ID, "Verify", true);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(IDialogConstants.OK_ID);
				close();
			}
		});
	}

}
