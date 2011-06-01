/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.processing.annotations;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import com.jstar.eclipse.annotations.ExceptionSpec;
import com.jstar.eclipse.annotations.Import;
import com.jstar.eclipse.annotations.InitSpec;
import com.jstar.eclipse.annotations.InitSpecStatic;
import com.jstar.eclipse.annotations.InitSpecs;
import com.jstar.eclipse.annotations.InitSpecsStatic;
import com.jstar.eclipse.annotations.Predicate;
import com.jstar.eclipse.annotations.Predicates;
import com.jstar.eclipse.annotations.Spec;
import com.jstar.eclipse.annotations.SpecStatic;
import com.jstar.eclipse.annotations.Specs;
import com.jstar.eclipse.annotations.SpecsStatic;
import com.jstar.eclipse.processing.annotations.objects.ImportObject;
import com.jstar.eclipse.processing.annotations.objects.PredicateObject;
import com.jstar.eclipse.processing.annotations.objects.SpecObject;
import com.jstar.eclipse.processing.annotations.objects.SpecObjectList;

public class AllAnnotations {
	private List<FileAnnotations> annotations;
	
	public AllAnnotations() {
		annotations = new LinkedList<FileAnnotations>();
	}
	
	public List<FileAnnotations> getFileAnnotations() {
		return annotations;
	}
	
	public boolean isEmpty() {
		boolean isEmpty = true;
		
		for (final FileAnnotations fileAnnotations : annotations) {
			if (!fileAnnotations.isEmpty()) {
				isEmpty = false;
			}
		}
		
		return isEmpty;
	}
	
	private Map<String, String> getExceptionPosts(ExceptionSpec[] exceptionSpecs) {
	    final Map<String, String> excepPosts = new HashMap<String, String>();
	    
		if (exceptionSpecs.length > 0) {
			for (final ExceptionSpec exceptionSpec : exceptionSpecs) {
				excepPosts.put(exceptionSpec.name(), exceptionSpec.post());
			}
		}
		
		return excepPosts;
	}
	
