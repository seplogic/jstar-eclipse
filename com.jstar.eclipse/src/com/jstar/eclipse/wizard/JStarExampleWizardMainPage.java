package com.jstar.eclipse.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class JStarExampleWizardMainPage extends WizardPage {
	
	private String info;

	protected JStarExampleWizardMainPage(String pageName, final String info) {
		super(pageName);
		this.info = info;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout();
		composite.setLayout(gl);
		new Label(composite, SWT.NONE).setText(info);
		setControl(composite);
	}

}
