/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import com.jstar.eclipse.exceptions.ConfigurationException;
import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.objects.JavaFilePersistentProperties;
import com.jstar.eclipse.preferences.PreferenceConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

public class JStar {
	
	private static JStar instance;
	private static final String SootOutput = "sootOutput";
	private static final String TERM = "TERM";
	private static final String XTERMCOLOR = "xterm-color";

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
	
	public enum DebugMode {
		PARSING("p", JavaFilePersistentProperties.DM_PARSING),
		SYMBOLIC("s", JavaFilePersistentProperties.DM_SYMBOLIC),
		CORE("c", JavaFilePersistentProperties.DM_CORE),
		SMT("m", JavaFilePersistentProperties.DM_SMT);
		
		private final String cmdOption;
		private final QualifiedName name;
		
		DebugMode(String cmdOption, QualifiedName name) {
			this.cmdOption = cmdOption;
			this.name = name;
		}

		public String getCmdOption() {
			return cmdOption;
		}

		public QualifiedName getQualifiedName() {
			return name;
		}		
	}
	
	@SuppressWarnings("static-access")
	// in io 2.0 FileUtils.listFiles should return Collection<File> instead of Collection
	public List<File> convertToJimple(final JavaFile fileToConvert) {
		String fileDirectory = fileToConvert.getOutputDirectory().getLocation().toOSString();         
        String javaFile = fileToConvert.getNameWithPackage();
      
		final String sootOutput = fileDirectory + File.separator + SootOutput + File.separator + javaFile;
		
		final String[] args = {
				"-cp", PreferenceConstants.getSootClassPath() + File.pathSeparator + fileToConvert.getProjectClasspath(),
				"-f", "J", 
				"-output-dir", sootOutput, 
				"-src-prec", "java",
				//"-v",
				"-print-tags",
				javaFile};
		
		soot.G.v().reset();
		soot.Main.main(args);
		
		final List<String> types = fileToConvert.getTypes();		
		final List<File> jimpleFiles = new LinkedList<File>();
		
		for (Object file : (FileUtils.listFiles(new File(sootOutput), new WildcardFileFilter("*.jimple"), null))) {
			final String fileName = ((File)file).getName();
			
			if (types.indexOf(new Path(fileName).removeFileExtension().toOSString()) != -1) {
				jimpleFiles.add((File)file);
			}
		}

		if (jimpleFiles == null || jimpleFiles.size() == 0) {
			ConsoleService.getInstance().printErrorMessage("An error occurred while converting java file to jimple format.");
			throw new NullPointerException();
		}
		
		return jimpleFiles;
	}

	public Process executeJStar(final IFolder workingDirectory, final IFolder folder, final String spec,
			final String logic, final String abs, final String jimpleFile, final PrintMode printMode, final String debugMode) throws IOException {

		final List<String> command = new ArrayList<String>();
		command.add(PreferenceConstants.getJStarExecutable());
		command.add("-e");
		command.add(printMode.getCmdOption());
		command.add("-l");
		command.add(logic);
		command.add("-a");
		command.add(abs);
		command.add("-s");
		command.add(spec);
		command.add("-f");
		command.add(jimpleFile);
		
		if (StringUtils.isNotBlank(debugMode)) {
			command.add("-d");
			command.add(debugMode);
		}
		
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.directory(new File(workingDirectory.getLocation().toOSString()));
		
		Map<String, String> env = pb.environment();
		
		//TODO: jStar accepts only ':' as path separator
		env.put(PreferenceConstants.JSTAR_LOGIC_LIBRARY, 
				PreferenceConstants.getJStarLogicLibrary() + ':' + folder.getLocation().toOSString());
		
		env.put(PreferenceConstants.JSTAR_ABS_LIBRARY, 
				PreferenceConstants.getJStarAbsLibrary() + ':' + folder.getLocation().toOSString());
		
		env.put(PreferenceConstants.JSTAR_SPECS_LIBRARY,
				PreferenceConstants.getJStarSpecLibrary() + ':' + folder.getLocation().toOSString());
		
		env.put(PreferenceConstants.JSTAR_SMT_PATH, PreferenceConstants.getSmtPath());
		env.put(PreferenceConstants.JSTAR_SMT_ARGUMENTS, PreferenceConstants.getSmtAtguments());
		
		env.put(TERM, XTERMCOLOR);

		return pb.start();
	}
	
	private String getErrorMessage(final String unspecifiedConfig) {
		return "The location of " + unspecifiedConfig + " is not specified. Go to Window -> Preferences -> jStar to specify it."; 
	}
	
	public void checkConfigurations() throws ConfigurationException {			
		if (StringUtils.isEmpty(PreferenceConstants.getJStarExecutable())) {
			throw new ConfigurationException(getErrorMessage("jStar executable"));
		}
		
		if (StringUtils.isEmpty(PreferenceConstants.getJStarLogicLibrary())) {
			throw new ConfigurationException(getErrorMessage("jStar logic library"));
		}
		
		if (StringUtils.isEmpty(PreferenceConstants.getJStarSpecLibrary())) {
			throw new ConfigurationException(getErrorMessage("jStar specification library"));
		}
		
		if (SystemUtils.IS_OS_MAC) {
			if (StringUtils.isEmpty(PreferenceConstants.getSootClassPathClasses())) {
				throw new ConfigurationException(getErrorMessage("classes.jar file in JAVA jre"));
			}
			
			if (StringUtils.isEmpty(PreferenceConstants.getSootClassPathUi())) {
				throw new ConfigurationException(getErrorMessage("ui.jar file in JAVA jre"));
			}
		}
		else {
			if (StringUtils.isEmpty(PreferenceConstants.getSootClassPathRt())) {
				throw new ConfigurationException(getErrorMessage("rt.jar file in JAVA jre"));
			}
		}
	}
}
