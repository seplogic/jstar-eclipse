/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.objects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstar.eclipse.services.ConsoleService;

public class StreamThread extends Thread {
	private static final String JSTAR_LINE_PREFIX = "json";	
	private InputStream inputStream;
	private List<VerificationError> errors;

	public StreamThread(final InputStream inputStream, final List<VerificationError> errors) {
		this.inputStream = inputStream;
		this.errors = errors;
	}

	public void run() {
		try {
			final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			final BufferedReader input = new BufferedReader(inputStreamReader);
			String line = null;
			
			final StringBuilder linesToPrint = new StringBuilder();
			
			while ((line = input.readLine()) != null) {							
				if (line.startsWith(JSTAR_LINE_PREFIX)) {
					errors.add(new VerificationError(new JSONObject(line.substring(5))));
				} 
				else {
					linesToPrint.append(line).append('\n');
				}
			}
			
			Display.getDefault().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					ConsoleService.getInstance().printLine(linesToPrint.toString());
					
				}
			});
							
		}
		catch (IOException ioe) {
			ioe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		} 
		catch (JSONException jsone) {
			jsone.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		}
	}
}
