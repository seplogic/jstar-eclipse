package com.jstar.eclipse.services;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.jstar.eclipse.dialogs.InputFileDialog;
import com.jstar.eclipse.exceptions.ConfigurationException;
import com.jstar.eclipse.jobs.VerificationJob;
import com.jstar.eclipse.objects.JavaFilePersistentProperties;
import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.services.JStar.PrintMode;

public class VerificationService {
	
	private static VerificationService instance;
	
	private Mutex rule;
	
	private VerificationService() {
	}

	public static VerificationService getInstance() {
		if (instance == null) {
			instance = new VerificationService();
		}
		return instance;
	}

	public void verifyConfig(JavaFile selectedFile, Shell shell) {	
		checkConfigurations();

		final InputFileDialog dialog = new InputFileDialog(shell, selectedFile);

		dialog.setBlockOnOpen(true);
		final int returnValue = dialog.open();
		
		if (returnValue == IDialogConstants.OK_ID) {
			executeJStar(selectedFile, !dialog.isSeparateSpec(), dialog.getSpecFieldValue(), dialog.getLogicFieldValue(), dialog.getAbsFieldValue(), dialog.getPrintMode());
		}
	}
	
	private void checkConfigurations() {
		try {
			JStar.getInstance().checkConfigurations();
		} catch (ConfigurationException re) {
			ConsoleService.getInstance().printErrorMessage(re.getMessage());
			throw new IllegalArgumentException();
		}
	}
	
	public void verify(JavaFile selectedFile, Shell shell) {
		checkConfigurations();
		
		boolean isSpecInSource = JavaFilePersistentProperties.isSpecInSourceFile(selectedFile);		
		String specFile = JavaFilePersistentProperties.getSpecFile(selectedFile);
		String logicFile = JavaFilePersistentProperties.getLogicFile(selectedFile);
		String absFile = JavaFilePersistentProperties.getAbsFile(selectedFile);
		PrintMode mode = JavaFilePersistentProperties.getMode(selectedFile);
		
		if ((StringUtils.isEmpty(specFile) && !isSpecInSource) || logicFile.isEmpty() || absFile.isEmpty()) {
			verifyConfig(selectedFile, shell);
			return;
		}
		
		executeJStar(selectedFile, isSpecInSource, specFile, logicFile, absFile, mode);
	}
	
	private void executeJStar(final JavaFile selectedFile, final boolean isSpecInSource, final String specFile, final String logicFile, final String absFile, final PrintMode mode) {
		VerificationJob job = new VerificationJob("jStar Verification", selectedFile, isSpecInSource, specFile, logicFile, absFile, mode);
		job.setPriority(Job.SHORT);
		job.setRule(getRule());
		job.schedule(); 
	}
	
	public void openFileInEditor(final IFile selectedFile) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();	
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(selectedFile.getName());
		
		try {
			page.openEditor(new FileEditorInput(selectedFile), desc.getId());
		} 
		catch (PartInitException pie) {
			pie.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		}
	}
	
	private Mutex getRule() {
		if (rule == null) {
			rule = new Mutex();
		}
		
		return rule;
	}
	
	public class Mutex implements ISchedulingRule {
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	}

}
