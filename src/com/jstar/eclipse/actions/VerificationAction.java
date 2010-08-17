package com.jstar.eclipse.actions;

import java.io.File;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;

import com.jstar.eclipse.dialogs.InputFileDialog;
import com.jstar.eclipse.objects.IFilePersistentProperties;
import com.jstar.eclipse.objects.VerificationError;
import com.jstar.eclipse.services.AnnotationProcessingService;
import com.jstar.eclipse.services.ConsoleService;
import com.jstar.eclipse.services.JStar;
import com.jstar.eclipse.services.JStar.PrintMode;

public class VerificationAction {

	protected void verifyConfig(IFile selectedFile, Shell shell) {
		try {
			JStar.getInstance().checkConfigurations();
		} catch (RuntimeException re) {
			re.printStackTrace();
			re.printStackTrace(ConsoleService.getInstance().getConsoleStream());
			return;
		}

		final List<File> jimpleFiles = JStar.getInstance().convertToJimple(selectedFile);

		final InputFileDialog dialog = new InputFileDialog(shell, selectedFile);

		dialog.setBlockOnOpen(true);
		final int returnValue = dialog.open();

		if (returnValue == IDialogConstants.OK_ID) {
			executeJStar(selectedFile, !dialog.isSeparateSpec(), dialog.getSpecFieldValue(), dialog.getLogicFieldValue(), dialog.getAbsFieldValue(), jimpleFiles, dialog.getPrintMode());
		}
	}
	
	protected void verify(IFile selectedFile, Shell shell) {	
		boolean isSpecInSource = IFilePersistentProperties.isSpecInSourceFile(selectedFile);		
		String specFile = IFilePersistentProperties.getSpecFile(selectedFile);
		String logicFile = IFilePersistentProperties.getLogicFile(selectedFile);
		String absFile = IFilePersistentProperties.getAbsFile(selectedFile);
		PrintMode mode = IFilePersistentProperties.getMode(selectedFile);
		final List<File> jimpleFiles = JStar.getInstance().convertToJimple(selectedFile);
		
		if ((StringUtils.isEmpty(specFile) && !isSpecInSource) || logicFile.isEmpty() || absFile.isEmpty()) {
			new ContextMenuVerificationConfigAction().verifyConfig(selectedFile, shell);
			return;
		}
		
		executeJStar(selectedFile, isSpecInSource, specFile, logicFile, absFile, jimpleFiles, mode);
	}
	
	private void executeJStar(IFile selectedFile, boolean isSpecInSource, String specFile, String logicFile, String absFile, List<File> jimpleFiles, PrintMode mode) {
		String spec;
		if (isSpecInSource) {
			spec = AnnotationProcessingService.getInstance().processAnnotations(selectedFile).getAbsolutePath();
		} else {
			spec = specFile;
		}
		try {
			ConsoleService.getInstance().clearConsole();
			
			ConsoleService.getInstance().clearMarkers(selectedFile);
			
			for (File jimpleFile : jimpleFiles) {
				Process pr = JStar.getInstance().executeJStar(selectedFile, spec, logicFile, absFile, jimpleFile.getAbsolutePath(), mode);			
				ConsoleService.getInstance().printToConsole(selectedFile, pr);
			}
			
			ConsoleService.getInstance().printToConsole("jStar Verification is completed.");
		} catch (Exception exc) {
			exc.printStackTrace();
			exc.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		}
	}

}
