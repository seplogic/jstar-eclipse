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

import javax.lang.model.element.TypeElement;

import com.jstar.eclipse.processing.annotations.objects.AnnotationObject;

public class ClassAnnotations {
	
	private TypeElement className;
	
	private Map<AnnotationType, List<AnnotationObject>> annotations;
	
	public ClassAnnotations(final TypeElement className) {
		this.annotations = new HashMap<AnnotationType, List<AnnotationObject>>();
		this.className = className;
	}
	
	public void addAnnotation(final AnnotationType annotation, final AnnotationObject annotationObject) {
		List<AnnotationObject> list;
		
		if (annotations.get(annotation) == null) {
			list = new LinkedList<AnnotationObject>();
			annotations.put(annotation, list);
		}
		else {
			list = annotations.get(annotation);
		}
		
		list.add(annotationObject);	
	}
	
	public List<AnnotationObject> getAnnotations(AnnotationType annotation) {
		List<AnnotationObject> annotationList = annotations.get(annotation);
		
		if (annotationList != null) {
			return annotationList;
		}
		
		List<AnnotationObject> emptyList = new LinkedList<AnnotationObject>();
		annotations.put(annotation, emptyList);
		
		return emptyList;
	}

	public TypeElement getClassName() {
		return className;
	}

	public boolean isEmpty() {
		return annotations.isEmpty();
	}

}
