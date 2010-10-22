package com.jstar.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.jstar.eclipse.services.ResourceService;
import com.jstar.eclipse.services.Utils;

public class ExternalClassSpecHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ResourceService.getInstance().addExternalClassSpec(Utils.getInstance().getJavaFileFromActiveEditor(null).getJavaProject());
		return null;
	}

}
