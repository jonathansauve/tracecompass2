/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jonathan Sauvé - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.xmlManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.Activator;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.ui.module.XmlAnalysisModuleSource;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectModelElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Main viewer to display the XML analysis manager view.
 *
 * @author Jonathan Sauvé
 *
 */
public class XmlAnalysisManagerViewer {

    /** The parent's composite */
    private Composite fParent;

    /** Other composites and controls */
    private Composite fXmlFilesAndActions;

    private Tree fXmlFilesTree;

    private Composite fActionsComposite;
    private Button fImportXmlFile;
    private Button fExportXmlFile;
    private Button fRemoveXmlFile;
    private Button fEditFile;

    /** Variables for the XmlFile */

    /** Xml file folder and files */
    private static File XML_FOLDER = XmlUtils.getXmlFilesPath().toFile();
    private File[] fXmlFiles = XML_FOLDER.listFiles();

    /**
     * Public constructor
     *
     * @param parent
     *            The parent's composite
     */
    public XmlAnalysisManagerViewer(Composite parent) {
        fParent = parent;

        createContents();
        addListeners();
    }

    private void createContents() {
        fXmlFilesAndActions = new Composite(fParent, SWT.NONE);
        fXmlFilesAndActions.setLayout(XmlAnalysisManagerUtils.createGridLayout(2, 0, 0));

        fXmlFilesTree = new Tree(fXmlFilesAndActions, SWT.V_SCROLL | SWT.H_SCROLL);
        for (int i = 0; i < fXmlFiles.length; i++) {
            TreeItem xmlFileItem = new TreeItem(fXmlFilesTree, SWT.NONE);
            xmlFileItem.setText(fXmlFiles[i].getName());
            xmlFileItem.setData(Messages.FileKey, fXmlFiles[i]);
        }
        fXmlFilesTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        fActionsComposite = new Composite(fXmlFilesAndActions, SWT.NONE);
        fActionsComposite.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 0, 5));
        fActionsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

        fImportXmlFile = new Button(fActionsComposite, SWT.PUSH);
        fImportXmlFile.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
        fImportXmlFile.setText(Messages.XmlManagerViewer_importXmlFileText);

        fExportXmlFile = new Button(fActionsComposite, SWT.PUSH);
        fExportXmlFile.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
        fExportXmlFile.setText(Messages.XmlManagerViewer_exportXmlFileText);

        fRemoveXmlFile = new Button(fActionsComposite, SWT.PUSH);
        fRemoveXmlFile.setText(Messages.XmlManagerViewer_removeXmlFileText);
        fRemoveXmlFile.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));

        fEditFile = new Button(fActionsComposite, SWT.PUSH);
        fEditFile.setText(Messages.XmlManagerViewer_editFileText);
        fEditFile.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
    }

    private void addListeners() {
        Listener xmlFilesTreeListener = new Listener() {

            @Override
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.Modify:
                    ModifyData data = (ModifyData) event.data;
                    boolean toDelete = data.getToDelete();
                    File xmlFile = data.getXmlFile();
                    if (!toDelete) {
                        File runtimeXmlFile = getRuntimeXmlFile(xmlFile);
                        if (runtimeXmlFile == null) {
                            return;
                        }
                        TreeItem newItem = new TreeItem(fXmlFilesTree, SWT.NONE);
                        newItem.setText(xmlFile.getName());
                        newItem.setData(Messages.FileKey, runtimeXmlFile);
                    } else {
                        int index = -1;
                        TreeItem[] children = fXmlFilesTree.getItems();
                        for (int i = 0; i < children.length; i++) {
                            if (children[i].getText().equals(xmlFile.getName())) {
                                index = i;
                                break;
                            }
                        }
                        IStatus status = XmlUtils.removeXmlFile(xmlFile);
                        if (status.isOK() && index != -1) {
                            children[index].dispose();
                        }
                    }
                    break;
                case SWT.Selection:
                    Event newEvent = new Event();
                    if (event.detail == SWT.CHECK) {
                        newEvent.widget = fRemoveXmlFile;
                        fRemoveXmlFile.notifyListeners(SWT.Activate, event);
                    }
                    newEvent.widget = fEditFile;
                    fEditFile.notifyListeners(SWT.Activate, newEvent);
                    break;
                default:
                    break;
                }
            }
        };
        fXmlFilesTree.addListener(SWT.Modify, xmlFilesTreeListener);
        fXmlFilesTree.addListener(SWT.Selection, xmlFilesTreeListener);

        fImportXmlFile.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(fParent.getShell());
                dialog.setText(Messages.XmlManagerViewer_importFileDialogText);
                String[] extensions = { Messages.XmlManagerViewer_xmlExtension };
                String[] extNames = { Messages.XmlManagerViewer_xmlExtensionName };
                dialog.setFilterExtensions(extensions);
                dialog.setFilterNames(extNames);

                String filePath = dialog.open();
                if (filePath != null) {
                    File xml = new File(filePath);
                    /* Check if the file is already active */
                    File xmlFilesFolder = XmlUtils.getXmlFilesPath().toFile();
                    for (File activeFile : xmlFilesFolder.listFiles()) {
                        if (activeFile.getName().equals(xml.getName())) {
                            ErrorDialog.openError(fParent.getShell(), Messages.XmlManagerViewer_importErrorTitle, Messages.XmlManagerViewer_importErrorMessage,
                                    new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.XmlManagerViewer_importErrorSameFile));
                            return;
                        }
                    }
                    IStatus status = XmlUtils.xmlValidate(xml);
                    if (status.isOK()) {
                        IStatus addStatus = XmlUtils.addXmlFile(xml);
                        if (!addStatus.isOK()) {
                            ErrorDialog.openError(fParent.getShell(), Messages.XmlManagerViewer_importErrorTitle, Messages.XmlManagerViewer_importErrorMessage, addStatus);
                        } else {
                            XmlAnalysisModuleSource.notifyModuleChange();
                            /*
                             * FIXME: It refreshes the list of analysis under a
                             * trace, but since modules are instantiated when
                             * the trace opens, the changes won't apply to an
                             * opened trace, it needs to be closed then reopened
                             */
                            refreshProject();
                            Event newFile = new Event();
                            File xmlFile = getRuntimeXmlFile(xml);
                            if (xmlFile == null) {
                                return;
                            }
                            newFile.data = new ModifyData(false, xmlFile);
                            fXmlFilesTree.notifyListeners(SWT.Modify, newFile);
                        }
                    } else {
                        ErrorDialog.openError(fParent.getShell(), Messages.XmlManagerViewer_importErrorTitle, Messages.XmlManagerViewer_importErrorInvalidFile, status);
                    }
                }
            }

            private void refreshProject() {
                // Check if we are closing down
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (window == null) {
                    return;
                }

                // Get the selection
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                IWorkbenchPart part = page.getActivePart();
                if (part == null) {
                    return;
                }
                ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
                if (selectionProvider == null) {
                    return;
                }
                ISelection selection = selectionProvider.getSelection();

                if (selection instanceof TreeSelection) {
                    TreeSelection sel = (TreeSelection) selection;
                    /* There should be only one item selected as per the plugin.xml */
                    Object element = sel.getFirstElement();
                    if (element instanceof TmfProjectModelElement) {
                        ((TmfProjectModelElement) element).getProject().refresh();
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        fExportXmlFile.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] files = fXmlFilesTree.getSelection();
                if (files.length == 0) {
                    return;
                }
                /* single selection, always the first one */
                File selectedFile = (File) files[0].getData(Messages.FileKey);

                FileDialog saveDialog = new FileDialog(fParent.getShell(), SWT.SAVE);
                saveDialog.setFilterNames(new String[] { Messages.XmlManagerViewer_xmlFilterName, Messages.XmlManagerViewer_allFilesFilterName });
                saveDialog.setFilterExtensions(new String[] { Messages.XmlManagerViewer_xmlExtension, Messages.XmlManagerViewer_allFilesExtension });
                saveDialog.setFileName(selectedFile.getName());

                String savePath = saveDialog.open();
                if (savePath != null) {
                    try {
                        Files.copy(selectedFile.toPath(), new File(savePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e1) {
                        Activator.logError(e1.getMessage(), e1);
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        fRemoveXmlFile.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] files = fXmlFilesTree.getSelection();
                for (int i = 0; i < files.length; i++) {
                    MessageBox messageBox = new MessageBox(fXmlFilesTree.getShell(), SWT.ICON_QUESTION
                            | SWT.YES | SWT.NO);
                    messageBox.setMessage(Messages.XmlManagerViewer_removingXmlAnalysisMessage);
                    messageBox.setText(Messages.XmlManagerViewer_removingXmlAnalysisText + " " + files[i].getText()); //$NON-NLS-1$
                    int response = messageBox.open();
                    if (response == SWT.YES) {
                        /* Notify the tree to delete this item, and the associate file */
                        File file = (File) files[i].getData(Messages.FileKey);
                        if (file != null) {
                            Event newEvent = new Event();
                            newEvent.data = new ModifyData(true, file);
                            fXmlFilesTree.notifyListeners(SWT.Modify, newEvent);
                        }
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        fEditFile.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] selection = fXmlFilesTree.getSelection();
                if (selection.length != 0) {
                    XmlFilePropertiesDialog pv = new XmlFilePropertiesDialog(fParent.getShell(), (File) selection[0].getData(Messages.FileKey));
                    pv.open();
                    update();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    /**
     * This function update the files associated with the widgets in the tree.
     */
    private void update() {
        TreeItem[] items = fXmlFilesTree.getItems();
        for (int i = 0; i < items.length; i++) {
            TreeItem item = items[i];
            for (File activeFile : XML_FOLDER.listFiles()) {
                if (item.getText().equals(activeFile.getName())) {
                    item.setData(Messages.FileKey, activeFile);
                    break;
                }
            }
        }
    }

    private File getRuntimeXmlFile(File xmlFile) {
        fXmlFiles = XML_FOLDER.listFiles();

        if (fXmlFiles != null) {
            for (int i = 0; i < fXmlFiles.length; i++) {
                if (fXmlFiles[i].getName().equals(xmlFile.getName())) {
                    return fXmlFiles[i];
                }
            }
        }
        return null;
    }

    private class ModifyData {
        final boolean fToDelete;
        final File fXmlFile;

        public ModifyData(boolean toDelete, File xmlFile) {
            this.fToDelete = toDelete;
            this.fXmlFile = xmlFile;
        }

        /**
         * Getter for the toDelete attribute
         *
         * @return The toDelete attribute
         */
        public boolean getToDelete() {
            return fToDelete;
        }

        /**
         * Getter for the xmlFile
         *
         * @return The xml file
         */
        public File getXmlFile() {
            return fXmlFile;
        }
    }

    /**
     * Set the focus on the viewer
     */
    public void setFocus() {
        if (fXmlFilesTree != null && !fXmlFilesTree.isDisposed()) {
            fXmlFilesTree.setFocus();
        }
    }
}
