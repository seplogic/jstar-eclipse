package com.jstar.eclipse.processing;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

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
import com.jstar.eclipse.processing.objects.AnnotationObject;
import com.jstar.eclipse.processing.objects.ImportObject;
import com.jstar.eclipse.processing.objects.PredicateObject;
import com.jstar.eclipse.processing.objects.SpecObject;
import com.jstar.eclipse.processing.objects.SpecObjectList;

public class AllAnnotations {
	private Map<ClassInformation, Map<Annotation, List<AnnotationObject>>> annotations;
	
	public AllAnnotations() {
		annotations = new HashMap<ClassInformation, Map<Annotation, List<AnnotationObject>>>();
	}

	public void generateFile(String sourceFileName, Set<ClassInformation> classInfos, Writer writer) throws IOException {
		System.out.println(annotations.toString());
		
		Map<Annotation, List<AnnotationObject>> importObjectsMap = annotations.get(new ClassInformation(null, sourceFileName));
		
		if (importObjectsMap != null) {
			List<AnnotationObject> importObjects = importObjectsMap.get(Annotation.Import); 
			
			for (AnnotationObject importObject : importObjects) {
				importObject.generateFile(writer);
			}
		}
		
		for (ClassInformation classInfo : classInfos) {
			
			if (classInfo.getClassName() != null) {
			
				Map<Annotation, List<AnnotationObject>> map = annotations.get(classInfo);	
				
				if (classInfo.getClassName().getKind().isClass() || classInfo.getClassName().getKind().isInterface()) {
					addClassDeclaration(classInfo.getClassName(), writer);
					writer.write("\n");
				}
				else {
					throw new RuntimeException("Unknown kind of element. Currently only class and interface are supported."); 
				}
				
				if (map != null) {
					if (map.get(Annotation.Predicate) != null) {
						for (AnnotationObject predicate : map.get(Annotation.Predicate)) {
							predicate.generateFile(writer);
							writer.write("\n");
						}
					}
					
					if (map.get(Annotation.InitSpec) != null) {
						for (AnnotationObject initSpec : map.get(Annotation.InitSpec)) {
							initSpec.generateFile(writer);
							writer.write("\n");
						}
					}
					
					if (map.get(Annotation.Spec) != null) {
						for (AnnotationObject spec : map.get(Annotation.Spec)) {
							spec.generateFile(writer);
							writer.write("\n");
						}
					}
				}
				
				writer.write("}\n");
			}
		}
	}


	private void addClassDeclaration(Element className, Writer writer) throws IOException {
		final StringBuilder classDeclaration = new StringBuilder("\n");
		classDeclaration.append(className.getKind().toString().toLowerCase());
		classDeclaration.append(" ");
		classDeclaration.append(className.getSimpleName().toString()).append(" ");
		
		/*final TypeMirror superClass = ((TypeElement)className).getSuperclass();

		if (!superClass.getKind().equals(TypeKind.NONE) && !"java.lang.Object".equalsIgnoreCase(superClass.toString())) {
			System.out.println(superClass.toString());
			classDeclaration.append("extends ").append(superClass.toString()).append(" ");
		}*/
		
		/*final List<? extends TypeMirror> interfaces = ((TypeElement)className).getInterfaces();
		if (!interfaces.isEmpty()) {
			classDeclaration.append("implements ");
			
			for (TypeMirror singleInterface : interfaces) {
				classDeclaration.append(singleInterface).append(", ");
			}
			
			classDeclaration.delete(classDeclaration.length() - 2, classDeclaration.length());
		}*/
 		
		writer.write(classDeclaration.toString());
		writer.write("{ \n");		
	}

	public boolean isEmpty() {
		return annotations.isEmpty();
	}
	
