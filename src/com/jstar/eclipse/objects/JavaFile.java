package com.jstar.eclipse.objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.jstar.eclipse.services.ConsoleService;

public class JavaFile {
	
	private static final String GENERATED = "generated";
	public static final String INPUT_FILES = "input_files";
	
	private IFile file;

	public JavaFile(final IFile file) {
		this.file = file;
	}

	public IFile getFile() {
		return file;
	}

	public String getPersistentProperty(QualifiedName name) throws CoreException {
		return file.getPersistentProperty(name);
	}

	public void setPersistentProperty(QualifiedName name, String value) throws CoreException {
		file.setPersistentProperty(name, value);	
	}

	public String getName() {
		return file.getName();
	}
	
	public ICompilationUnit getCompilationUnit() {
		return JavaCore.createCompilationUnitFrom(file);
	}
	
	public String getNameWithPackage() {
		return getPackage() + file.getFullPath().removeFileExtension().lastSegment();	
	}
	
	public String getPackage() {
		try {
			IPackageDeclaration[] packages = getCompilationUnit().getPackageDeclarations();
			
			if (packages.length == 0) {
				return "";
			}
			
			// ICompilationUnit.getPackageDeclaration documentation: There normally is at most one package declaration.
			//final IPackageDeclaration filePackage = packages[0];
			
			//return filePackage.getElementName() + ".";
	
			ConsoleService.getInstance().printErrorMessage("Currently jStar does not support packages");
			throw new IllegalArgumentException();
		}
		catch (JavaModelException jme) {
			ConsoleService.getInstance().printErrorMessage("Cannot obtain the package declaration from the source file.");
			throw new RuntimeException();
		}
	}
	
	public IPackageFragmentRoot getPackageFragmentRoot() {
		return (IPackageFragmentRoot) getCompilationUnit().getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
	}
	
	public IPath getPackageFragmentRootLocation() {
		return getPackageFragmentRoot().getResource().getLocation();
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
	
	public IJavaProject getJavaProject() {
		return getCompilationUnit().getJavaProject();
	}
	
	public IPath getWorkspaceLocation() {
		return getJavaProject().getJavaModel().getResource().getLocation();
	}
	
	 private String getAbsolutePath(IClasspathEntry entry) {
		IPath entryPath = entry.getPath();
		IPackageFragmentRoot lib = null;
		
		try {
			lib = getJavaProject().findPackageFragmentRoot(entryPath);
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
	
	public String getProjectClasspath() {
		StringBuilder projectClassPath = new StringBuilder(); 
		
		try {			
			for (IClasspathEntry entry : getJavaProject().getResolvedClasspath(true)) {
				final int entryKind = entry.getEntryKind();
				if (entryKind == IClasspathEntry.CPE_SOURCE) {								
					projectClassPath.append(getWorkspaceLocation().append(entry.getPath()).toOSString());
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
	
	public IPath getDirectoryPath() {
		return file.getLocation().removeLastSegments(1);
	}
	
	public String makeGeneratedDir() {
		final IPath fileDirectoryPath = getDirectoryPath();	
		final IPath inputFilesPath = fileDirectoryPath.append(INPUT_FILES);
		final File inputFiles = inputFilesPath.toFile();
	
		if (!inputFiles.exists() || !inputFiles.isDirectory()) {
			inputFiles.mkdir();
		}
		
		final File generated = inputFilesPath.append(GENERATED).toFile();
		
		if (!generated.exists() || !generated.isDirectory()) {
			generated.mkdir();
		}
		
		return generated.getAbsolutePath();
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

}
