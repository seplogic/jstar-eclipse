package com.jstar.eclipse.objects;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;

import com.jstar.eclipse.exceptions.FolderNotFoundException;
import com.jstar.eclipse.exceptions.NoJStarRootFolderException;

public class JavaProject {
	
	public static final String GENERATED = "generated";
	private IJavaProject project;
	
	public JavaProject (final IJavaProject project) {
		this.project = project;
	}
	
	public IPath getWorkspaceLocation() {
		return project.getJavaModel().getResource().getLocation();
	}
	
	public IJavaProject getProject() {
		return project;
	}
	
	public IFolder getJStarRootFolder() {
		final String relativePath = JavaFilePersistentProperties.getJStarRootFolder(this);
		final IFolder folder = project.getProject().getFolder(relativePath);
		
		if (!folder.exists()) {
			throw new NoJStarRootFolderException();
		}

		return folder;
	}
	
	public void setJStarRootFolder(final IFolder jStarFolder) {
		JavaFilePersistentProperties.setJStarRootFolder(this, jStarFolder.getProjectRelativePath().toOSString());
	}
	
	public IFolder getGeneratedDir() {
		final IFolder jStarRootFolder = getJStarRootFolder();	
		final IFolder generated = jStarRootFolder.getFolder(GENERATED);
		
		if (!generated.exists()) {
			throw new FolderNotFoundException(generated);
		}
		
		return generated;
	}
}
