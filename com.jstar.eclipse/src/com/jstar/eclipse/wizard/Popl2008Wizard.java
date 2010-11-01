package com.jstar.eclipse.wizard;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.jstar.eclipse.Activator;
import com.jstar.eclipse.objects.JavaFilePersistentProperties;
import com.jstar.eclipse.objects.JavaProject;
import com.jstar.eclipse.services.ConsoleService;
import com.jstar.eclipse.services.Utils;

public class Popl2008Wizard extends Wizard implements INewWizard {
	
	private static final String POPL2008 = "com.jstar.eclipse.example.popl2008";
	private JStarExampleWizardMainPage mainPage;

	public Popl2008Wizard() {
		super();
	}

	@Override
	public boolean performFinish() {
		try {
			final URL examples = FileLocator.find(Activator.getDefault().getBundle(), new Path(Activator.POPL2008_EXAMPLE), null);
			String examplesLocation = "";
			
			try {
				examplesLocation = FileLocator.toFileURL(examples).getFile();
			} 
			catch (IOException ioe) {
				ConsoleService.getInstance().printErrorMessage("Cannot obtain the location of examples.");
				ioe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
				throw new RuntimeException();
			}
		
			importExisitingProject(new Path(examplesLocation));
		} 
		catch (CoreException ce) {
			ce.printStackTrace(ConsoleService.getInstance().getConsoleStream());
		}

		return true;
	}

	@Override
	public void addPages() {
		mainPage = new JStarExampleWizardMainPage("POPL2008 Example", "Create a simple Cell/Recell/DCell example.");
		mainPage.setTitle("jStar Example");
		mainPage.setDescription("Create the POPL2008 Example.");
		addPage(mainPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	private boolean importExisitingProject(IPath projectPath) throws CoreException {

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final File existingProject = workspace.getRoot().getLocation().append(POPL2008).toFile();
		
		if (existingProject.exists()) {
			Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "The worksace already has a folder named " + POPL2008 + ".", null);
        	ErrorDialog.openError(Utils.getInstance().getActiveWindow().getShell(), "jStar", "Cannot import project " + POPL2008, status);
        	
			return false;
		}
		
		final File projectExample = projectPath.append(new Path(POPL2008)).toFile();
		
		try {
			FileUtils.copyDirectory(projectExample, workspace.getRoot().getLocation().append(POPL2008).toFile());
		} 
		catch (IOException ioe) {
			ioe.printStackTrace(ConsoleService.getInstance().getConsoleStream());
			return false;
		}
		
		final IProjectDescription description = workspace.loadProjectDescription(workspace.getRoot().getLocation().append(POPL2008).append(IProjectDescription.DESCRIPTION_FILE_NAME));
		final IProject project = workspace.getRoot().getProject(description.getName());

		if (project.exists()) {
			Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "The worksace already has a project named " + project.getName() + ".", null);
        	ErrorDialog.openError(Utils.getInstance().getActiveWindow().getShell(), "jStar", "Cannot import project " + project.getName(), status);
        	
			return false;
		}
		
		final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(description, monitor);
				project.open(monitor);
			}
		};
		
		workspace.run(runnable, workspace.getRuleFactory().modifyRule(workspace.getRoot()), IWorkspace.AVOID_UPDATE, null);
		
		final JavaProject javaProject = new JavaProject(JavaCore.create(project));
		JavaFilePersistentProperties.setJStarRootFolder(javaProject, "jStar");
		
		final IFile readme = project.getFile("readme.html");
		Utils.getInstance().openFileInEditor(readme, Utils.BROWSER_EDITOR, true);
		
		return true;
	}
}
