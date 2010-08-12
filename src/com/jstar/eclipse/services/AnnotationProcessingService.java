package com.jstar.eclipse.services;

import java.io.File;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.core.resources.IFile;

import com.jstar.eclipse.preferences.PreferenceConstants;

public class AnnotationProcessingService {
	
	public static final String INPUT_FILES = "input_files";
	public final static String SPEC_EXT = ".spec";
	private static final String GENERATED = "generated";
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
	
	public File processAnnotations(IFile selectedFile) {
		
		final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
		
		final String generated = makeGeneratedDir(selectedFile);
		
		String[] arguments = {
				"-proc:only", 
				"-d", generated, 
				"-cp", PreferenceConstants.getAnnotationsPath() + ';' + PreferenceConstants.getAnnotationProcessorPath(),
				"-processor", PROCESSOR,
				selectedFile.getLocation().toOSString()
		};
		
		int exitValue = javac.run(null, null, null, arguments);
		
		if (exitValue != 0) {
			throw new RuntimeException("An error occurred while processing annotations");
		}

		return new File(generated + File.separator + removeFileExtension(selectedFile.getName().toString()) + SPEC_EXT);
	}
	
	private String makeGeneratedDir(IFile selectedFile) {
		final File fileLocation = new File(selectedFile.getLocation().toString()).getParentFile();
		
		final String inputFilesLocationString = fileLocation.getAbsolutePath() + File.separator + INPUT_FILES;
		final File inputFilesLocation = new File(inputFilesLocationString);
		
		if (!inputFilesLocation.exists() || !inputFilesLocation.isDirectory()) {
			inputFilesLocation.mkdir();
		}
		
		final String generatedLocationString = inputFilesLocationString + File.separator + GENERATED;
		final File generatedLocation = new File(generatedLocationString);
		
		if (!generatedLocation.exists() || !generatedLocation.isDirectory()) {
			generatedLocation.mkdir();
		}
		
		return generatedLocationString;
	}
	
	public String removeFileExtension(final String fileName) {
		int dot = fileName.lastIndexOf('.');
		return fileName.substring(0, dot);
	}

}
