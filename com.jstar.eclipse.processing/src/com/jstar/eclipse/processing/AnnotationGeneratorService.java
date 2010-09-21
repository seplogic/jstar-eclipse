package com.jstar.eclipse.processing;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.lang.model.element.Element;

import com.jstar.eclipse.processing.annotations.AnnotationType;
import com.jstar.eclipse.processing.annotations.ClassAnnotations;
import com.jstar.eclipse.processing.annotations.FileAnnotations;
import com.jstar.eclipse.processing.annotations.objects.AnnotationObject;
import com.jstar.eclipse.processing.annotations.objects.ImportObject;

public class AnnotationGeneratorService {
	
	public static AnnotationGeneratorService instance;
	
	private AnnotationGeneratorService() {
	}
	
	public static AnnotationGeneratorService getInstance() {
		if (instance == null) {
			instance = new AnnotationGeneratorService();
		}
		
		return instance;
	}
	
	public void generateFile(final FileAnnotations fileAnnotations, final Writer writer) throws IOException {
		List<ImportObject> importObjects = fileAnnotations.getImportAnnotations();
			
		for (ImportObject importObject : importObjects) {
			importObject.generateFile(writer);
		}
		
		for (ClassAnnotations classAnnotations : fileAnnotations.getClassAnnotations()) {
			final Element className = classAnnotations.getClassName();
			
			if (className.getKind().isClass() || className.getKind().isInterface()) {
				addClassDeclaration(className, writer);
				writer.write("\n");
			}
			else {
				throw new RuntimeException("Unknown kind of element. Currently only class and interface are supported."); 
			}
						
			for (AnnotationObject predicate : classAnnotations.getAnnotations(AnnotationType.Predicate)) {
				predicate.generateFile(writer);
				writer.write("\n");
			}
			
			
			for (AnnotationObject initSpec : classAnnotations.getAnnotations(AnnotationType.InitSpec)) {
				initSpec.generateFile(writer);
				writer.write("\n");
			}
			
			for (AnnotationObject spec : classAnnotations.getAnnotations(AnnotationType.Spec)) {
				spec.generateFile(writer);
				writer.write("\n");
			}
		
			writer.write("}\n");
			
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
}
