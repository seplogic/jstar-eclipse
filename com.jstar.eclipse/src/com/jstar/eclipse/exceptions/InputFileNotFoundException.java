package com.jstar.eclipse.exceptions;

import org.eclipse.core.resources.IFile;

public class InputFileNotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 2332313556877153466L;
	
	private IFile inputFile;
	
	public InputFileNotFoundException(IFile inputFile) {
		super();
		this.inputFile = inputFile;
	}

	public IFile getInputFile() {
		return inputFile;
	}

}
