/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.processing;

import java.io.IOException;
import java.io.Writer;
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

import com.jstar.eclipse.processing.annotations.AllAnnotations;
import com.jstar.eclipse.processing.annotations.FileAnnotations;
import com.jstar.eclipse.processing.annotations.objects.ImportObject;
import com.sun.source.tree.Tree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

@SuppressWarnings("restriction")
@SupportedAnnotationTypes("com.jstar.eclipse.annotations.*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)

public class SpecAnnotationProcessor extends AbstractProcessor {

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		
		Messager messager = processingEnv.getMessager();
		
		AllAnnotations allAnnotations = new AllAnnotations();
		
		for (TypeElement typeElement : annotations){
			for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {				
				processAnnotation(element, typeElement, allAnnotations, messager);			
			}			
        }
		
		if (!allAnnotations.isEmpty()) {
			try {
				generateSpec(processingEnv.getFiler(), allAnnotations);
				generateImports(processingEnv.getFiler(), allAnnotations);
			}
			catch(Exception exc) {
				messager.printMessage(Diagnostic.Kind.ERROR, exc.getMessage());
			}
		}
		
        return true;
	}

	private void generateSpec(Filer filer, AllAnnotations allAnnotations) throws IOException {		
		for (final FileAnnotations fileAnnotations : allAnnotations.getFileAnnotations()) {		
		   FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "",  fileAnnotations.getBaseSourceFileName() + ".spec");

		   Writer writer = fileObject.openWriter();	   
		   AnnotationGeneratorService.getInstance().generateFile(fileAnnotations, writer);
     
		   writer.flush();
		   writer.close();
		}
		
	}
	
	private void generateImports(final Filer filer, final AllAnnotations allAnnotations) throws IOException {		
		for (final FileAnnotations fileAnnotations : allAnnotations.getFileAnnotations()) {	
		   final List<ImportObject> importObjects = fileAnnotations.getImportAnnotations();
		   final FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "",  fileAnnotations.getBaseSourceFileName() + ".imports");
		   final Writer writer = fileObject.openWriter();	
		   
		   for (final ImportObject object : importObjects) {
			   object.writeImports(writer);
		   }
     
		   writer.flush();
		   writer.close();
		}
	}

	private void processAnnotation(Element element, TypeElement typeElement,
	    AllAnnotations allAnnotations, Messager messager) {
		
		List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();

		for (AnnotationMirror mirror : annotations) {
			final String mirrorAnnotationType = mirror.getAnnotationType().toString();
			
			if (typeElement.toString().equals(mirrorAnnotationType)) {
				Trees instance = Trees.instance(processingEnv);
				SourcePositions sourcePositions = instance.getSourcePositions();	
				
				TreePath path = instance.getPath(element, mirror);
				
				CompilationUnitTree cu = path.getCompilationUnit();
				Tree tree =  path.getLeaf();
					
				Element elementClass;
				
				if (element.getKind().isClass() || element.getKind().isInterface()) {
					elementClass = element;
				}
				else {
					elementClass = element.getEnclosingElement();
				}
				
				int nameLenght = mirror.getAnnotationType().asElement().getSimpleName().length() + 1;
														
				allAnnotations.addAnnotation(cu.getSourceFile().toUri().getPath(), elementClass, element, mirror, sourcePositions.getStartPosition(cu, tree) + 1, sourcePositions.getEndPosition(cu, tree) + nameLenght);
			}
		}
	}

}