	public void addAnnotation(String sourceFileName, TypeElement className, Element element, AnnotationMirror mirror, long startPos, long endPos) {	
		final FileAnnotations fileAnnotations = getFileAnnotations(sourceFileName);
		final ClassAnnotations classAnnotations = fileAnnotations.getClassAnnotations(className);
		
		final String baseSourceFileName = fileAnnotations.getBaseSourceFileName();
		
		if (AnnotationType.Import.getName().equals(mirror.getAnnotationType().toString())) {	
			ImportObject annotationObject = new ImportObject(startPos, endPos, baseSourceFileName);
			Import importAnnotation = element.getAnnotation(Import.class);
			for (String value : importAnnotation.value()) {
				annotationObject.addSpecFile(value);
			}	
			
			fileAnnotations.addImportAnnotations(annotationObject);
			return;
		}
		
		if (AnnotationType.Spec.getName().equals(mirror.getAnnotationType().toString())) {	
			Spec specAnnotation = element.getAnnotation(Spec.class);
			SpecObject annotationObject = getSpecObject(specAnnotation, element, startPos, endPos, baseSourceFileName);
			annotationObject.setMethodDeclaration(getMehodDeclaration(element));
			classAnnotations.addAnnotation(AnnotationType.Spec, annotationObject);
			return;
		}
		
		if (AnnotationType.SpecStatic.getName().equals(mirror.getAnnotationType().toString())) {	
			SpecStatic specAnnotation = element.getAnnotation(SpecStatic.class);
			SpecObject annotationObject = getSpecStaticObject(specAnnotation, element, startPos, endPos, baseSourceFileName);
			annotationObject.setMethodDeclaration(getMehodDeclaration(element));
			classAnnotations.addAnnotation(AnnotationType.Spec, annotationObject);
			return;
		}
		
		if (AnnotationType.Specs.getName().equals(mirror.getAnnotationType().toString())) {	
			SpecObjectList annotationObjectList = new SpecObjectList(startPos, endPos, baseSourceFileName);
			Specs specAnnotations = element.getAnnotation(Specs.class);
			
			final String methodDeclaration = getMehodDeclaration(element);
			
			if (specAnnotations.value().length > 0) {
				for (Spec specAnnotation : specAnnotations.value()) {
					SpecObject annotationObject = getSpecObject(specAnnotation, element, startPos, endPos, baseSourceFileName);
					annotationObject.setMethodDeclaration(methodDeclaration);
					annotationObjectList.addSpecObjects(annotationObject);
				}
	
				annotationObjectList.setMethodDeclaration(methodDeclaration);
				
				classAnnotations.addAnnotation(AnnotationType.Spec, annotationObjectList);
			}
			return;
		}
		
		if (AnnotationType.SpecsStatic.getName().equals(mirror.getAnnotationType().toString())) {	
			SpecObjectList annotationObjectList = new SpecObjectList(startPos, endPos, baseSourceFileName);
			SpecsStatic specAnnotations = element.getAnnotation(SpecsStatic.class);
			
			final String methodDeclaration = getMehodDeclaration(element);
			
			if (specAnnotations.value().length > 0) {
				for (SpecStatic specAnnotation : specAnnotations.value()) {
					SpecObject annotationObject = getSpecStaticObject(specAnnotation, element, startPos, endPos, baseSourceFileName);
					annotationObject.setMethodDeclaration(methodDeclaration);
					annotationObjectList.addSpecObjects(annotationObject);
				}
	
				annotationObjectList.setMethodDeclaration(methodDeclaration);
				annotationObjectList.setStat(true);
				
				classAnnotations.addAnnotation(AnnotationType.Spec, annotationObjectList);
			}
			return;
		}
		
		if (AnnotationType.Predicate.getName().equals(mirror.getAnnotationType().toString())) {	
			Predicate predicate = element.getAnnotation(Predicate.class);		
			classAnnotations.addAnnotation(AnnotationType.Predicate, getPredicateObject(predicate, startPos, endPos, baseSourceFileName));
			return;
		}
		
		if (AnnotationType.Predicates.getName().equals(mirror.getAnnotationType().toString())) {			
			Predicates predicates = element.getAnnotation(Predicates.class);
			for (Predicate predicate : predicates.value()) {
				classAnnotations.addAnnotation(AnnotationType.Predicate, getPredicateObject(predicate, startPos, endPos, baseSourceFileName));
			}		
			return;
		}
		
		if (AnnotationType.InitSpec.getName().equals(mirror.getAnnotationType().toString())) {			
			InitSpec specAnnotation = element.getAnnotation(InitSpec.class);
			classAnnotations.addAnnotation(AnnotationType.InitSpec, getInitSpecObject(specAnnotation, startPos, endPos, baseSourceFileName));
			return;
		}
		
		if (AnnotationType.InitSpecStatic.getName().equals(mirror.getAnnotationType().toString())) {			
			InitSpecStatic specAnnotation = element.getAnnotation(InitSpecStatic.class);
			classAnnotations.addAnnotation(AnnotationType.InitSpec, getInitSpecStaticObject(specAnnotation, startPos, endPos, baseSourceFileName));
			return;
		}
		
		if (AnnotationType.InitSpecs.getName().equals(mirror.getAnnotationType().toString())) {	
			SpecObjectList annotationObjectList = new SpecObjectList(startPos, endPos, baseSourceFileName);
			InitSpecs specAnnotations = element.getAnnotation(InitSpecs.class);
			
			if (specAnnotations.value().length > 0) {
				for (InitSpec specAnnotation : specAnnotations.value()) {
					SpecObject annotationObject = getInitSpecObject(specAnnotation, startPos, endPos, baseSourceFileName);
					annotationObjectList.addSpecObjects(annotationObject);
				}
	
				annotationObjectList.setMethodDeclaration("void <init>()");
				
				classAnnotations.addAnnotation(AnnotationType.InitSpec, annotationObjectList);
			}
			return;
		}
		
		if (AnnotationType.InitSpecsStatic.getName().equals(mirror.getAnnotationType().toString())) {	
			SpecObjectList annotationObjectList = new SpecObjectList(startPos, endPos, baseSourceFileName);
			InitSpecsStatic specAnnotations = element.getAnnotation(InitSpecsStatic.class);
			
			if (specAnnotations.value().length > 0) {
				for (InitSpecStatic specAnnotation : specAnnotations.value()) {
					SpecObject annotationObject = getInitSpecStaticObject(specAnnotation, startPos, endPos, baseSourceFileName);
					annotationObjectList.addSpecObjects(annotationObject);
				}
	
				annotationObjectList.setMethodDeclaration("void <init>()");
				annotationObjectList.setStat(true);
				
				classAnnotations.addAnnotation(AnnotationType.InitSpec, annotationObjectList);
			}
			return;
		}
	}
	
