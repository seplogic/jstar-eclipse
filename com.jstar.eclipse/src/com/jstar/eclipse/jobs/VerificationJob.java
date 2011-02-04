/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.jobs;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.json.JSONException;

import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.services.AnnotationProcessingService;
import com.jstar.eclipse.services.ConsoleService;
import com.jstar.eclipse.services.JStar;
import com.jstar.eclipse.services.Utils;
import com.jstar.eclipse.services.JStar.PrintMode;

public class VerificationJob extends Job {
	
	private boolean isSpecInSource;
	private JavaFile selectedFile;
	private String specFile;
	private String logicFile;
	private String absFile;
	private PrintMode mode;
	private String debugMode;

	public VerificationJob(String name, final JavaFile selectedFile, final boolean isSpecInSource, final String specFile, final String logicFile, final String absFile, final PrintMode mode, String debugMode) {
		super(name);
		this.isSpecInSource = isSpecInSource;
		this.selectedFile = selectedFile;
		this.specFile = specFile;
		this.logicFile = logicFile;
		this.absFile = absFile;
		this.mode = mode;
		this.debugMode = debugMode;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		String spec;
		
		if (isSpecInSource) {
			spec = AnnotationProcessingService.getInstance().processAnnotations(selectedFile).getAbsolutePath();
			Utils.getInstance().makeImportsReady(selectedFile);
		} 
		else {
			spec = specFile;
		}
		
		List<File> jimpleFiles = JStar.getInstance().convertToJimple(selectedFile);
		
		try {		
			selectedFile.clearMarkers();
			
			for (File jimpleFile : jimpleFiles) {
				Process pr = JStar.getInstance().executeJStar(selectedFile.getWorkingDirectory(), selectedFile.getJavaProject().getGeneratedDir(), spec, logicFile, absFile, jimpleFile.getAbsolutePath(), mode, debugMode);			
				ConsoleService.getInstance().printToConsole(selectedFile, pr);
			}
			
			ConsoleService.getInstance().printToConsole("jStar Verification is completed.");
		} 
		catch (CoreException ce) {
			ce.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		} 
		catch (IOException ioe) {
			ioe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		} 
		catch (JSONException jsone) {
			jsone.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		} 
		catch (InterruptedException ie) {
			ie.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		}
		
		return Status.OK_STATUS;
	}

}
