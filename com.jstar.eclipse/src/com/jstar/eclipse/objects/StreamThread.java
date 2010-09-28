package com.jstar.eclipse.objects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.ui.console.MessageConsoleStream;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstar.eclipse.services.ConsoleService;

public class StreamThread extends Thread {
	private static final String JSTAR_LINE_PREFIX = "json";	
	private InputStream inputStream;
	private List<VerificationError> errors;
	private MessageConsoleStream out;

	public StreamThread(final InputStream inputStream, final MessageConsoleStream out, final List<VerificationError> errors) {
		this.inputStream = inputStream;
		this.errors = errors;
		this.out = out;
	}

	public void run() {
		try {
			final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			final BufferedReader input = new BufferedReader(inputStreamReader);
			String line = null;
			
			while ((line = input.readLine()) != null) {							
				if (line.startsWith(JSTAR_LINE_PREFIX)) {
					errors.add(new VerificationError(new JSONObject(line.substring(5))));
				} 
				else {
					out = ConsoleService.getInstance().printLine(line, out);
				}
			}
		}
		catch (IOException ioe) {
			ioe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		} 
		catch (JSONException jsone) {
			jsone.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		}
	}
}
