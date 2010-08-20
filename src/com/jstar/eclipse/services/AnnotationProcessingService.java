package com.jstar.eclipse.services;

import java.io.File;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.preferences.PreferenceConstants;

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
		
		final String generated = selectedFile.makeGeneratedDir();
		
		String[] arguments = {
				"-proc:only", 
				"-d", generated, 
				"-cp", selectedFile.getProjectClasspath() + File.pathSeparator + PreferenceConstants.getAnnotationProcessorPath(),
				"-processor", PROCESSOR,
				selectedFile.getAbsolutePath()
		};
		
		int exitValue = javac.run(null, null, ConsoleService.getInstance().getConsoleStream(), arguments);
		
		if (exitValue != 0) {
			ConsoleService.getInstance().printErrorMessage("An error occurred while processing annotations.");
			throw new RuntimeException();
		}
		
		final File specFile = new File(generated + File.separator + removeFileExtension(selectedFile.getName().toString()) + SPEC_EXT);
		
		if (!specFile.exists()) {
			ConsoleService.getInstance().printErrorMessage("Any annotations could be found in the source file.");
			throw new NullPointerException();
		}

		return specFile;
	}
	
	private String removeFileExtension(final String fileName) {
		int dot = fileName.lastIndexOf('.');
		return fileName.substring(0, dot);
	}

}
