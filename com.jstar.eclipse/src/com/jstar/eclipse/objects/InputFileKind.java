package com.jstar.eclipse.objects;

import com.jstar.eclipse.exceptions.SpecNotFoundException;
import com.jstar.eclipse.exceptions.LogicNotFoundException;
import com.jstar.eclipse.exceptions.AbsNotFoundException;

public enum InputFile {
	
	SPEC("spec"),
	LOGIC("logic"),
	ABS("abs");
	
	private String extension;
	
	InputFile(final String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}
	
	public Exception getNotFoundException() {
		switch (this) { 
			case SPEC : return new SpecNotFoundException();
			case LOGIC : return new LogicNotFoundException();
			case ABS : return new AbsNotFoundException();
		}
		
		return null;
	}

}
