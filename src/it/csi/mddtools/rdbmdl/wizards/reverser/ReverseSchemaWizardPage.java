package it.csi.mddtools.rdbmdl.wizards.reverser;

import java.sql.Connection;
import java.sql.SQLException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (rdbmdl).
 */

public class ReverseSchemaWizardPage extends WizardPage {
	private Text containerText;

	private Text fileText;
	
	private Combo dbmsType;
	
	private Text jdbcUrl;

	private Combo schemaNameCombo;
	
	private ISelection selection;
	
	private Text usernameText;
	private Text passwordText;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public ReverseSchemaWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("Multi-page Editor File");
		setDescription("This wizard creates a new file with *.rdbmdl extension that can be opened by a multi-page editor.");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Directory:");

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		label = new Label(container, SWT.NULL);
		label.setText("");
	
		
		label = new Label(container, SWT.NULL);
		label.setText("&Dbms:");

		dbmsType = new Combo(container, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		dbmsType.setLayoutData(gd);
		dbmsType.setItems(new String[]{ORACLE_DBMS_TYPE, POSTGRES_DBMS_TYPE});
		dbmsType.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				initJdbcUrlTemplate(dbmsType.getText());
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				initJdbcUrlTemplate(dbmsType.getText());
			}
			
			private void initJdbcUrlTemplate(String dbmsTypeName){
				if ("ORACLE".equals(dbmsTypeName))
					jdbcUrl.setText("jdbc:oracle:thin:@[server]:[port]:[sid]");
				else if ("POSTGRESQL".equals(dbmsTypeName))
					jdbcUrl.setText(" jdbc:postgresql://[host]:[port]/[database]?user=[userName]&password=[pass]");
			}
		});
		
		
		label = new Label(container, SWT.NULL);
		label.setText("");
		
		label = new Label(container, SWT.NULL);
		label.setText("&Jdbc url:");

		jdbcUrl = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		jdbcUrl.setLayoutData(gd);
		jdbcUrl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText("");
		
		label = new Label(container, SWT.NULL);
		label.setText("&Username:");

		usernameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		usernameText.setLayoutData(gd);
		usernameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText("");
		
		label = new Label(container, SWT.NULL);
		label.setText("&Password:");

		passwordText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		passwordText.setLayoutData(gd);
		passwordText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText("");
		
		label = new Label(container, SWT.NULL);
		label.setText("&Connect and load schemas");
		
		Button connectBtn = new Button(container, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		connectBtn.setLayoutData(gd);
		connectBtn.setText("Connect!");
		connectBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				try{
					connectAndFill();
				}
				catch(SQLException se){
					updateStatus("Connection error:"+se);
				}
				
			}

		});
		
		
		label = new Label(container, SWT.NULL);
		label.setText("");
		
		label = new Label(container, SWT.NULL);
		label.setText("&Schema name:");

		schemaNameCombo = new Combo(container, SWT.NULL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		schemaNameCombo.setLayoutData(gd);
		schemaNameCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
		});

		
		initialize();
		dialogChanged();
		setControl(container);
	}

	public final static String ORACLE_DBMS_TYPE = "ORACLE";
	public final static String POSTGRES_DBMS_TYPE = "POSTGRES";
	
	void connectAndFill()throws SQLException{
		String dbmsTypeName = getDbmsType();
		AbstractReverser reverser = null;
		if (dbmsTypeName != null){
			if(dbmsTypeName.equals(ORACLE_DBMS_TYPE)){
				reverser = new OracleReverser();
			}
			else if(dbmsTypeName.equals(POSTGRES_DBMS_TYPE)){
				reverser = new PostgresReverser();
			} 
			if (reverser != null){
				schemaNameCombo.setItems(reverser.getAllSchemaNames(getJdbcUrl(), getUsernameText(), getPasswordText()));			
			}
			dialogChanged();
		}
	
	}
	
	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				containerText.setText(container.getFullPath().toString());
			}
		}
		fileText.setText("my.rdbmdl");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()));
		String fileName = getFileName();

		if (getContainerName().length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("File container must exist");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			if (ext.equalsIgnoreCase("rdbmdl") == false) {
				updateStatus("File extension must be \"rdbmdl\"");
				return;
			}
		}
		if (getDbmsType() == null || getDbmsType().length()==0){
			updateStatus("DMBS type must be specified");
			return;
		}
		if (getJdbcUrl().length() == 0) {
			updateStatus("JDBC connection url must be specified");
			return;
		}
		if (schemaNameCombo.getItemCount()>0 && getSchemaName().length() == 0) {
			updateStatus("Schema name must be specified");
			return;
		}
		if (getUsernameText().length() == 0) {
			updateStatus("Username must be specified");
			return;
		}
		if (getPasswordText().length() == 0) {
			updateStatus("Password must be specified");
			return;
		}
		
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getFileName() {
		return fileText.getText();
	}

	public String getDbmsType(){
		return dbmsType.getText();
	}
	
	public String getJdbcUrl() {
		return jdbcUrl.getText();
	}

	public String getSchemaName() {
		if (schemaNameCombo.getSelectionIndex()!=-1)
			return schemaNameCombo.getItem(schemaNameCombo.getSelectionIndex());
		else
			return "";
	}

	public String getUsernameText() {
		return usernameText.getText();
	}

	public String getPasswordText() {
		return passwordText.getText();
	}	
	
}