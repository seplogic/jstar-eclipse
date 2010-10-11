package com.jstar.eclipse.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IJavaProject;

import com.jstar.eclipse.exceptions.NoJStarRootFolderException;
import com.jstar.eclipse.objects.JavaProject;
import com.jstar.eclipse.services.Utils;

public class ProjectPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	
	private Text jStarRootFolderTextField;
	private JavaProject javaProject;

	public ProjectPropertyPage() {
		super();
	}

	@Override
	protected Control createContents(Composite parent) {
		javaProject = new JavaProject((IJavaProject)getElement());
		
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
	    gridLayout.numColumns = 3;
	    gridLayout.horizontalSpacing = 10;
	    gridLayout.verticalSpacing = 10;
		composite.setLayout(gridLayout);
		
		Label description = new Label(composite, SWT.NONE);
		GridData labelGridData = new GridData();
		labelGridData.horizontalSpan = 3;
		description.setLayoutData(labelGridData);
		description.setText("The root folder of jStar input files (generated jimple files, specification files, logic and abstraction rule files)");
		
		Label rootLabel = new Label(composite, SWT.NONE);
		rootLabel.setText("Root folder:");
		
		jStarRootFolderTextField = new Text(composite, SWT.BORDER);
		jStarRootFolderTextField.setEditable(false);
		GridData rootData = new GridData();
		rootData.horizontalAlignment = SWT.FILL;
		rootData.grabExcessHorizontalSpace = true;
		jStarRootFolderTextField.setLayoutData(rootData);
		jStarRootFolderTextField.setText(getJStarRootFolder());
		
		Button browseButton = new Button(composite, SWT.PUSH);
		
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final IFolder folder = Utils.getInstance().specifyJStarRootFolder(javaProject);
				
				if (folder != null) {
					jStarRootFolderTextField.setText(folder.getProjectRelativePath().toOSString());
				}
			}
		});
		
		browseButton.setText("Browse");
		
		noDefaultAndApplyButton();
		
		return composite;
	}

	private String getJStarRootFolder() {
		try {
			return javaProject.getJStarRootFolder().getProjectRelativePath().toOSString();
		}
		catch (NoJStarRootFolderException njsrfe) {
			return "";
		}
	}
	
	public boolean performOk() {
		// TODO: Ask if a user really wants to change root folder
		javaProject.setJStarRootFolder(javaProject.getProject().getProject().getFolder(jStarRootFolderTextField.getText()));
	    return super.performOk();
	}

}
