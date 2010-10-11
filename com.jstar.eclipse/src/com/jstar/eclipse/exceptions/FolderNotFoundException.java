package com.jstar.eclipse.exceptions;

import org.eclipse.core.resources.IFolder;

public class FolderNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 7493390179026292643L;
	
	private IFolder folder;
	
	public FolderNotFoundException(final IFolder folder) {
		super();
		this.folder = folder;
	}

	public IFolder getFolder() {
		return folder;
	}
	

}
