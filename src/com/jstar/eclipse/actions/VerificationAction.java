package com.jstar.eclipse.actions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.json.JSONException;

import com.jstar.eclipse.dialogs.InputFileDialog;
import com.jstar.eclipse.exceptions.ConfigurationException;
import com.jstar.eclipse.objects.JavaFilePersistentProperties;
import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.services.AnnotationProcessingService;
import com.jstar.eclipse.services.ConsoleService;
import com.jstar.eclipse.services.JStar;
import com.jstar.eclipse.services.JStar.PrintMode;

public class VerificationAction {

	protected void verifyConfig(JavaFile selectedFile, Shell shell) {
		checkConfigurations();

		final InputFileDialog dialog = new InputFileDialog(shell, selectedFile);

		dialog.setBlockOnOpen(true);
		final int returnValue = dialog.open();
		
		final List<File> jimpleFiles = JStar.getInstance().convertToJimple(selectedFile);

		if (returnValue == IDialogConstants.OK_ID) {
			executeJStar(selectedFile, !dialog.isSeparateSpec(), dialog.getSpecFieldValue(), dialog.getLogicFieldValue(), dialog.getAbsFieldValue(), jimpleFiles, dialog.getPrintMode());
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
	
	protected void verify(JavaFile selectedFile, Shell shell) {
		checkConfigurations();
		
		boolean isSpecInSource = JavaFilePersistentProperties.isSpecInSourceFile(selectedFile);		
		String specFile = JavaFilePersistentProperties.getSpecFile(selectedFile);
		String logicFile = JavaFilePersistentProperties.getLogicFile(selectedFile);
		String absFile = JavaFilePersistentProperties.getAbsFile(selectedFile);
		PrintMode mode = JavaFilePersistentProperties.getMode(selectedFile);
		final List<File> jimpleFiles = JStar.getInstance().convertToJimple(selectedFile);
		
		if ((StringUtils.isEmpty(specFile) && !isSpecInSource) || logicFile.isEmpty() || absFile.isEmpty()) {
			new ContextMenuVerificationConfigAction().verifyConfig(selectedFile, shell);
			return;
		}
		
		executeJStar(selectedFile, isSpecInSource, specFile, logicFile, absFile, jimpleFiles, mode);
	}
	
	private void executeJStar(JavaFile selectedFile, boolean isSpecInSource, String specFile, String logicFile, String absFile, List<File> jimpleFiles, PrintMode mode) {
		String spec;
		
		if (isSpecInSource) {
			spec = AnnotationProcessingService.getInstance().processAnnotations(selectedFile).getAbsolutePath();
		} 
		else {
			spec = specFile;
		}
		
		try {
			ConsoleService.getInstance().clearConsole();
			
			selectedFile.clearMarkers();
			
			for (File jimpleFile : jimpleFiles) {
				Process pr = JStar.getInstance().executeJStar(spec, logicFile, absFile, jimpleFile.getAbsolutePath(), mode);			
				ConsoleService.getInstance().printToConsole(selectedFile, pr);
			}
			
			ConsoleService.getInstance().printToConsole("jStar Verification is completed.");
		} catch (CoreException ce) {
			ce.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		} catch (IOException ioe) {
			ioe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		} catch (JSONException jsone) {
			jsone.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		} catch (InterruptedException ie) {
			ie.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		}
	}
	
	protected void openFileInEditor(final IFile selectedFile) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();	
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(selectedFile.getName());
		
		try {
			page.openEditor(new FileEditorInput(selectedFile), desc.getId());
		} 
		catch (PartInitException pie) {
			pie.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		}
	}

}