	private void putAnnotation(ClassInformation className, Annotation annotation, AnnotationObject annotationObject) {
		Map<Annotation, List<AnnotationObject>> map;
		
		if (annotations.get(className) == null) {
			map = new HashMap<Annotation, List<AnnotationObject>>();
			annotations.put(className, map);
		}
		else {
			map = annotations.get(className);
		}
		
		List<AnnotationObject> list;
		if (map.get(annotation) == null) {
			list = new LinkedList<AnnotationObject>();
			map.put(annotation, list);
		}
		else {
			list = map.get(annotation);
		}
		
		list.add(annotationObject);	
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

	public void addAnnotation(Element element, AnnotationMirror mirror, long startPos, long endPos, ClassInformation elementClass) {	
		System.out.println(mirror.getAnnotationType().toString());
		
		if (Annotation.Import.getName().equals(mirror.getAnnotationType().toString())) {	
			ImportObject annotationObject = new ImportObject(startPos, endPos, elementClass.getSourceFileName());
			Import importAnnotation = element.getAnnotation(Import.class);
			for (String value : importAnnotation.value()) {
				annotationObject.addSpecFile(value);
			}	
			
			putAnnotation(new ClassInformation(null, elementClass.getSourceFileName()), Annotation.Import, annotationObject);
			return;
		}
		
		if (Annotation.Spec.getName().equals(mirror.getAnnotationType().toString())) {	
			Spec specAnnotation = element.getAnnotation(Spec.class);
			SpecObject annotationObject = getSpecObject(specAnnotation, element, startPos, endPos, elementClass.getSourceFileName());
			annotationObject.setMethodDeclaration(getMehodDeclaration(element));
			putAnnotation(elementClass, Annotation.Spec, annotationObject);
			return;
		}
		
		if (Annotation.SpecStatic.getName().equals(mirror.getAnnotationType().toString())) {	
			SpecStatic specAnnotation = element.getAnnotation(SpecStatic.class);
			SpecObject annotationObject = getSpecStaticObject(specAnnotation, element, startPos, endPos, elementClass.getSourceFileName());
			annotationObject.setMethodDeclaration(getMehodDeclaration(element));
			putAnnotation(elementClass, Annotation.Spec, annotationObject);
			return;
		}
		
		if (Annotation.Specs.getName().equals(mirror.getAnnotationType().toString())) {	
			SpecObjectList annotationObjectList = new SpecObjectList(startPos, endPos, elementClass.getSourceFileName());
			Specs specAnnotations = element.getAnnotation(Specs.class);
			
			final String methodDeclaration = getMehodDeclaration(element);
			
			if (specAnnotations.value().length > 0) {
				for (Spec specAnnotation : specAnnotations.value()) {
					SpecObject annotationObject = getSpecObject(specAnnotation, element, startPos, endPos, elementClass.getSourceFileName());
					annotationObject.setMethodDeclaration(methodDeclaration);
					annotationObjectList.addSpecObjects(annotationObject);
				}
	
				annotationObjectList.setMethodDeclaration(methodDeclaration);
				
				putAnnotation(elementClass, Annotation.Spec, annotationObjectList);
			}
			return;
		}
		
		if (Annotation.SpecsStatic.getName().equals(mirror.getAnnotationType().toString())) {	
			SpecObjectList annotationObjectList = new SpecObjectList(startPos, endPos, elementClass.getSourceFileName());
			SpecsStatic specAnnotations = element.getAnnotation(SpecsStatic.class);
			
			final String methodDeclaration = getMehodDeclaration(element);
			
			if (specAnnotations.value().length > 0) {
				for (SpecStatic specAnnotation : specAnnotations.value()) {
					SpecObject annotationObject = getSpecStaticObject(specAnnotation, element, startPos, endPos, elementClass.getSourceFileName());
					annotationObject.setMethodDeclaration(methodDeclaration);
					annotationObjectList.addSpecObjects(annotationObject);
				}
	
				annotationObjectList.setMethodDeclaration(methodDeclaration);
				annotationObjectList.setStat(true);
				
				putAnnotation(elementClass, Annotation.Spec, annotationObjectList);
			}
			return;
		}
		
		if (Annotation.Predicate.getName().equals(mirror.getAnnotationType().toString())) {	
			Predicate predicate = element.getAnnotation(Predicate.class);		
			putAnnotation(elementClass, Annotation.Predicate, getPredicateObject(predicate, startPos, endPos, elementClass.getSourceFileName()));
			return;
		}
		
		if (Annotation.Predicates.getName().equals(mirror.getAnnotationType().toString())) {			
			Predicates predicates = element.getAnnotation(Predicates.class);
			for (Predicate predicate : predicates.value()) {
				putAnnotation(elementClass, Annotation.Predicate, getPredicateObject(predicate, startPos, endPos, elementClass.getSourceFileName()));
			}		
			return;
		}
		
		if (Annotation.InitSpec.getName().equals(mirror.getAnnotationType().toString())) {			
			InitSpec specAnnotation = element.getAnnotation(InitSpec.class);
			putAnnotation(elementClass, Annotation.InitSpec, getInitSpecObject(specAnnotation, startPos, endPos, elementClass.getSourceFileName()));
			return;
		}
		
		if (Annotation.InitSpecStatic.getName().equals(mirror.getAnnotationType().toString())) {			
			InitSpecStatic specAnnotation = element.getAnnotation(InitSpecStatic.class);
			putAnnotation(elementClass, Annotation.InitSpec, getInitSpecStaticObject(specAnnotation, startPos, endPos, elementClass.getSourceFileName()));
			return;
		}
		
		if (Annotation.InitSpecs.getName().equals(mirror.getAnnotationType().toString())) {	
			SpecObjectList annotationObjectList = new SpecObjectList(startPos, endPos, elementClass.getSourceFileName());
			InitSpecs specAnnotations = element.getAnnotation(InitSpecs.class);
			
			if (specAnnotations.value().length > 0) {
				for (InitSpec specAnnotation : specAnnotations.value()) {
					SpecObject annotationObject = getInitSpecObject(specAnnotation, startPos, endPos, elementClass.getSourceFileName());
					annotationObjectList.addSpecObjects(annotationObject);
				}
	
				annotationObjectList.setMethodDeclaration("void <init>()");
				
				putAnnotation(elementClass, Annotation.InitSpec, annotationObjectList);
			}
			return;
		}
		
		if (Annotation.InitSpecsStatic.getName().equals(mirror.getAnnotationType().toString())) {	
			SpecObjectList annotationObjectList = new SpecObjectList(startPos, endPos, elementClass.getSourceFileName());
			InitSpecsStatic specAnnotations = element.getAnnotation(InitSpecsStatic.class);
			
			if (specAnnotations.value().length > 0) {
				for (InitSpecStatic specAnnotation : specAnnotations.value()) {
					SpecObject annotationObject = getInitSpecStaticObject(specAnnotation, startPos, endPos, elementClass.getSourceFileName());
					annotationObjectList.addSpecObjects(annotationObject);
				}
	
				annotationObjectList.setMethodDeclaration("void <init>()");
				annotationObjectList.setStat(true);
				
				putAnnotation(elementClass, Annotation.InitSpec, annotationObjectList);
			}
			return;
		}
	}
	
	private SpecObject getInitSpecObject(InitSpec specAnnotation, long startPos, long endPos, String sourceFileName) {
		SpecObject annotationObject = new SpecObject(startPos, endPos, sourceFileName);
		annotationObject.setPre(specAnnotation.pre());
		annotationObject.setPost(specAnnotation.post());
		annotationObject.setMethodDeclaration("void <init>()");
		
		return annotationObject;
	}
	
	private SpecObject getInitSpecStaticObject(InitSpecStatic specAnnotation, long startPos, long endPos, String sourceFileName) {
		SpecObject annotationObject = new SpecObject(startPos, endPos, sourceFileName);
		annotationObject.setPre(specAnnotation.pre());
		annotationObject.setPost(specAnnotation.post());
		annotationObject.setStat(true);
		annotationObject.setMethodDeclaration("void <init>()");
		
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
		
		return annotationObject;
	}
	
	private SpecObject getSpecStaticObject(SpecStatic specAnnotation, Element element, long startPos, long endPos, String sourceFileName) {
		SpecObject annotationObject = new SpecObject(startPos, endPos, sourceFileName);
		annotationObject.setPre(specAnnotation.pre());
		annotationObject.setPost(specAnnotation.post());					
		annotationObject.setMethodDeclaration(getMehodDeclaration(element));
		annotationObject.setStat(true);
		
		return annotationObject;
	}

	
	public Set<String> getSourceFileNames() {
		Set<String> sourceFileNames = new HashSet<String>();
		
		for (final ClassInformation classInformation : annotations.keySet()) {
			sourceFileNames.add(classInformation.getSourceFileName());
		}
		
		return sourceFileNames;
	}

	public Set<ClassInformation> getClasses(final String sourceFileName) {
		Set<ClassInformation> sourceFileNames = new HashSet<ClassInformation>();
		
		for (final ClassInformation classInformation : annotations.keySet()) {
			if (classInformation.getSourceFileName().equals(sourceFileName)) {
				sourceFileNames.add(classInformation);
			}
		}
		
		return sourceFileNames;
	}
	
}
