/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.processing.annotations.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

public class SpecObjectList extends AnnotationObject {
	
	private List<SpecObject> specObjects;
	private String methodDeclaration;
	private boolean stat;
	
	public SpecObjectList(long startPos, long endPos, String fileName) {
		this.startPos = startPos;
		this.endPos = endPos;
		this.fileName = fileName;
		this.specObjects = new LinkedList<SpecObject>();
	}

	@Override
	public void generateFile(Writer writer) throws IOException {
		writer.write("   " + methodDeclaration + appendStatic() + " :\n");
		boolean first = true;
		
		for (SpecObject specObject : specObjects) {
			if (!first) {
				writer.write("      andalso\n");
			}
			
	        first = false;
			specObject.generateFileForList(writer);		
		}
		
		appendPosition(writer);
	}

	public void addSpecObjects(SpecObject specObject) {
		specObjects.add(specObject);
	}

	public void setMethodDeclaration(String methodDeclaration) {
		this.methodDeclaration = methodDeclaration;
	}

	public void setStat(boolean stat) {
		this.stat = stat;
	}
	
	private String appendStatic() {
		if (stat) {
			return " static";
		}
		
		return "";
	}

}
