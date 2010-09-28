/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.processing.annotations.objects;

import java.io.IOException;
import java.io.Writer;

import com.jstar.eclipse.annotations.DefinitionType;

public class PredicateObject extends AnnotationObject {
	
	private String predicate;
	
	private String formula;
	
	private DefinitionType type;
	
	public PredicateObject(long startPos, long endPos, String fileName) {
		this.startPos = startPos;
		this.endPos = endPos;
		this.fileName = fileName;
	}

	@Override
	public void generateFile(Writer writer) throws IOException {
		writer.write("   " + type.toString().toLowerCase() + " " + predicate + " as " + formula + ";\n");
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public void setType(DefinitionType type) {
		this.type = type;
	}

}
