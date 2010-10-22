/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.objects;

import com.jstar.eclipse.exceptions.NoJStarRootFolderException;
import com.jstar.eclipse.services.ConsoleService;
import com.jstar.eclipse.services.JStar.PrintMode;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

//TODO: Rename
public class JavaFilePersistentProperties {
	
	private static final QualifiedName MODE = new QualifiedName("JSTAR_VERIFICATION", "MODE");
	private static final QualifiedName SPEC_IN_SOURCE_FILE = new QualifiedName("JSTAR_VERIFICATION", "SPEC_IN_SOURCE_FILE");
	private static final QualifiedName JSTAR_ROOT_FOLDER = new QualifiedName("JSTAR_VERIFICATION", "JSTAR_ROOT_FOLDER");
	
	public static String getJStarRootFolder(final JavaProject project) {
		final String jStarRootFolder = getProperty(project.getProject().getResource(), JSTAR_ROOT_FOLDER);	
		
		if (StringUtils.isBlank(jStarRootFolder)) {
			throw new NoJStarRootFolderException();
		}
		
		return jStarRootFolder;
	}
	
	public static void setJStarRootFolder(final JavaProject project, String jStarFolderRoot) {
		setProperty(project.getProject().getResource(), JSTAR_ROOT_FOLDER, jStarFolderRoot);
	}
	
	public static boolean isSpecInSourceFile(final JavaFile file) {
		return isSpecInSourceFile(file.getFile());
	}
	
	public static boolean isSpecInSourceFile(final IResource resource) {
		if ("false".equalsIgnoreCase(getProperty(resource, SPEC_IN_SOURCE_FILE))) {
			return false;
		}
		
		return true;
	}
	
	public static void setSpecInSourceFile(final JavaFile file, boolean specInSourceFile) {
		setProperty(file.getFile(), SPEC_IN_SOURCE_FILE, String.valueOf(specInSourceFile));
	}
	
	public static PrintMode getMode(final JavaFile file) {
	    PrintMode printMode = null;
	    
	    try {
			printMode = PrintMode.valueOf(getProperty(file.getFile(), MODE));
		} 
	    catch (Exception e) {
			// Default value "-q"
			
			return PrintMode.QUIET;
		}
		
		return printMode;
	}
	
	public static void setMode(final JavaFile file, final PrintMode mode) {
		setProperty(file.getFile(), MODE, mode.toString());
	}
	
	private static void setProperty(final IResource resource, final QualifiedName name, final String value) {
		try {
			resource.setPersistentProperty(name, value);
		} 
		catch (CoreException ce) {
			ConsoleService.getInstance().printErrorMessage("Could not save property " + name.getLocalName() + " with value " + value + " of file " + resource.getName() + ".");
		}
	}
	
	private static String getProperty(final IResource resource, final QualifiedName name) {
		try {
			return resource.getPersistentProperty(name);
		} 
		catch (CoreException ce) {
			return null;
		}
	}

}
