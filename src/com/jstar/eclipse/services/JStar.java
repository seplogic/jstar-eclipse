package com.jstar.eclipse.services;


import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
	
	private String removeFileExtension(final String fileName) {
		int dot = fileName.lastIndexOf('.');
		return fileName.substring(0, dot);
	}


	@SuppressWarnings("static-access")
	private String convertToJimple(final IFile fileToConvert) {
		
		final ICompilationUnit compilationUnit = JavaCore.createCompilationUnitFrom(fileToConvert);
		final IPackageFragmentRoot pfr = (IPackageFragmentRoot) compilationUnit.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		final IPackageFragment pf = (IPackageFragment) compilationUnit.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		
		String fileDirectory = compilationUnit.getJavaProject().getProject().getLocation().toOSString();
		fileDirectory = fileDirectory.substring(0, fileDirectory.lastIndexOf(File.separator));
        fileDirectory = fileDirectory + pfr.getPath().toOSString();  
        
        String javaFile = "";
        
		if (pf.isDefaultPackage()) {
			javaFile = removeFileExtension(compilationUnit.getElementName());
		}
		else {
			javaFile = pf.getElementName()+ '.' + removeFileExtension(compilationUnit.getElementName());
			throw new RuntimeException("Currently jStar does not support packages");
		}
		
		final String sootOutput = fileDirectory + File.separator + SootOutput;
		
		final String[] args = {"-cp", fileDirectory + File.pathSeparator + PreferenceConstants.getSootClassPathRt(),
				"-f", "J", 
				"-output-dir", sootOutput, 
				"-src-prec", "java",
				"-print-tags",
				javaFile};
		
		soot.G.v().reset();
		soot.Main.main(args);
	
		return sootOutput + File.separator + javaFile + ".jimple";
	}

	public Process executeJStar(final IFile selectedFile, final String spec,
			final String logic, final String abs, final PrintMode printMode) throws IOException {
		
		JStar.getInstance().setSpecFile(spec);
		JStar.getInstance().setLogicFile(logic);
		JStar.getInstance().setAbsFile(abs);

		final String jimpleFile = convertToJimple(selectedFile);

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
