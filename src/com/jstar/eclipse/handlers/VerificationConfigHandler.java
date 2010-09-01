package com.jstar.eclipse.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.jstar.eclipse.services.Utils;
import com.jstar.eclipse.services.VerificationService;

public class VerificationConfigHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		VerificationService.getInstance().verifyConfig(Utils.getInstance().getFileFromActiveEditor(null), Utils.getInstance().getActiveWindow().getShell());
		return null;
	}

}
