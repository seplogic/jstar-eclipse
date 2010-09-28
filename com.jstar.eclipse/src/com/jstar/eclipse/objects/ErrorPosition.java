/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.objects;

public class ErrorPosition {
	private int startPos;
	private int endPos;

	public ErrorPosition(int start, int end) {
		startPos = start;
		endPos = end;
	}

	public int getEndPos() {
		return endPos;
	}

	public int getStartPos() {
		return startPos;
	}
}
