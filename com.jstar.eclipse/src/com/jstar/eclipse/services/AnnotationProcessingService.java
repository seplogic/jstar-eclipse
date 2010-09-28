/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.services;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

import com.jstar.eclipse.Activator;
import com.jstar.eclipse.objects.JavaFile;

public class AnnotationProcessingService {

	public final static String SPEC_EXT = ".spec";
	private static final String PROCESSOR = "com.jstar.eclipse.processing.SpecAnnotationProcessor";
	
	private static AnnotationProcessingService instance;
	
	private AnnotationProcessingService() {
	}
	
	public static AnnotationProcessingService getInstance() {
		if (instance == null) {
			instance = new AnnotationProcessingService();
		}
		return instance;
	}
	
	public File processAnnotations(JavaFile selectedFile) {
		
		final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
		
		if (javac == null) {
			ConsoleService.getInstance().printErrorMessage("Cannot find java compiler. Please check if you are using JDK and not JRE.");
			throw new NullPointerException();
		}
		
		final String generated = selectedFile.makeGeneratedDir();
		
		
		final URL processorURL = FileLocator.find(Activator.getDefault().getBundle(), new Path(Activator.PROCESSOR_PATH), null);
		final URL requiredLib = FileLocator.find(Activator.getDefault().getBundle(), new Path(Activator.COMMONS_IO_1_4_PATH), null);
		String processorLocation = "";
		
		try {
			processorLocation = FileLocator.toFileURL(processorURL).getFile();
		} 
		catch (IOException ioe) {
			ConsoleService.getInstance().printErrorMessage("Cannot obtain the location of annotation processor.");
			ioe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
			throw new RuntimeException();
		}
		
		String requiredLibLocation = "";
		
		try {
			requiredLibLocation = FileLocator.toFileURL(requiredLib).getFile();
		} 
		catch (IOException ioe) {
			ConsoleService.getInstance().printErrorMessage("Cannot obtain the location of required libraries for annotation processor.");
			ioe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
			throw new RuntimeException();
		}
				
		String[] arguments = {
				"-proc:only", 
				"-d", generated, 
				"-cp", selectedFile.getProjectClasspath() + File.pathSeparator + processorLocation + File.pathSeparator + requiredLibLocation,
				"-processor", PROCESSOR,
				selectedFile.getAbsolutePath()
		};
		
		int exitValue = javac.run(null, null, ConsoleService.getInstance().getConsoleStream(), arguments);
		
		if (exitValue != 0) {
			ConsoleService.getInstance().printErrorMessage("An error occurred while processing annotations.");
			throw new RuntimeException();
		}
		
		final File specFile = new File(generated + File.separator + selectedFile.getNameWithoutExtension() + SPEC_EXT);
		
		if (!specFile.exists()) {
			ConsoleService.getInstance().printErrorMessage("Any annotations could be found in the source file.");
			throw new NullPointerException();
		}

		return specFile;
	}

}
