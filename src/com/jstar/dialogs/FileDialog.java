package com.jstar.dialogs;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.jstar.services.JStar;


public class FileDialog extends Dialog {
	private IFile selectedFile;
	private FileFieldEditor specField;
	private FileFieldEditor logicField;
	private FileFieldEditor absField;
	private Button button;
	private Button button1;

	public FileDialog(Shell parentShell, IFile selectedFile) {
		super(parentShell);
		this.selectedFile = selectedFile;
	}

	public Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		specField = new FileFieldEditor("spec", "Specification", composite);
		logicField = new FileFieldEditor("logic", "Logic rules", composite);
		absField = new FileFieldEditor("abs", "Abstraction rules", composite);
		button = new Button(composite, SWT.NONE);
		button.setText("Verify");
		button
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						executejStar();
					}
				});
		button1 = new Button(composite, SWT.NONE);
		button1.setText("Cancel");
		button1
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						closeMe();
					}
				});
		return composite;
	}

	private void closeMe() {
		this.close();
	}

	public FileFieldEditor getSpecField() {
		return specField;
	}

	public FileFieldEditor getLogicField() {
		return logicField;
	}

	public FileFieldEditor getAbsField() {
		return absField;
	}

	private void executejStar() {

		try {
			Process pr = JStar.getInstance().executeJStar(
					selectedFile.getLocation().toString(),
					specField.getStringValue(), 
					logicField.getStringValue(),
					absField.getStringValue());
			

			BufferedReader input = new BufferedReader(new InputStreamReader(pr
					.getInputStream()));

			String line = null;

		    MessageConsole myConsole = findConsole("Console");
		    MessageConsoleStream out = myConsole.newMessageStream();
			   
			while ((line = input.readLine()) != null) {
				out.println(line);
			}

			int exitVal = pr.waitFor();
			System.out.println("Exited with error code " + exitVal);
			
			closeMe();

		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}

	}
	
   private MessageConsole findConsole(String name) {
	      ConsolePlugin plugin = ConsolePlugin.getDefault();
	      IConsoleManager conMan = plugin.getConsoleManager();
	      IConsole[] existing = conMan.getConsoles();
	      for (int i = 0; i < existing.length; i++)
	         if (name.equals(existing[i].getName()))
	            return (MessageConsole) existing[i];
	      //no console found, so create a new one
	      MessageConsole myConsole = new MessageConsole(name, null);
	      conMan.addConsoles(new IConsole[]{myConsole});
	      return myConsole;
	   }

}
