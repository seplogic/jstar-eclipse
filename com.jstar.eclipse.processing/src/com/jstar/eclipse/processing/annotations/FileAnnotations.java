package com.jstar.eclipse.processing.annotations;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Element;

import org.apache.commons.io.FilenameUtils;

import com.jstar.eclipse.processing.annotations.objects.ImportObject;

public class FileAnnotations {
	
	private String sourceFileName;
	
	private List<ImportObject> importAnnotations;
	
	private List<ClassAnnotations> classAnnotations;
	
	public FileAnnotations(final String sourceFileName) {
		this.sourceFileName = sourceFileName;
		classAnnotations = new LinkedList<ClassAnnotations>();
		importAnnotations = new LinkedList<ImportObject>();
	}

	public void addImportAnnotations(final ImportObject importAnnotation) {
		importAnnotations.add(importAnnotation);
	}

	public List<ImportObject> getImportAnnotations() {
		return importAnnotations;
	}
	
	public ClassAnnotations getClassAnnotations(final Element className) {
		for (final ClassAnnotations classAnnotationList : classAnnotations) {
			if (classAnnotationList.getClassName().equals(className)) {
				return classAnnotationList;
			}
		}
		
		ClassAnnotations newClassAnnotations = new ClassAnnotations(className);
		classAnnotations.add(newClassAnnotations);
		return newClassAnnotations;
	}

	public List<ClassAnnotations> getClassAnnotations() {
		return classAnnotations;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}
	
	public String getBaseSourceFileName() {
		return FilenameUtils.getBaseName(sourceFileName);
	}

	public boolean isEmpty() {
		boolean isEmpty = true;
		
		for (final ClassAnnotations classAnnotationList : classAnnotations) {
			if (!classAnnotationList.isEmpty()) {
				isEmpty = false;
			}
		}
		
		return isEmpty;
	}

}
