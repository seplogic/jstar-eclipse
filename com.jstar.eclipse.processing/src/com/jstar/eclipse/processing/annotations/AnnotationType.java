package com.jstar.eclipse.processing.annotations;

public enum AnnotationType {
	Import ("com.jstar.eclipse.annotations.Import"),
	Specs ("com.jstar.eclipse.annotations.Specs"),
	Spec ("com.jstar.eclipse.annotations.Spec"),
	SpecsStatic ("com.jstar.eclipse.annotations.SpecsStatic"),
	SpecStatic ("com.jstar.eclipse.annotations.SpecStatic"),
	InitSpec ("com.jstar.eclipse.annotations.InitSpec"),
	InitSpecs ("com.jstar.eclipse.annotations.InitSpecs"),
	Predicate ("com.jstar.eclipse.annotations.Predicate"),
	Predicates ("com.jstar.eclipse.annotations.Predicates"),
	InitSpecStatic ("com.jstar.eclipse.annotations.InitSpecStatic"),
	InitSpecsStatic ("com.jstar.eclipse.annotations.InitSpecsStatic");
	
	private String name;
	
	AnnotationType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
