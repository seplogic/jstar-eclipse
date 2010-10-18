/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.jstar.eclipse.exceptions.FolderNotFoundException;
import com.jstar.eclipse.exceptions.InputFileNotFoundException;
import com.jstar.eclipse.services.ConsoleService;
import com.jstar.eclipse.services.JStar.PrintMode;

public class JavaFile {	
	private static final String IMPORTS = "imports";
	private IFile file;
	private JavaProject javaProject;

	public JavaFile(final IFile file) {
		this.file = file;
	}
	
	public JavaProject getJavaProject() {
		if (javaProject == null) {
			return new JavaProject(getCompilationUnit().getJavaProject());
		}
		
		return javaProject;
	}
	
	public IFolder getOutputDirectory() {
		final IFolder jStarRootFolder = getJavaProject().getJStarRootFolder();	
		final IFolder outputFolder = jStarRootFolder.getFolder(getOutputRelativePath());
		
		if (!outputFolder.exists()) {
			throw new FolderNotFoundException(outputFolder);
		}
		
		return outputFolder;
	}
	
	//TODO: change generated directory not to be a resource in workspace
	public IFolder getGeneratedDir() {
		final IFolder folder = getJavaProject().getGeneratedDir().getFolder(getOutputRelativePath());
		
		if (!folder.exists()) {
			throw new FolderNotFoundException(folder);
		}
		
		return folder;
	}
	
	public IFile getGeneratedSpec() {
		return getGeneratedDir().getFile(new Path(getNameWithoutExtension()).addFileExtension(InputFileKind.SPEC.getExtension()));
	}
	
	public IFile getGeneratedImports() {
		return getGeneratedDir().getFile(new Path(getNameWithoutExtension()).addFileExtension(IMPORTS));
	}

	public IFile getFile() {
		return file;
	}

	public String getName() {
		return file.getName();
	}
	
	public String getNameWithPackage() {
		return getPackage() + getNameWithoutExtension();	
	}
	
	public String getNameWithoutExtension() {
		return file.getFullPath().removeFileExtension().lastSegment();
	}
	
	public String getPackage() {
		try {
			IPackageDeclaration[] packages = getCompilationUnit().getPackageDeclarations();
			
			if (packages.length == 0) {
				return "";
			}
			else {
				ConsoleService.getInstance().printErrorMessage("Currently jStar does not support packages");
			}
			
			//ICompilationUnit.getPackageDeclaration documentation: There normally is at most one package declaration.
			final IPackageDeclaration filePackage = packages[0];
			
			return filePackage.getElementName() + ".";
		}
		catch (JavaModelException jme) {
			ConsoleService.getInstance().printErrorMessage("Cannot obtain the package declaration from the source file.");
			throw new RuntimeException();
		}
	}
	
	public ICompilationUnit getCompilationUnit() {
		return JavaCore.createCompilationUnitFrom(file);
	}
	
	public List<String> getTypes() {
		final List<String> types = new LinkedList<String>();
		
		try {
			for (IType type : getCompilationUnit().getAllTypes()) {
				types.add(getPackage() + type.getElementName());
			}
			
			return types;
		} 
		catch (JavaModelException e) {
			ConsoleService.getInstance().printErrorMessage("Cannot obtain types from the source file.");
			throw new RuntimeException();
		}
	}
	
	public String getProjectClasspath() {
		StringBuilder projectClassPath = new StringBuilder(); 
		
		try {			
			for (IClasspathEntry entry : getJavaProject().getProject().getResolvedClasspath(true)) {
				final int entryKind = entry.getEntryKind();
				if (entryKind == IClasspathEntry.CPE_SOURCE) {								
					projectClassPath.append(getJavaProject().getWorkspaceLocation().append(entry.getPath()).toOSString());
					projectClassPath.append(File.pathSeparator);
				}
				
				if (entryKind == IClasspathEntry.CPE_LIBRARY) {
					projectClassPath.append(getAbsolutePath(entry));
					projectClassPath.append(File.pathSeparator);
					
				}
				
				//TODO: IClasspathEntry.CPE_PROJECT
			}
			
			return projectClassPath.toString();
		} catch (CoreException ce) {
			ce.printStackTrace(ConsoleService.getInstance().getConsoleStream());
			return "";
		}
	}
	
