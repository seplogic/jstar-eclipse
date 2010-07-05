package com.jstar.services;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.jstar.configuration.ConfigurationProperties;

public class JStar {

	private static JStar instance;
	
	private static final String SootOutput = "sootOutput";

	private JStar() {
	}

	public static JStar getInstance() {
		if (instance == null) {
			instance = new JStar();
		}
		return instance;
	}

	@SuppressWarnings( { "unchecked", "static-access" })
	public String convertToJimple(String fileToConvertString) {

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(
				null, null, null);

		Iterable<JavaFileObject> fileObjects = (Iterable<JavaFileObject>) fileManager.getJavaFileObjects(fileToConvertString);
		File fileToConvert = new File(fileToConvertString);
		String fileDirectory = fileToConvert.getParentFile().getAbsolutePath();
		String[] options = new String[] { "-d", fileDirectory};
		compiler.getTask(null, fileManager, null, Arrays.asList(options), null, fileObjects).call();
		try {
			fileManager.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String classFile = "";
		int dot = fileToConvert.getName().lastIndexOf('.');
		if (0 < dot && dot <= fileToConvert.getName().length() - 2) {
			classFile = fileToConvert.getName().substring(0, dot);
		}

		final String sootOutput = fileDirectory + File.separator + SootOutput;
		
		String[] args = {"-cp", fileDirectory + File.pathSeparator + ConfigurationProperties.getSootClassPathRt(),
				"-f", "J", 
				"-output-dir", sootOutput, 
				classFile};
		
		soot.G.v().reset();
		soot.Main.main(args);

		return sootOutput + File.separator + classFile + ".jimple";

	}

	public Process executeJStar(final String fileToVerify, final String spec,
			final String logic, final String abs) throws IOException {

		final String jimpleFile = convertToJimple(fileToVerify);

		ProcessBuilder pb = new ProcessBuilder(ConfigurationProperties.getJStarExecutable(), 
				"-l", logic, 
				"-a", abs, 
				"-s", spec, 
				"-f", jimpleFile);
		
		Map<String, String> env = pb.environment();
		
		String path = env.get("Path");
		env.put("Path", ConfigurationProperties.getCygwinPath()
				+ File.pathSeparator + ConfigurationProperties.getJStarPath()
				+ File.pathSeparator + path);
		env.put(ConfigurationProperties.JSTAR_LOGIC_LIBRARY,
				ConfigurationProperties.getJStarLogicLibrary());
		env.put(ConfigurationProperties.JSTAR_ABS_LIBRARY,
				ConfigurationProperties.getJStarAbsLibrary());
		env.put(ConfigurationProperties.JSTAR_SPECS_LIBRARY,
				ConfigurationProperties.getJStarSpecLibrary());

		return pb.start();
	}

}
