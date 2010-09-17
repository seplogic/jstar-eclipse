package com.jstar.eclipse.processing;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.sun.source.tree.Tree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

@SuppressWarnings("restriction")
@SupportedAnnotationTypes("com.jstar.eclipse.annotations.*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)

public class SpecAnnotationProcessor extends AbstractProcessor {
	
	private String className;

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		
		Messager messager = processingEnv.getMessager();
		
		Set<? extends Element> elements = roundEnv.getRootElements();
		
		List<Element> classNames = new LinkedList<Element>();
		for (Element element : elements) {
			if (element.getKind().isClass() || element.getKind().isInterface()) {
				System.out.println(element.getSimpleName().toString());
				classNames.add(element);   
			}
		}
		
		AllAnnotations allAnnotations = new AllAnnotations(classNames);
		
		for (TypeElement typeElement : annotations){

			for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
				className = getSourceFile(element);				
				allAnnotations.setFileName(className);
				
				processAnnotation(element, typeElement, allAnnotations, messager);			
			}			
        }
		
		if (!allAnnotations.isEmpty()) {
			try {
				generateSpecProcessor(processingEnv.getFiler(), allAnnotations);
			}
			catch(Exception exc) {
				messager.printMessage(Diagnostic.Kind.ERROR, exc.getMessage());
			}
		}
		
        return true;
	}

	private String getSourceFile(Element element) { //Assuming we write only one file name 
		if (className == null) {
			Trees instance = Trees.instance(processingEnv);
			TreePath path = instance.getPath(element);
			
			CompilationUnitTree cu = path.getCompilationUnit();
			
			return getFileName(cu.getSourceFile().getName()); 
		}
		return className;
	}
	
	public String getFileName(final String name) {
		int separator = name.lastIndexOf(File.separator);
		
		if (separator == -1) {
			separator = 0;
		}
		
		int dot = name.lastIndexOf('.');
		return name.substring(separator, dot);
	}

	private void generateSpecProcessor(Filer filer, AllAnnotations allAnnotations) throws IOException {			
		   FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "",  className + ".spec");

		   Writer writer = fileObject.openWriter();	   
		   allAnnotations.generateFile(writer);
     
		   writer.flush();
		   writer.close();
		
	}

	private void processAnnotation(Element element, TypeElement typeElement,
	    AllAnnotations allAnnotations, Messager messager) {
		
		List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();

		for (AnnotationMirror mirror : annotations) {
			System.out.println(mirror.getAnnotationType().toString());
			final String mirrorAnnotationType = mirror.getAnnotationType().toString();
			
			if (typeElement.toString().equals(mirrorAnnotationType)) {
				Trees instance = Trees.instance(processingEnv);
				SourcePositions sourcePositions = instance.getSourcePositions();	
				
				TreePath path = instance.getPath(element, mirror);
				
				CompilationUnitTree cu = path.getCompilationUnit();
				Tree tree =  path.getLeaf();
					
				String elementClass;
				if (element.getKind().isClass() || element.getKind().isInterface()) {
					elementClass = element.getSimpleName().toString();
				}
				else {
					elementClass = element.getEnclosingElement().getSimpleName().toString();
				}
				
				int nameLenght = mirror.getAnnotationType().asElement().getSimpleName().length() + 1;
				
				System.out.println("Element:" + element.getSimpleName() + " Annotation: " + mirrorAnnotationType + " Positions: " + (sourcePositions.getStartPosition(cu, tree) + 1) + " " + (sourcePositions.getEndPosition(cu, tree) + nameLenght));
						
				allAnnotations.addAnnotation(element, mirror, sourcePositions.getStartPosition(cu, tree) + 1, sourcePositions.getEndPosition(cu, tree) + nameLenght, elementClass);
			}
		}
	}

}
