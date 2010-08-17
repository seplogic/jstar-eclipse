package com.jstar.eclipse.services;


import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.jstar.eclipse.preferences.PreferenceConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

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
	
	public String removeFileExtension(final String fileName) {
		int dot = fileName.lastIndexOf('.');
		return fileName.substring(0, dot);
	}


	@SuppressWarnings("static-access")
	// in io 2.0 FileUtils.listFiles should return Collection<File> instead of Collection
	public List<File> convertToJimple(final IFile fileToConvert) {
		
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
		
		final String sootOutput = fileDirectory + File.separator + SootOutput + File.separator + javaFile;
		
		final String[] args = {
				"-cp", fileDirectory + File.pathSeparator + PreferenceConstants.getSootClassPathRt() + 
									   File.pathSeparator + PreferenceConstants.getAnnotationsPath(),
				"-f", "J", 
				"-output-dir", sootOutput, 
				"-src-prec", "java",
				"-v",
				"-print-tags",
				javaFile};
		
		soot.G.v().reset();
		soot.Main.main(args);
		
		final File directory = new File(sootOutput);
		
		List<String> types = new LinkedList<String>();
		try {
			for (IType type : compilationUnit.getAllTypes()) {
				types.add(type.getElementName());
			}
		} catch (JavaModelException e) {
			throw new RuntimeException("An error occurred while getting all types.");
		}
		
		final List<File> jimpleFiles = new LinkedList<File>();
		for (Object file : (FileUtils.listFiles(directory, new WildcardFileFilter("*.jimple"), null))) {
			//TODO : packages
			if (types.indexOf(removeFileExtension(((File)file).getName())) != -1) {
				jimpleFiles.add((File)file);
			}
		}
				
		if (jimpleFiles == null || jimpleFiles.size() == 0) {
			throw new RuntimeException("An error occurred while converting to java file to jimple format");
		}
		
		return jimpleFiles;
	}

	public Process executeJStar(final IFile selectedFile, final String spec,
			final String logic, final String abs, final String jimpleFile, final PrintMode printMode) throws IOException {
		
		JStar.getInstance().setSpecFile(spec);
		JStar.getInstance().setLogicFile(logic);
		JStar.getInstance().setAbsFile(abs);

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
	
	public void checkConfigurations() {
		if (SystemUtils.IS_OS_WINDOWS && StringUtils.isEmpty(PreferenceConstants.getCygwinPath())) {
			throw new RuntimeException("Please specify the location of cygwin");
		}
		
		if (StringUtils.isEmpty(PreferenceConstants.getJStarPath())) {
			throw new RuntimeException("Please specify the location of jStar");
		}
		
		if (StringUtils.isEmpty(PreferenceConstants.getJStarLogicLibrary())) {
			throw new RuntimeException("Please specify the location of jStar logic library");
		}
		
		if (StringUtils.isEmpty(PreferenceConstants.getJStarSpecLibrary())) {
			throw new RuntimeException("Please specify the location of jStar specification library");
		}
		
		if (StringUtils.isEmpty(PreferenceConstants.getSootClassPathRt())) {
			throw new RuntimeException("Please specify the location of rt.jar file in JAVA jre");
		}
		
		if (StringUtils.isEmpty(PreferenceConstants.getAnnotationsPath())) {
			throw new RuntimeException("Please specify the location of annotations.jar file");
		}
		
		if (StringUtils.isEmpty(PreferenceConstants.getAnnotationProcessorPath())) {
			throw new RuntimeException("Please specify the location of jstar_processing.jar file");
		}
	}

	private String getCygwinPath() {
		final String cygwinPath = PreferenceConstants.getCygwinPath();
		if (StringUtils.isNotEmpty(cygwinPath)) {
			return cygwinPath + File.pathSeparator;
		}
		return "";
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
