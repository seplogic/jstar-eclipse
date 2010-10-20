package com.jstar.eclipse.exceptions;

import org.eclipse.core.resources.IResource;

public class RequiredResourceNotFoundException extends Exception {

	private static final long serialVersionUID = -5701658254563277732L;
	
	private IResource resource;

	public RequiredResourceNotFoundException(final IResource resource) {
		super();
		this.resource = resource;
	}
	
	public IResource getResource() {
		return resource;
	}
}
