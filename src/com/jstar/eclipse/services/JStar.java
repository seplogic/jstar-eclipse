package com.jstar.eclipse.services;


import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import com.jstar.eclipse.exceptions.ConfigurationException;
import com.jstar.eclipse.objects.JavaFile;
import com.jstar.eclipse.preferences.PreferenceConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

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
	
	public String removeFileExtension(final String fileName) {
		int dot = fileName.lastIndexOf('.');
		return fileName.substring(0, dot);
	}

	@SuppressWarnings("static-access")
	// in io 2.0 FileUtils.listFiles should return Collection<File> instead of Collection
	public List<File> convertToJimple(final JavaFile fileToConvert) {
		String fileDirectory = fileToConvert.getPackageFragmentRootLocation().toOSString();         
        String javaFile = fileToConvert.getNameWithPackage();		
		final String sootOutput = fileDirectory + File.separator + SootOutput + File.separator + javaFile;
		
		final String[] args = {
				"-cp", PreferenceConstants.getSootClassPathRt() + File.pathSeparator + fileToConvert.getProjectClasspath(),
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
			if (types.indexOf(removeFileExtension(((File)file).getName())) != -1) {
				jimpleFiles.add((File)file);
			}
		}

		if (jimpleFiles == null || jimpleFiles.size() == 0) {
			ConsoleService.getInstance().printErrorMessage("An error occurred while converting java file to jimple format.");
			throw new NullPointerException();
		}
		
		return jimpleFiles;
	}

	public Process executeJStar(final String spec,
			final String logic, final String abs, final String jimpleFile, final PrintMode printMode) throws IOException {

		ProcessBuilder pb = new ProcessBuilder(PreferenceConstants.getJStarExecutable(), 
				"-e", printMode.getCmdOption(),
				"-l", logic, 
				"-a", abs, 
				"-s", spec, 
				"-f", jimpleFile);
		
		Map<String, String> env = pb.environment();
		
		String path = StringUtils.isEmpty(env.get("Path")) ? "" : File.pathSeparator + env.get("Path");
		env.put("Path", getCygwinPath() + PreferenceConstants.getJStarPath() + path);
		
		env.put(PreferenceConstants.JSTAR_LOGIC_LIBRARY,
				PreferenceConstants.getJStarLogicLibrary());
		env.put(PreferenceConstants.JSTAR_ABS_LIBRARY,
				PreferenceConstants.getJStarAbsLibrary());
		env.put(PreferenceConstants.JSTAR_SPECS_LIBRARY,
				PreferenceConstants.getJStarSpecLibrary());

		return pb.start();
	}
	
	private String getErrorMessage(final String unspecifiedConfig) {
		return "The location of " + unspecifiedConfig + " is not specified. Go to Window -> Preferences -> jStar Configuration to specify it."; 
	}
	
	public void checkConfigurations() throws ConfigurationException {		
		if (SystemUtils.IS_OS_WINDOWS && StringUtils.isEmpty(PreferenceConstants.getCygwinPath())) {
			throw new ConfigurationException(getErrorMessage("cygwin"));
		}
		
		if (StringUtils.isEmpty(PreferenceConstants.getJStarPath())) {
			throw new ConfigurationException(getErrorMessage("jStar"));
		}
		
		if (StringUtils.isEmpty(PreferenceConstants.getJStarLogicLibrary())) {
			throw new ConfigurationException(getErrorMessage("jStar logic library"));
		}
		
		if (StringUtils.isEmpty(PreferenceConstants.getJStarSpecLibrary())) {
			throw new ConfigurationException(getErrorMessage("jStar specification library"));
		}
		
		if (StringUtils.isEmpty(PreferenceConstants.getSootClassPathRt())) {
			throw new ConfigurationException(getErrorMessage("rt.jar file in JAVA jre"));
		}
		
		if (StringUtils.isEmpty(PreferenceConstants.getAnnotationsPath())) {
			throw new ConfigurationException(getErrorMessage("annotations.jar file"));
		}
	}

	private String getCygwinPath() {
		final String cygwinPath = PreferenceConstants.getCygwinPath();
		if (StringUtils.isNotEmpty(cygwinPath)) {
			return cygwinPath + File.pathSeparator;
		}
		return "";
	}

}
