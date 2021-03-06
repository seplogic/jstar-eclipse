/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.services;

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.json.JSONException;

import com.jstar.eclipse.objects.ErrorPosition;
import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.objects.VerificationError;
import com.jstar.eclipse.objects.StreamThread;

public class ConsoleService {

	private static ConsoleService instance;

	private static final String CONSOLE = "jStar";
	
	private MessageConsole console;

	private ConsoleService() {
	}

	public static ConsoleService getInstance() {
		if (instance == null) {
			instance = new ConsoleService();
		}
		return instance;
	}
	
	public PrintStream getConsoleStream() {
		MessageConsole myConsole = getConsole();
		MessageConsoleStream out = myConsole.newMessageStream();
		return new PrintStream(out);
	}

	public void printToConsole(String lines) {
		MessageConsole myConsole = findConsole(CONSOLE);
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println(lines);
		showConsole();
	}
	
	public void printErrorMessage(String errorMessage) {
		MessageConsole myConsole = getConsole();
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println("An error occurred: " + errorMessage);
		showConsole();
	}
	
	public void printToConsole(JavaFile selectedFile, Process pr) throws IOException, JSONException, InterruptedException, CoreException {
		MessageConsole myConsole = getConsole();
		MessageConsoleStream out = myConsole.newMessageStream();
		List<VerificationError> errors = new LinkedList<VerificationError>();
		
		StreamThread errorGobbler = new StreamThread(pr.getErrorStream(), errors);            
	    StreamThread outputGobbler = new StreamThread(pr.getInputStream(), errors);
	        
	    errorGobbler.start();
	    outputGobbler.start();
	    
	    errorGobbler.join();
	    outputGobbler.join();

		int exitVal = pr.waitFor();
		
		if (exitVal != 0) {
			out.println("Exited with error code " + exitVal);
		}

		for (VerificationError error : errors) {
			if (error != null) {
				IMarker marker = selectedFile.createMarker(VerificationError.JSTAR_ERROR_MARKER);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);

				final ErrorPosition errorOffset = selectedFile.getErrorPosition(
						error.getStartLine(), 
						error.getEndLine(), 
						error.getStartPos(), 
						error.getEndPos()
				);

				marker.setAttribute(IMarker.CHAR_START, errorOffset.getStartPos());
				marker.setAttribute(IMarker.CHAR_END, errorOffset.getEndPos());
				marker.setAttribute(IMarker.MESSAGE, error.getError_message());
			}
		}
		
		showConsole();
	}
	
	public MessageConsole getConsole() {
		if (console == null) {
    		console = findConsole(CONSOLE);
    	}
		return console;
	}
	
	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();

		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		}

		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}
	
	private void showConsole() {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		conMan.showConsoleView(getConsole());
	}

	private class ColourIndexPair {
		private Colour colour;
		private int index;

		public ColourIndexPair(Colour colour, int index) {
			this.colour = colour;
			this.index = index;
		}
	
		public Colour getColour() {
			return colour;
		}

		public int getIndex() {
			return index;
		}
	}
	
	private boolean minIndex(int index, int index1, int index2) {
		return index != -1 && ((index < index1 && index1 != -1) || index1 == -1) && ((index < index2 && index2 != -1) || index2 == -1); 
	}
	
	private ColourIndexPair findNextColour(String line) {
		final String red = (char) 0x1B + "[1;31m";
		final String green = (char) 0x1B + "[1;32m";
		final String black = (char) 0x1B + "[0m";
		
		int redIndex = line.indexOf(red);
		int greenIndex = line.indexOf(green);
		int blackIndex = line.indexOf(black);
		

		if (minIndex(redIndex, greenIndex, blackIndex)) {
			return new ColourIndexPair(Colour.RED, redIndex);
		}
		
		if (minIndex(greenIndex, redIndex, blackIndex)) {
			return new ColourIndexPair(Colour.GREEN, greenIndex);
		}
		
		if (minIndex(blackIndex, greenIndex, redIndex)) {
			return new ColourIndexPair(Colour.BLACK, blackIndex);
		}
		
		return new ColourIndexPair(null, -1);
	}
	
	public void printLine(String lines) {
		
		MessageConsoleStream out = getConsole().newMessageStream();
		
		while (StringUtils.isNotEmpty(lines)) {
			final ColourIndexPair colourIndex = findNextColour(lines);
			int index = colourIndex.getIndex();
			Colour colour = colourIndex.getColour();
			
			if (index == -1) {
				out.print(lines);
				lines = "";
			}
			else {
				out.print(lines.substring(0, index));
				lines = lines.substring(index + colour.getCmdSymbolCount());
				out = out.getConsole().newMessageStream();
				out.setColor(new Color(null, colour.getRGB()));
			}
		}
	}

	private enum Colour {
		RED(255, 0, 0, 7), GREEN(0, 255, 0, 7), BLACK(0, 0, 0, 4);

		private int red;
		private int green;
		private int blue;

		private int cmdSymbolCount;

		Colour(int red, int green, int blue, int count) {
			this.red = red;
			this.green = green;
			this.blue = blue;
			cmdSymbolCount = count;
		}

		public RGB getRGB() {
			return new RGB(red, green, blue);
		}

		public int getCmdSymbolCount() {
			return cmdSymbolCount;
		}
	}
}
