package com.jstar.eclipse.objects;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class VerificationError {
	
	public static final String JSTAR_ERROR_MARKER = "jstar.verification.marker";
	
	private int startLine;
	
	private int endLine;
	
	private int startPos;
	
	private int endPos;
	
	private String error_message;
	
	public VerificationError(final JSONObject json) throws JSONException {
		final JSONObject error_pos = json.getJSONObject("error_pos");
		startLine = error_pos.getInt("sline");
		endLine = error_pos.getInt("eline");
		startPos = error_pos.getInt("spos");
		endPos = error_pos.getInt("epos");
		error_message = json.getString("error_text");
		
		final String counter_example = json.getString("counter_example");
		if (StringUtils.isNotEmpty(counter_example)) {
			error_message += "\n" + counter_example;
		}
	}

	public void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public int getStartLine() {
		return startLine;
	}

	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	public int getEndLine() {
		return endLine;
	}

	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}

	public int getStartPos() {
		return startPos;
	}

	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}

	public int getEndPos() {
		return endPos;
	}

	public void setError_message(String error_message) {
		this.error_message = error_message;
	}

	public String getError_message() {
		return error_message;
	}
}
