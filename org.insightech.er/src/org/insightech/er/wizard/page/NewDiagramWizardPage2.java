package org.insightech.er.wizard.page;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.insightech.er.ResourceString;
import org.insightech.er.db.DBManagerFactory;
import org.insightech.er.preference.PreferenceInitializer;

public class NewDiagramWizardPage2 extends WizardPage {

	private Combo databaseCombo;

	private List customTypeList;

	public NewDiagramWizardPage2(IStructuredSelection selection) {
		super(ResourceString.getResourceString("wizard.new.diagram.title"));
		this.setTitle(ResourceString
				.getResourceString("wizard.new.diagram.title"));
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.NULL);
		label.setText(ResourceString.getResourceString("label.database"));

		this.databaseCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		GridData dbData = new GridData(GridData.FILL_HORIZONTAL);
		dbData.widthHint = 200;
		this.databaseCombo.setLayoutData(dbData);
		this.databaseCombo.setVisibleItemCount(10);

		for (String db : DBManagerFactory.getAllDBList()) {
			this.databaseCombo.add(db);
		}

		this.databaseCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});

		this.databaseCombo.setFocus();

		Label customTypeLabel = new Label(composite, SWT.NONE);
		GridData labelGridData = new GridData();
		labelGridData.verticalAlignment = SWT.TOP;
		labelGridData.horizontalAlignment = SWT.LEFT;
		customTypeLabel.setLayoutData(labelGridData);
		customTypeLabel.setText(ResourceString.getResourceString("label.custom.type"));
		this.customTypeList = new List(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		GridData typeData = new GridData(GridData.FILL_HORIZONTAL);
		typeData.widthHint = 200;
		typeData.heightHint = 10 * this.customTypeList.getItemHeight();
		this.customTypeList.setLayoutData(typeData);

		for (String type:PreferenceInitializer.getAllExcelTypeFiles()) {
			this.customTypeList.add(type);
		}

		this.validatePage();

		this.setControl(composite);
	}

	protected boolean validatePage() {
		boolean valid = true;

		if (this.databaseCombo.getText().length() == 0) {
			setMessage(ResourceString
					.getResourceString("select.database.message"));
			valid = false;

			this.setPageComplete(false);
		}

		if (valid) {
			this.setPageComplete(true);

			setMessage(ResourceString
					.getResourceString("wizard.new.diagram.message"));
		}

		return valid;
	}

	public String getDatabase() {
		return this.databaseCombo.getText();
	}

	public String[] getCustomTypes() {
		return this.customTypeList.getSelection();
	}
}
