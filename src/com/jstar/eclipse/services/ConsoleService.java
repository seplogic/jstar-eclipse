package com.jstar.eclipse.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.json.JSONException;
import org.json.JSONObject;

import com.jstar.eclipse.objects.VerificationError;

public class ConsoleService {

	private static ConsoleService instance;

	private static final String CONSOLE = "Console";
	private static final String JSTAR_LINE_PREFIX = "json";

	private ConsoleService() {
	}

	public static ConsoleService getInstance() {
		if (instance == null) {
			instance = new ConsoleService();
		}
		return instance;
	}
	
	public PrintStream getConsoleStream() {
		MessageConsole myConsole = findConsole(CONSOLE);
		MessageConsoleStream out = myConsole.newMessageStream();
		return new PrintStream(out);
	}

	public void printToConsole(String lines) {
		MessageConsole myConsole = findConsole(CONSOLE);
		MessageConsoleStream out = myConsole.newMessageStream();
		out.println(lines);
		showConsole();
	}
	
	public void clearConsole() {
		MessageConsole myConsole = findConsole(CONSOLE);
		myConsole.clearConsole();
	}
	
	public void clearMarkers(IFile selectedFile) throws CoreException {
		IMarker[] problems = selectedFile.findMarkers(
				VerificationError.JSTAR_ERROR_MARKER, 
				true,
				IResource.DEPTH_INFINITE
		);
		
		for (IMarker problem : problems) {
			problem.delete();
		}
	}
	
	public void printToConsole(IFile selectedFile, Process pr) throws IOException, JSONException, InterruptedException, CoreException {
		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

		String line = null;

		MessageConsole myConsole = findConsole(CONSOLE);
		MessageConsoleStream out = myConsole.newMessageStream();

		List<VerificationError> errors = new LinkedList<VerificationError>();
		while ((line = input.readLine()) != null) {
			if (line.startsWith(JSTAR_LINE_PREFIX)) {
				errors.add(new VerificationError(new JSONObject(line
						.substring(5))));
				System.out.println(line);
			} else {
				out = printLine(line, out);
			}
		}

		int exitVal = pr.waitFor();
		
		if (exitVal != 0) {
			out.println("Exited with error code " + exitVal);
		}

		for (VerificationError error : errors) {
			if (error != null) {
				IMarker marker = selectedFile.createMarker(VerificationError.JSTAR_ERROR_MARKER);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);

				final ErrorPos errorOffset = getErrorPos(
						selectedFile, 
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
		try {
			IConsole myConsole = findConsole(CONSOLE);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			String id = IConsoleConstants.ID_CONSOLE_VIEW;
			IConsoleView view = (IConsoleView) page.showView(id);
			view.display(myConsole);
		} 
		catch (PartInitException pie) {
			pie.printStackTrace();
		}
		catch (NullPointerException npe) {
			npe.printStackTrace();
		}
		
	}

	private class ErrorPos {
		private int startPos;
		private int endPos;

		public ErrorPos(int start, int end) {
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

	private ErrorPos getErrorPos(IFile javaFile, int startLine, int endLine, int startSymbol, int endSymbol) {
		BufferedReader input;
		String line = null;
		int lineNumber = 1;
		int start = 0;
		int end = 0;
		
		if (startLine == -1 || endLine == -1 || startSymbol == -1 || endSymbol == -1) {
			try {
				input = new BufferedReader(new InputStreamReader(javaFile.getContents()));
				line = input.readLine();
				
				return new ErrorPos(0, line.length());				
			} catch (CoreException ce) {
				ce.printStackTrace();
				ce.printStackTrace(getConsoleStream());
			} catch (IOException ioe) {
				ioe.printStackTrace();
				ioe.printStackTrace(getConsoleStream());
			}
			
			return new ErrorPos(0, 0);			
		}

		try {
			char character;
			char nextCharacter = ' ';
			InputStreamReader inputStreamReader = new InputStreamReader(javaFile.getContents());	
			
			character = (char)inputStreamReader.read();
			while (inputStreamReader.ready()) {
				
				if (lineNumber < startLine) {
					start += 1;
					end += 1;
				} 
				else if (lineNumber < endLine) {
					end += 1;
				}
				else {
					break;
				}
				
				nextCharacter = (char)inputStreamReader.read();
				if (character == '\n' || (character == '\r' && nextCharacter != '\n') ) {
					lineNumber++;
				}
				
				character = nextCharacter;
			}

			start += startSymbol - 1;
			end += endSymbol;

			return new ErrorPos(start, end);

		} catch (CoreException ce) {
			ce.printStackTrace();
			ce.printStackTrace(getConsoleStream());
		} catch (IOException ioe) {
			ioe.printStackTrace();
			ioe.printStackTrace(getConsoleStream());
		}

		return new ErrorPos(0, 0);
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
	
	private MessageConsoleStream printLine(String line, MessageConsoleStream out) {
		
		while (StringUtils.isNotEmpty(line)) {
			final ColourIndexPair colourIndex = findNextColour(line);
			int index = colourIndex.getIndex();
			Colour colour = colourIndex.getColour();
			
			if (index == -1) {
				out.print(line);
				line = "";
			}
			else {
				out.print(line.substring(0, index));
				line = line.substring(index + colour.getCmdSymbolCount());
				out = out.getConsole().newMessageStream();
				out.setColor(new Color(null, colour.getRGB()));
			}
		}

		out.println();
		return out;
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
