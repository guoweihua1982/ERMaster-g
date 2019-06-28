package org.insightech.er.editor.view.dialog.option.tab;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.PlatformUI;
import org.insightech.er.ResourceString;
import org.insightech.er.common.dialog.ValidatableTabWrapper;
import org.insightech.er.common.exception.InputException;
import org.insightech.er.common.widgets.CompositeFactory;
import org.insightech.er.db.DBManagerFactory;
import org.insightech.er.editor.model.settings.Settings;
import org.insightech.er.editor.view.dialog.option.OptionSettingDialog;
import org.insightech.er.preference.PreferenceInitializer;

public class DBSelectTabWrapper extends ValidatableTabWrapper {

	private Combo databaseCombo;

	private List customTypeList;

	private Settings settings;

	public DBSelectTabWrapper(OptionSettingDialog dialog, TabFolder parent,
			Settings settings) {
		super(dialog, parent, "label.database");

		this.settings = settings;
	}

	@Override
	protected void initLayout(GridLayout layout) {
		super.initLayout(layout);
		layout.numColumns = 2;
	}

	@Override
	public void initComposite() {

		this.databaseCombo = CompositeFactory.createReadOnlyCombo(null, this,
				"label.database");
		this.databaseCombo.setVisibleItemCount(10);

		for (String db : DBManagerFactory.getAllDBList()) {
			this.databaseCombo.add(db);
		}

		this.databaseCombo.setFocus();

		this.customTypeList = CompositeFactory.createList(null, this, "label.custom.type", 10);
		for (String type : PreferenceInitializer.getAllExcelTypeFiles()) {
			this.customTypeList.add(type);
		}
	}

	@Override
	protected void addListener() {
		super.addListener();

		this.databaseCombo.addSelectionListener(new SelectionAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				changeDatabase();
			}
		});
	}

	@Override
	public void setData() {
		for (int i = 0; i < this.databaseCombo.getItemCount(); i++) {
			String database = this.databaseCombo.getItem(i);
			if (database.equals(this.settings.getDatabase())) {
				this.databaseCombo.select(i);
				break;
			}
		}

		String[] customTypes = this.settings.getCustomTypes();
		if (customTypes != null) {
			java.util.List<String> typeList = Arrays.asList(customTypes);
			for (int i = 0; i < this.customTypeList.getItemCount(); i++) {
				String customType = this.customTypeList.getItem(i);
				if(typeList.contains(customType)) {
					this.customTypeList.select(i);
				}
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validatePage() throws InputException {
		this.settings.setDatabase(this.databaseCombo.getText());
	}

	private void changeDatabase() {
		MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), SWT.ICON_QUESTION
				| SWT.OK | SWT.CANCEL);
		messageBox.setText(ResourceString
				.getResourceString("dialog.title.change.database"));
		messageBox.setMessage(ResourceString
				.getResourceString("dialog.message.change.database"));

		if (messageBox.open() == SWT.OK) {
			String database = this.databaseCombo.getText();
			this.settings.setDatabase(database);

			this.dialog.resetTabs();

		} else {
			this.setData();
		}
	}

	@Override
	public void setInitFocus() {
		this.databaseCombo.setFocus();
	}

	@Override
	public void perfomeOK() {
		String[] newTypes = this.customTypeList.getSelection();
		this.settings.setCustomTypes(newTypes);
	}
}