	private FileAnnotations getFileAnnotations(final String sourceFileName) {
		for (final FileAnnotations fileAnnotations : annotations) {
			if (fileAnnotations.getSourceFileName().equals(sourceFileName)) {
				return fileAnnotations;
			}
		}
		
		FileAnnotations newFileAnnotations = new FileAnnotations(sourceFileName);
		annotations.add(newFileAnnotations);
		return newFileAnnotations;
	}
	
	private String getMehodDeclaration(Element element) {
		StringBuilder methodDeclaration = new StringBuilder();
		if (element instanceof ExecutableElement) {
			for (Modifier modifier : ((ExecutableElement) element).getModifiers()) {
				methodDeclaration.append(modifier.toString()).append(" ");
			}
			
			methodDeclaration.append(((ExecutableElement)element).getReturnType().toString()).append(" ");
			
			methodDeclaration.append(element.getSimpleName().toString());
			methodDeclaration.append('(');
			List<? extends VariableElement> params = ((ExecutableElement) element).getParameters(); 			
			List<? extends TypeMirror> typeParams = ((ExecutableType) element.asType()).getParameterTypes(); 
					
			for (int index = 0 ; index < params.size() ; index++) {
				methodDeclaration.append(typeParams.get(index).toString()).append(" ").append(params.get(index).getSimpleName().toString()).append(", ");
			}
			
			if (params.size() > 0) {
				methodDeclaration = methodDeclaration.delete(methodDeclaration.length() - 2, methodDeclaration.length());
			}
				
			methodDeclaration.append(')');
		}
		
		return methodDeclaration.toString();
	}

	private SpecObject getInitSpecObject(InitSpec specAnnotation, long startPos, long endPos, String sourceFileName) {
		SpecObject annotationObject = new SpecObject(startPos, endPos, sourceFileName);
		annotationObject.setPre(specAnnotation.pre());
		annotationObject.setPost(specAnnotation.post());
		annotationObject.setMethodDeclaration("void <init>()");
		annotationObject.setExceptionPosts(getExceptionPosts(specAnnotation.excep()));
		
		return annotationObject;
	}
	
	private SpecObject getInitSpecStaticObject(InitSpecStatic specAnnotation, long startPos, long endPos, String sourceFileName) {
		SpecObject annotationObject = new SpecObject(startPos, endPos, sourceFileName);
		annotationObject.setPre(specAnnotation.pre());
		annotationObject.setPost(specAnnotation.post());
		annotationObject.setStat(true);
		annotationObject.setMethodDeclaration("void <init>()");
		annotationObject.setExceptionPosts(getExceptionPosts(specAnnotation.excep()));
		
		return annotationObject;
	}
	
	private PredicateObject getPredicateObject(Predicate predicate, long startPos, long endPos, String sourceFileName) {
		PredicateObject annotationObject = new PredicateObject(startPos, endPos, sourceFileName);
		annotationObject.setFormula(predicate.formula());
		annotationObject.setPredicate(predicate.predicate());
		annotationObject.setType(predicate.type());
		
		return annotationObject;
	}
	
	private SpecObject getSpecObject(Spec specAnnotation, Element element, long startPos, long endPos, String sourceFileName) {
		SpecObject annotationObject = new SpecObject(startPos, endPos, sourceFileName);
		annotationObject.setPre(specAnnotation.pre());
		annotationObject.setPost(specAnnotation.post());
		annotationObject.setExceptionPosts(getExceptionPosts(specAnnotation.excep()));
		
		return annotationObject;
	}
	
	private SpecObject getSpecStaticObject(SpecStatic specAnnotation, Element element, long startPos, long endPos, String sourceFileName) {
		SpecObject annotationObject = new SpecObject(startPos, endPos, sourceFileName);
		annotationObject.setPre(specAnnotation.pre());
		annotationObject.setPost(specAnnotation.post());					
		annotationObject.setMethodDeclaration(getMehodDeclaration(element));
		annotationObject.setStat(true);
		annotationObject.setExceptionPosts(getExceptionPosts(specAnnotation.excep()));
		
		return annotationObject;
	}	
}
