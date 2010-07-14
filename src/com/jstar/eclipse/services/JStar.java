package com.jstar.eclipse.services;


import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.jstar.eclipse.preferences.PreferenceConstants;

public class JStar {

	private static JStar instance;
	
	private static final String SootOutput = "sootOutput";
	
	private String specFile = "";
	private String logicFile = "";
	private String absFile = "";

	private JStar() {
	}

	public static JStar getInstance() {
		if (instance == null) {
			instance = new JStar();
		}
		return instance;
	}
	
	public enum PrintMode {
		QUIET("-q"),
		VERBOSE("-v");
		
		private final String cmdOption;
		
		PrintMode(String cmdOption) {
			this.cmdOption = cmdOption;
		}

		public String getCmdOption() {
			return cmdOption;
		}		
	}


	@SuppressWarnings("static-access")
	public String convertToJimple(String fileToConvertString) {

		File fileToConvert = new File(fileToConvertString);
		String fileDirectory = fileToConvert.getParentFile().getAbsolutePath();

		String classFile = "";
		int dot = fileToConvert.getName().lastIndexOf('.');
		if (0 < dot && dot <= fileToConvert.getName().length() - 2) {
			classFile = fileToConvert.getName().substring(0, dot);
		}

		final String sootOutput = fileDirectory + File.separator + SootOutput;
		
		String[] args = {"-cp", fileDirectory + File.pathSeparator + PreferenceConstants.getSootClassPathRt(),
				"-f", "J", 
				"-output-dir", sootOutput, 
				"-src-prec", "java",
				"-print-tags",
				classFile};
		
		soot.G.v().reset();
		soot.Main.main(args);
	
		return sootOutput + File.separator + classFile + ".jimple";
	}

	public Process executeJStar(final IFile selectedFile, final String spec,
			final String logic, final String abs, final PrintMode printMode) throws IOException {
		
		String fileToVerify = selectedFile.getLocation().toString();
		
		JStar.getInstance().setSpecFile(spec);
		JStar.getInstance().setLogicFile(logic);
		JStar.getInstance().setAbsFile(abs);

		final String jimpleFile = convertToJimple(fileToVerify);

		ProcessBuilder pb = new ProcessBuilder(PreferenceConstants.getJStarExecutable(), 
				"-e", printMode.getCmdOption(),
				"-l", logic, 
				"-a", abs, 
				"-s", spec, 
				"-f", jimpleFile);
		
		Map<String, String> env = pb.environment();
		
		String path = env.get("Path");
		env.put("Path", PreferenceConstants.getCygwinPath()
				+ File.pathSeparator + PreferenceConstants.getJStarPath()
				+ File.pathSeparator + path);
		
		env.put(PreferenceConstants.JSTAR_LOGIC_LIBRARY,
				PreferenceConstants.getJStarLogicLibrary());
		env.put(PreferenceConstants.JSTAR_ABS_LIBRARY,
				PreferenceConstants.getJStarAbsLibrary());
		env.put(PreferenceConstants.JSTAR_SPECS_LIBRARY,
				PreferenceConstants.getJStarSpecLibrary());

		return pb.start();
	}

	public void setSpecFile(String specFile) {
		JStar.getInstance().specFile = specFile;
	}

	public String getSpecFile() {
		return specFile;
	}

	public void setLogicFile(String logicFile) {
		JStar.getInstance().logicFile = logicFile;
	}

	public String getLogicFile() {
		return logicFile;
	}

	public void setAbsFile(String absFile) {
		JStar.getInstance().absFile = absFile;
	}

	public String getAbsFile() {
		return absFile;
	}

}
