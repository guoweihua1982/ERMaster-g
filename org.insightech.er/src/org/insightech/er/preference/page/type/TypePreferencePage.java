package org.insightech.er.preference.page.type;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.insightech.er.ERDiagramActivator;
import org.insightech.er.ResourceString;
import org.insightech.er.Resources;
import org.insightech.er.common.widgets.CompositeFactory;
import org.insightech.er.db.sqltype.SqlTypeFactory;
import org.insightech.er.preference.PreferenceInitializer;
import org.insightech.er.util.io.IOUtils;

public class TypePreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static final String TYPE_DEFAULT = "SqlType.xls";

	private static final String TYPE_SAMPLE = "PostGIS.xls";

	private TypeFileListEditor fileListEditor;

	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Composite buttonComposite = CompositeFactory.createChildComposite(
				composite, 2, 2);
		this.createButtonComposite(buttonComposite);

		CompositeFactory.fillLine(composite,
				Resources.PREFERENCE_PAGE_MARGIN_TOP);

		this.fileListEditor = new TypeFileListEditor(
				PreferenceInitializer.TYPE_FILE_LIST,
				ResourceString.getResourceString("label.custom.type"),
				composite);
		this.fileListEditor.load();

		CompositeFactory.fillLine(composite);

		CompositeFactory.createLabel(composite,
				"dialog.message.type.file.store", 2);

		return composite;
	}

	private void createButtonComposite(Composite composite) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		Button buttonEn = new Button(composite, SWT.NONE);
		buttonEn.setText(ResourceString
				.getResourceString("label.button.download.type.default"));
		buttonEn.addSelectionListener(new SelectionAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				download(TYPE_DEFAULT);
			}
		});

		Button buttonJa = new Button(composite, SWT.NONE);
		buttonJa.setText(ResourceString
				.getResourceString("label.button.download.type.sample"));
		buttonJa.addSelectionListener(new SelectionAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				download(TYPE_SAMPLE);
			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void performDefaults() {
		this.fileListEditor.loadDefault();

		super.performDefaults();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean performOk() {
		this.fileListEditor.store();

		try {
			SqlTypeFactory.loadCustomType();
		} catch ( Exception e) {
			ERDiagramActivator.showExceptionDialog(e);
		}
		return super.performOk();
	}

	private void download(String fileName) {
		String filePath = ERDiagramActivator.showSaveDialog(null, fileName,
				fileName, new String[] { ".xls" }, true);

		if (filePath != null) {
			InputStream in = null;
			OutputStream out = null;
			try {
				in = this.getClass().getResourceAsStream("/" + fileName);
				out = new FileOutputStream(filePath);

				IOUtils.copy(in, out);
			} catch (IOException ioe) {
				ERDiagramActivator.showExceptionDialog(ioe);

			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e1) {
						ERDiagramActivator.showExceptionDialog(e1);
					}

				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e1) {
						ERDiagramActivator.showExceptionDialog(e1);
					}
				}

			}
		}
	}

	public static InputStream getDefaultExcelType() {
		return TypePreferencePage.class.getResourceAsStream("/"
				+ TYPE_DEFAULT);
	}
}
