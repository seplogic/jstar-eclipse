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

import org.apache.commons.lang.StringUtils;

public class ImportObject extends AnnotationObject {
	
	private List<String> specFiles;
	
	public ImportObject(long startPos, long endPos, String fileName) {
		this.startPos = startPos;
		this.endPos = endPos;
		this.fileName = fileName;
		specFiles = new LinkedList<String>();
	}

	public void addSpecFile(String specFile) {
		this.specFiles.add(specFile);
	}

	@Override
	public void generateFile(Writer writer) throws IOException {
		for (final String specFile : specFiles) {
			writer.write("import \"" + StringUtils.replace(specFile, ".", "/") + ".spec\";\n");
		}
	}
	
	public void writeImports(final Writer writer) throws IOException {
		for (final String specFile : specFiles) {
			writer.write(specFile + "\n");
		}
	}


	
}
