package com.jstar.eclipse.objects;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;

import com.jstar.eclipse.exceptions.NoJStarRootFolderException;

public class JavaProject {
	
	IJavaProject project;
	
	public JavaProject (IJavaProject project) {
		this.project = project;
	}
	
	public IPath getWorkspaceLocation() {
		return project.getJavaModel().getResource().getLocation();
	}
	
	public IJavaProject getProject() {
		return project;
	}
	
	public IFolder getJStarRootFolder() throws NoJStarRootFolderException {
		final String relativePath = JavaFilePersistentProperties.getJStarRootFolder(this);
		
		final IFolder folder = project.getProject().getFolder(relativePath);
		
		if (!folder.exists()) {
			throw new NoJStarRootFolderException();
		}

		return folder;
	}
	
	public void setJStarRootFolder(IFolder jStarFolder) {
		JavaFilePersistentProperties.setJStarRootFolder(this, jStarFolder.getProjectRelativePath().toOSString());
	}
}
