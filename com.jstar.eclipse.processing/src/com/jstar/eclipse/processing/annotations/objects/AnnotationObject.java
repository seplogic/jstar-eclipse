/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.processing.annotations.objects;

import java.io.IOException;
import java.io.Writer;

public abstract class AnnotationObject {

	protected long startPos;
	protected long endPos;
	protected String fileName;

	public long getStartPos() {
		return startPos;
	}

	public void setStartPos(long startPos) {
		this.startPos = startPos;
	}

	public long getEndPos() {
		return endPos;
	}

	public void setEndPos(long endPos) {
		this.endPos = endPos;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	abstract public void generateFile(Writer writer) throws IOException;

	protected void appendPosition(Writer writer) throws IOException {
		writer.write(new StringBuilder("      /*Source Line Pos Tag: sline: 0 eline: 0 spos: ")
		.append(startPos).append(" epos: ").append(endPos).append(" file: ").append(fileName).append(".java*/\n").toString());
	}
}