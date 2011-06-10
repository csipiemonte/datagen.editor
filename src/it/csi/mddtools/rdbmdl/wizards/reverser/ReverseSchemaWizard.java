package it.csi.mddtools.rdbmdl.wizards.reverser;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;

import it.csi.mddtools.rdbmdl.RdbmdlFactory;
import it.csi.mddtools.rdbmdl.Schema;
import it.csi.mddtools.rdbmdl.Table;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;

import java.io.*;

import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "rdbmdl". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

public class ReverseSchemaWizard extends Wizard implements INewWizard {
	private ReverseSchemaWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for ReverseSchemaWizard.
	 */
	public ReverseSchemaWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new ReverseSchemaWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		final String jdbcUrl = page.getJdbcUrl();
		final String schemaName = page.getSchemaName();
		final String username = page.getUsernameText();
		final String password = page.getPasswordText();
		final String dbmsType = page.getDbmsType();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, dbmsType, jdbcUrl, username, password, schemaName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	
	private Schema getModelObject(String dbmsType, String jdbcUrl, String schemaName, String username, String password) throws CoreException{
		RdbmdlFactory fact = RdbmdlFactory.eINSTANCE;
		
		/*
		 * creazione schema da metadati db
		 */
		if (ReverseSchemaWizardPage.ORACLE_DBMS_TYPE.equals(dbmsType)){
//			ReverseSchemaMetaData metaData = new ReverseSchemaMetaData();
//			return metaData.createSchema(fact, jdbcUrl, schemaName, username, password);
			OracleReverser reverser = new OracleReverser();
			return reverser.createSchema(fact, jdbcUrl, schemaName, username, password);
		}
		else if (ReverseSchemaWizardPage.POSTGRES_DBMS_TYPE.equals(dbmsType)){
			PostgresReverser reverser = new PostgresReverser();
			return reverser.createSchema(fact, jdbcUrl, schemaName, username, password);
		}
		else 
			throw new IllegalArgumentException("tipo DB "+dbmsType+" non gestito");
	}
	
	
	
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 * @param schemaName 
	 * @param password 
	 */

	private void doFinish(
		String containerName,
		String fileName,
		String dbmsType,
		String jdbcUrl,
		String username,
		String password, String schemaName, IProgressMonitor monitor)
		throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		
		
		////
		
		// Create a resource set
		//
		ResourceSet resourceSet = new ResourceSetImpl();
		
		IContainer container = (IContainer) resource;
		final IFile modelFile = container.getFile(new Path(fileName));
		
		// Get the URI of the model file.
		//
		URI fileURI = URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true);

		// Create a resource for this file.
		//
		Resource emfResource = resourceSet.createResource(fileURI);

		// Add the initial model object to the contents.
		//
		EObject rootObject = getModelObject(dbmsType, jdbcUrl, schemaName, username, password);
		if (rootObject != null) {
			emfResource.getContents().add(rootObject);
		}

		// Save the contents of the resource to the file system.
		//
		Map<Object, Object> options = new HashMap<Object, Object>();
		options.put(XMLResource.OPTION_ENCODING, "UTF-8");
		try {
			emfResource.save(options);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		////
		
	
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, modelFile, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}
	
	/**
	 * We will initialize file contents with a sample text.
	 */

	private InputStream openContentStream() {
		String contents =
			"This is the initial file contents for *.rdbmdl file that should be word-sorted in the Preview page of the multi-page editor";
		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "datagen.editor", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}