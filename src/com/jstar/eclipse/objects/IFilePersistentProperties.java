package com.jstar.eclipse.objects;

import com.jstar.eclipse.services.JStar.PrintMode;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

public class IFilePersistentProperties {
	
	private static final QualifiedName MODE = new QualifiedName("JSTAR_VERIFICATION", "MODE");
	private static final QualifiedName SPEC_IN_SOURCE_FILE = new QualifiedName("JSTAR_VERIFICATION", "SPEC_IN_SOURCE_FILE");
	private static final QualifiedName SPEC_FILE = new QualifiedName("JSTAR_VERIFICATION", "SPEC_FILE");
	private static final QualifiedName LOGIC_FILE = new QualifiedName("JSTAR_VERIFICATION", "LOGIC_FILE");
	private static final QualifiedName ABS_FILE = new QualifiedName("JSTAR_VERIFICATION", "ABS_FILE");
	
	public static String getSpecFile(final IFile file) {
		final String specFile = getProperty(file, SPEC_FILE);
		
		return StringUtils.isEmpty(specFile) ? "" : specFile;
	}
	
	public static void setSpecFile(final IFile file, String specFile) {
		setProperty(file, SPEC_FILE, specFile);
	}
	
	public static String getLogicFile(final IFile file) {
		final String logicFile = getProperty(file, LOGIC_FILE);
		
		return StringUtils.isEmpty(logicFile) ? "" : logicFile;
	}
	
	public static void setLogicFile(final IFile file, String logicFile) {
		setProperty(file, LOGIC_FILE, logicFile);
	}
	
	public static String getAbsFile(final IFile file) {
		final String absFile = getProperty(file, ABS_FILE);
		
		return StringUtils.isEmpty(absFile) ? "" : absFile;
	}
	
	public static void setAbsFile(final IFile file, String absFile) {
		setProperty(file, ABS_FILE, absFile);
	}
	
	public static boolean isSpecInSourceFile(final IFile file) {
		if ("false".equalsIgnoreCase(getProperty(file, SPEC_IN_SOURCE_FILE))) {
			return false;
		}
		
		return true;
	}
	
	public static void setSpecInSourceFile(final IFile file, boolean specInSourceFile) {
		setProperty(file, SPEC_IN_SOURCE_FILE, String.valueOf(specInSourceFile));
	}
	
	public static PrintMode getMode(final IFile file) {
	    PrintMode printMode = null;
	    try {
			printMode = PrintMode.valueOf(getProperty(file, MODE));
		} catch (Exception e) {
			// Default value "-q"
			
			return PrintMode.QUIET;
		}
		
		return printMode;
	}
	
	public static void setMode(final IFile file, final PrintMode mode) {
		setProperty(file, MODE, mode.toString());
	}
	
	private static void setProperty(final IFile file, final QualifiedName name, final String value) {
		try {
			file.setPersistentProperty(name, value);
		} 
		catch (CoreException ce) {
		}
	}
	
	private static String getProperty(final IFile file, final QualifiedName name) {
		try {
			return file.getPersistentProperty(name);
		} catch (CoreException e) {
			return null;
		}
	}

}
