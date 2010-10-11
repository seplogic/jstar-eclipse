package com.jstar.eclipse.exceptions;

import com.jstar.eclipse.objects.InputFileKind;

public class InputFileNotFoundException extends Exception {
	
	private static final long serialVersionUID = 2332313556877153466L;
	
	private InputFileKind inputFile;
	
	public InputFileNotFoundException(InputFileKind inputFile) {
		super();
		this.inputFile = inputFile;
	}

	public InputFileKind getInputFile() {
		return inputFile;
	}

}
