/*
 * This file is part of jStar Eclipse Plug-in.
 * 
 * jStar Eclipse Plug-in is distributed under a BSD license,  see, LICENSE
 */
package com.jstar.eclipse.listeners;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;

import com.jstar.eclipse.services.Utils;
import com.jstar.eclipse.services.VerificationService;

public class SaveListener implements IExecutionListener {

	@Override
	public void notHandled(String commandId, NotHandledException exception) {	
	}

	@Override
	public void postExecuteFailure(String commandId, ExecutionException exception) {
	}

	@Override
	public void postExecuteSuccess(String commandId, Object returnValue) {
		if (commandId.equals("org.eclipse.ui.file.save")) {	
			VerificationService.getInstance().verify(Utils.getInstance().getFileFromActiveEditor(null), Utils.getInstance().getActiveWindow().getShell());
		}		
	}

	@Override
	public void preExecute(String commandId, ExecutionEvent event) {	
	}

}
