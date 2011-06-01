/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.processing.annotations.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class SpecObject extends AnnotationObject {	
	private String pre;
	private String post;
	private String methodDeclaration;
	private Map<String, String> exceptionPosts;
	private boolean stat = false;
	
	public SpecObject(long startPos, long endPos, String fileName) {
		this.startPos = startPos;
		this.endPos = endPos;
		this.fileName = fileName;
	}
	
	public void setPre(String pre) {
		this.pre = pre;
	}

	public void setPost(String post) {
		this.post = post;
	}

	@Override
	public void generateFile(Writer writer) throws IOException {
		writer.write("   " + methodDeclaration + appendStatic() + " :\n");
		generateFileForList(writer);
		appendPosition(writer);
	}
	
	public void generateFileForList(Writer writer) throws IOException {
		writer.write("      { " + pre + " }\n");
		writer.write("      { " + post + " }\n");
		
		for (final String name : exceptionPosts.keySet()) {
			writer.write("      { " + name + " : " + exceptionPosts.get(name) + " }\n");
		}
	}

	public void setMethodDeclaration(String methodDeclaration) {
		this.methodDeclaration = methodDeclaration;
	}

	public String getMethodDeclaration() {
		return methodDeclaration;
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

	public void setExceptionPosts(Map<String, String> exceptionPosts) {
		this.exceptionPosts = exceptionPosts;
	}

}
