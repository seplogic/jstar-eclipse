package com.jstar.eclipse.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Predicate {
	String predicate();
	String formula();
	DefinitionType type() default DefinitionType.Define;
}