	public String getAbsolutePath() {
		return file.getLocation().toOSString();
	}
	
	public void clearMarkers() throws CoreException {
		IMarker[] problems = file.findMarkers(
				VerificationError.JSTAR_ERROR_MARKER, 
				true,
				IResource.DEPTH_INFINITE
		);
		
		for (IMarker problem : problems) {
			problem.delete();
		}
	}

	public IMarker createMarker(String jstarErrorMarker) throws CoreException {
		return file.createMarker(jstarErrorMarker);
	}
	
	public ErrorPosition getErrorPosition(int startLine, int endLine, int startSymbol, int endSymbol) {
		BufferedReader input;
		String line = null;
		int lineNumber = 1;
		int start = 0;
		int end = 0;
		
		if (startLine == -1 || endLine == -1 || startSymbol == -1 || endSymbol == -1) {
			try {
				input = new BufferedReader(new InputStreamReader(file.getContents()));
				line = input.readLine();
				
				return new ErrorPosition(0, line.length());				
			} catch (CoreException ce) {
				ce.printStackTrace(ConsoleService.getInstance().getConsoleStream());
			} catch (IOException ioe) {
				ioe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
			}
			
			return new ErrorPosition(0, 0);			
		}

		try {
			char character;
			char nextCharacter = ' ';
			InputStreamReader inputStreamReader = new InputStreamReader(file.getContents());	
			
			character = (char)inputStreamReader.read();
			while (inputStreamReader.ready()) {
				
				if (lineNumber < startLine) {
					start += 1;
					end += 1;
				} 
				else if (lineNumber < endLine) {
					end += 1;
				}
				else {
					break;
				}
				
				nextCharacter = (char)inputStreamReader.read();
				if (character == '\n' || (character == '\r' && nextCharacter != '\n') ) {
					lineNumber++;
				}
				
				character = nextCharacter;
			}

			start += startSymbol - 1;
			end += endSymbol;

			return new ErrorPosition(start, end);

		} catch (CoreException ce) {
			ce.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		} catch (IOException ioe) {
			ioe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		}

		return new ErrorPosition(0, 0);
	}
	
	public IFile getInputFile(final InputFileKind inputFile) throws InputFileNotFoundException {
		final IFolder jStarRootFolder = getOutputDirectory();	
		final IFile file = jStarRootFolder.getFile(new Path(getNameWithoutExtension()).addFileExtension(inputFile.getExtension()));
		
		if (!file.exists()) {
			throw new InputFileNotFoundException(file);
		}
		
		return file;
	}
	
	public IFile getSpecFile() throws InputFileNotFoundException {
		return getInputFile(InputFileKind.SPEC);
	}
	
	public IFile getLogicFile() throws InputFileNotFoundException {
		return getInputFile(InputFileKind.LOGIC);	
	}
	
	public IFile getAbsFile() throws InputFileNotFoundException {
		return getInputFile(InputFileKind.ABS);	
	}
	
	public boolean isSpecInSource() {
		return JavaFilePersistentProperties.isSpecInSourceFile(this);
	}
	
	public void setSpecInSource(final boolean specInSource) {
		JavaFilePersistentProperties.setSpecInSourceFile(this, specInSource);
	}
	
	public PrintMode getMode() {
		return JavaFilePersistentProperties.getMode(this);
	}
	
	public void setMode(final PrintMode mode) {
		JavaFilePersistentProperties.setMode(this, mode);
	}
	
	private String getAbsolutePath(IClasspathEntry entry) {
		IPath entryPath = entry.getPath();
		IPackageFragmentRoot lib = null;
		
		try {
			lib = getJavaProject().getProject().findPackageFragmentRoot(entryPath);
		} 
		catch (JavaModelException jme) {
		}
		
		if (lib == null) {
			return entryPath.toOSString();
		}
		
		if (lib.getResource() == null) {
			return entryPath.toOSString();
		}
		else {
			return lib.getResource().getLocation().toOSString();
		}

	}
	
	private IPath getOutputRelativePath() {
		final String[] packages = this.getNameWithPackage().split("\\.");
		IPath path = new Path("");
		
		for (final String part : packages) {
			path = path.append(new Path(part));
		}
		
		return path.removeLastSegments(1).makeRelative();
	}
}
