package com.jstar.eclipse.objects;

public enum InputFileKind {
	
	SPEC("spec"),
	LOGIC("logic"),
	ABS("abs");
	
	private String extension;
	
	InputFileKind(final String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}
}
