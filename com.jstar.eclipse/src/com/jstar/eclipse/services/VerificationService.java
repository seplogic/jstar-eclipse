/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.services;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;

import com.jstar.eclipse.dialogs.InputFileDialog;
import com.jstar.eclipse.exceptions.ConfigurationException;
import com.jstar.eclipse.exceptions.FolderNotFoundException;
import com.jstar.eclipse.exceptions.InputFileNotFoundException;
import com.jstar.eclipse.exceptions.NoJStarRootFolderException;
import com.jstar.eclipse.exceptions.RequiredFileNotFoundException;
import com.jstar.eclipse.jobs.VerificationJob;
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
		try {
			JStar.getInstance().checkConfigurations();
		} 
		catch (ConfigurationException ce) {
			ConsoleService.getInstance().printErrorMessage(ce.getMessage());
			return;
		}
		
		try {
			checkRequiredFiles(selectedFile);
		} 
		catch (RequiredFileNotFoundException e) {
			return;
		}
		
		final InputFileDialog dialog = new InputFileDialog(shell, selectedFile);
		dialog.setBlockOnOpen(true);
		final int returnValue = dialog.open();
		
		if (returnValue == IDialogConstants.OK_ID) {
			//TODO: check if specification file exists if dialog.isSeparateSpec == false
			executeJStar(selectedFile, !dialog.isSeparateSpec(), dialog.getPrintMode());
		}
	}
	
	private void checkRequiredFiles(JavaFile selectedFile) throws RequiredFileNotFoundException {
		try {
			selectedFile.getJavaProject().getJStarRootFolder();
		}
		catch (NoJStarRootFolderException njsrfe) {
			final IFolder folder = Utils.getInstance().specifyJStarRootFolder(selectedFile.getJavaProject());
			
			if (folder != null) {
				selectedFile.getJavaProject().setJStarRootFolder(folder);
			}
			
			throw new RequiredFileNotFoundException();
		}
		
		try {
			selectedFile.getJavaProject().getGeneratedDir();
			selectedFile.getOutputDirectory();
			selectedFile.getGeneratedDir();
		}
		catch (FolderNotFoundException fnfe) {
			final IFolder folder = selectedFile.getJavaProject().getJStarRootFolder();
			
			//TODO: refactor
			Utils.getInstance().createFolder(folder, fnfe.getFolder().getProjectRelativePath().removeFirstSegments(folder.getProjectRelativePath().segmentCount()));
		}
	}
	
	private void checkInputFiles(JavaFile selectedFile) throws RequiredFileNotFoundException {
		if (!selectedFile.isSpecInSource()) {
			try {
				selectedFile.getSpecFile();
			}
			catch (InputFileNotFoundException ifnfe) {
				throw new RequiredFileNotFoundException();
			}
		}
		
		try {
			selectedFile.getLogicFile();
			selectedFile.getAbsFile();
		}
		catch (InputFileNotFoundException ifnfe) {
			throw new RequiredFileNotFoundException();
		}
	}
	
	public void verify(JavaFile selectedFile, Shell shell) {
		try {
			JStar.getInstance().checkConfigurations();
		} catch (ConfigurationException ce) {
			ConsoleService.getInstance().printErrorMessage(ce.getMessage());
			return;
		}
		
		try {
			checkRequiredFiles(selectedFile);
		} 
		catch (RequiredFileNotFoundException e) {
			return;
		}
		
		boolean isSpecInSource = selectedFile.isSpecInSource();			
		PrintMode mode = selectedFile.getMode();	
		
		try {
			checkInputFiles(selectedFile);
		}
		catch (RequiredFileNotFoundException rfnfe) {
			verifyConfig(selectedFile, shell);
			return;
		}
				
		executeJStar(selectedFile, isSpecInSource, mode);
	}
	
	private void executeJStar(final JavaFile selectedFile, final boolean isSpecInSource, final PrintMode mode) {
		String specFile = isSpecInSource ? null : selectedFile.getSpecFile().getLocation().toOSString();
		String logicFile = selectedFile.getLogicFile().getLocation().toOSString();
		String absFile = selectedFile.getAbsFile().getLocation().toOSString();	
		
		VerificationJob job = new VerificationJob("jStar Verification", selectedFile, isSpecInSource, specFile, logicFile, absFile, mode);
		job.setPriority(Job.SHORT);
		job.setRule(getRule());
		job.schedule(); 
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
