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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.TmfXmlUiStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static java.nio.file.StandardCopyOption.*;

/**
 * This dialog allows the user to modify some of the attributes in the XML
 * analysis, such as the color of the definedValues or the graph title.
 *
 * @author Jonathan Sauvé
 *
 */
public class XmlFilePropertiesDialog extends Dialog {

    private File fXmlFile = null;
    private File fOriginalXmlFile = null;

    private Shell fShell = null;
    private Composite fParent = null;

    private SashForm fSash = null;
    private Tree fTree = null;

    private TreeItem fLastSelectedItem = null;
    private int fLastSelectedItemIndex = 0;

    private Composite fComposite = null;
    private ScrolledComposite fScrolledComposite = null;
    private Composite fProperties = null;

    private boolean fCreateAnotherTable = false;
    private final Table[] fCurrentTable = new Table[1];
    private Composite fChanges;
    private Button fRestoreDefaults;
    private Button fSaveChanges;

    private Map<Integer, Runnable> fUnappliedModif = new HashMap<>();
    private List<Node> fInitialValues = new ArrayList<>();

    /**
     * Public constructor
     *
     * @param parentShell
     *            The parent's shell
     * @param xmlFile
     *            The xmlFile to show the properties
     */
    public XmlFilePropertiesDialog(Shell parentShell, File xmlFile) {
        super(parentShell);
        super.setShellStyle(super.getShellStyle() | SWT.SHELL_TRIM);
        fOriginalXmlFile = xmlFile;
        // create a temporary file to save
        try {
            fXmlFile = File.createTempFile("temp", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
            Files.copy(xmlFile.toPath(), fXmlFile.toPath(), REPLACE_EXISTING);

        } catch (IOException e) {
            Activator.logError(e.getMessage(), e);
            return;
        }
        fUnappliedModif.clear();
        fInitialValues.clear();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        fParent = (Composite) super.createDialogArea(parent);
        fParent.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 0, 0));
        fParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createContents();

        return fParent;
    }

    @Override
    protected void configureShell(Shell newShell) {
        newShell.setText(Messages.XmlFilePropertiesDialog_shellText + fOriginalXmlFile.getName());
        newShell.setMinimumSize(700, 800);
        newShell.setSize(700, 800);
        newShell.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 0, 0));

        super.configureShell(newShell);

        fShell = newShell;
    }

    @Override
    protected void cancelPressed() {
        Button cancelButton = super.getButton(IDialogConstants.CANCEL_ID);
        if (cancelButton != null) {
            cancelButton.addListener(SWT.Selection, new CloseShellListener(IDialogConstants.CANCEL_ID));
        }
    }

    @Override
    protected void okPressed() {
        Button okButton = super.getButton(IDialogConstants.OK_ID);
        if (okButton != null) {
            okButton.addListener(SWT.Selection, new CloseShellListener(IDialogConstants.OK_ID));
        }
    }

    @Override
    public boolean close() {
        try {
            Files.copy(fXmlFile.toPath(), fOriginalXmlFile.toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            Activator.logError(e.getMessage(), e);
        }
        fXmlFile.delete();
        return super.close();
    }

    /**
     * Initialize the principal content for this view.
     */
    private void createContents() {
        fSash = new SashForm(fParent, SWT.HORIZONTAL | SWT.NONE);
        fSash.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 0, 0));
        fSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        fTree = new Tree(fSash, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        fTree.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 0, 0));
        fTree.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, true));
        fTree.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem selectedItem = (TreeItem) e.item;
                TreeItem[] children = fTree.getItems();
                boolean found = false;
                for (int i = 0; i < children.length; i++) {
                    if (selectedItem.hashCode() == children[i].hashCode()) {
                        fLastSelectedItemIndex = i;
                        found = true;
                    }
                }
                if (!found) {
                    fLastSelectedItemIndex = 0;
                }
                Node root = (Node) selectedItem.getData(Messages.NodeKey);
                if (!selectedItem.equals(fLastSelectedItem)) {
                    int returnCode = 0;
                    if (modifsInStandby()) {
                        MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(),
                                Messages.XmlFilePropertiesDialog_xmlUnsavedChangesText, null, Messages.XmlFilePropertiesDialog_xmlUnsavedChangesQuestion,
                                MessageDialog.QUESTION, new String[] { Messages.XmlFilePropertiesDialog_xmlUnsavedChangesOption1,
                                        Messages.XmlFilePropertiesDialog_xmlUnsavedChangesOption2,
                                        Messages.XmlFilePropertiesDialog_xmlUnsavedChangesOption3 },
                                0);
                        returnCode = dialog.open();
                    }
                    switch (returnCode) {
                    // close without saving
                    case 0:
                        clearModifs();
                        fillComposite(root);
                        fLastSelectedItem = selectedItem;
                        break;
                    // cancel
                    case 1:
                        fTree.setSelection(fLastSelectedItem);
                        break;
                    // save and close
                    case 2:
                        applyAllModifs();
                        fillComposite(root);
                        fLastSelectedItem = selectedItem;
                        break;
                    // same as cancel
                    default:
                        fTree.setSelection(fLastSelectedItem);
                        break;
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        XmlAnalysisManagerParser parser = null;
        try {
            parser = new XmlAnalysisManagerParser(fXmlFile.getPath());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Activator.logError(e.getMessage(), e);
        }
        if (parser != null) {
            List<Node> roots = parser.getRoots();
            for (Node root : roots) {
                root.setUserData(Messages.FileKey, fXmlFile, null);
                fInitialValues.add(root);
                TreeItem item = new TreeItem(fTree, SWT.NONE);
                item.setText(root.getNodeName());
                item.setData(Messages.NodeKey, root);
            }
        }

        fComposite = new Composite(fSash, SWT.BORDER);
        fComposite.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 0, 0));
        fComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        fScrolledComposite = new ScrolledComposite(fComposite, SWT.V_SCROLL | SWT.H_SCROLL);
        fScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        fProperties = new Composite(fScrolledComposite, SWT.NONE);

        fChanges = new Composite(fComposite, SWT.NONE);
        GridLayout changesCompositeLayout = new GridLayout(2, false);
        changesCompositeLayout.marginHeight = 20;
        changesCompositeLayout.marginWidth = 20;
        fChanges.setLayout(changesCompositeLayout);
        fChanges.setLayoutData(new GridData(SWT.END, SWT.END, true, false));

        fRestoreDefaults = new Button(fChanges, SWT.PUSH);
        fRestoreDefaults.setText(Messages.XmlFilePropertiesDialog_restoreDefaultsButtonText);
        fRestoreDefaults.setLayoutData(new GridData(140, 30));
        fRestoreDefaults.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_QUESTION
                        | SWT.YES | SWT.NO);
                messageBox.setMessage(Messages.XmlFilePropertiesDialog_restoreDefaultsQuestion);
                messageBox.setText(Messages.XmlFilePropertiesDialog_restoreDefaultsShellText);
                int answer = messageBox.open();
                if (answer == SWT.YES) {
                    fRestoreDefaults();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        fSaveChanges = new Button(fChanges, SWT.PUSH);
        fSaveChanges.setText(Messages.XmlFilePropertiesDialog_saveChangesButtonText);
        fSaveChanges.setLayoutData(new GridData(120, 30));
        fSaveChanges.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                applyAllModifs();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        fSaveChanges.setEnabled(false);

        fSash.setWeights(new int[] { 1, 3 });
    }

    /**
     * Initialize the filling.
     *
     * @param root
     *            The root node
     */
    private void fillComposite(final Node root) {
        /* Clear parent's children */
        Control[] parentChildren = fProperties.getChildren();
        for (int i = 0; i < parentChildren.length; i++) {
            parentChildren[i].dispose();
        }
        fCreateAnotherTable = true;
        fillCompositeWithRoot(root);
        fCurrentTable[0] = null;

        fScrolledComposite.setContent(fProperties);
        fScrolledComposite.setExpandHorizontal(true);
        fScrolledComposite.setExpandVertical(true);
        fScrolledComposite.setMinSize(fProperties.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Fill the composite with informations from the root. After this, fill it
     * with the children informations
     *
     * @param root
     *            The root node
     */
    private void fillCompositeWithRoot(final Node root) {
        fProperties.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 0, 0));
        fProperties.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));

        String nodeName = root.getNodeName();
        switch (nodeName) {
        case TmfXmlUiStrings.TIME_GRAPH_VIEW:
            Composite IDComposite = new Composite(fProperties, SWT.NONE);
            IDComposite.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 5, 5));
            IDComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));

            Label ID = new Label(IDComposite, SWT.NONE);
            ID.setText(Messages.XmlFilePropertiesDialog_IDLabelText);

            Composite IDValueComposite = new Composite(fProperties, SWT.NONE);
            IDValueComposite.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 15, 5));
            IDValueComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));

            Text IDValue = new Text(IDValueComposite, SWT.BORDER);
            IDValue.setLayoutData(new GridData(350, 40));
            final String initialTitle = root.getAttributes().getNamedItem(TmfXmlStrings.ID).getNodeValue();
            IDValue.setText(initialTitle);
            IDValue.setData(Messages.NodeKey, root);
            IDValue.addModifyListener(new ModifyTextListener(IDValue, initialTitle, root, TmfXmlStrings.ID));

            XmlAnalysisManagerUtils.addBasicMenuToText(IDValue);

            new MenuItem(IDValue.getMenu(), SWT.SEPARATOR);

            MenuItem resetText = new MenuItem(IDValue.getMenu(), SWT.NONE);
            resetText.setText(Messages.XmlFilePropertiesDialog_resetTextText);
            resetText.setData(Messages.NodeKey, root);
            resetText.addSelectionListener(new ResetTextListener(IDValue, initialTitle, root));
            break;
        case TmfXmlUiStrings.XY_VIEW:
            Composite IDComposite2 = new Composite(fProperties, SWT.NONE);
            IDComposite2.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 5, 5));
            IDComposite2.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));

            Label ID2 = new Label(IDComposite2, SWT.NONE);
            ID2.setText(Messages.XmlFilePropertiesDialog_IDLabelText);

            Composite IDValueComposite2 = new Composite(fProperties, SWT.NONE);
            IDValueComposite2.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 15, 5));
            IDValueComposite2.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));

            Text IDValue2 = new Text(IDValueComposite2, SWT.BORDER);
            IDValue2.setLayoutData(new GridData(350, 40));
            final String initialTitle2 = root.getAttributes().getNamedItem(TmfXmlStrings.ID).getNodeValue();
            IDValue2.setText(initialTitle2);
            IDValue2.setData(Messages.NodeKey, root);
            IDValue2.addModifyListener(new ModifyTextListener(IDValue2, initialTitle2, root, TmfXmlStrings.ID));
            break;
        case TmfXmlStrings.STATE_PROVIDER:
            break;
        default:
            break;
        }

        NodeList rootChildren = root.getChildNodes();
        for (int i = 0; i < rootChildren.getLength(); i++) {
            fillCompositeWithRootChildren(root, rootChildren.item(i));
        }

        List<Table> tables = new ArrayList<>();
        Control[] parentChildren = fProperties.getChildren();
        for (int i = 0; i < parentChildren.length; i++) {
            if (parentChildren[i] instanceof Table) {
                tables.add((Table) parentChildren[i]);
                break;
            }
        }
        if (!tables.isEmpty()) {
            for (int i = 0; i < tables.size(); i++) {
                Table table = tables.get(i);
                for (int j = 0; j < table.getColumnCount(); j++) {
                    table.getColumn(i).pack();
                }
                table.pack();
            }

        }

        fProperties.layout(true, true);
    }

    /**
     * This function fills the composite with the children of this root. This
     * function is recursive
     *
     * @param root
     *            The root of the tree. Three possibilities:
     *            {@link TmfXmlUiStrings#TIME_GRAPH_VIEW},
     *            {@link TmfXmlUiStrings#XY_VIEW} or
     *            {@link TmfXmlStrings#STATE_PROVIDER}
     * @param child
     *            The child node
     */
    private void fillCompositeWithRootChildren(final Node root, final Node child) {
        String nodeName = child.getNodeName();

        if (!root.getNodeName().equals(TmfXmlStrings.STATE_PROVIDER)) {
            switch (nodeName) {
            case TmfXmlUiStrings.ENTRY_ELEMENT:
                Label entryTitle = new Label(fProperties, SWT.NONE);
                entryTitle.setText(Messages.XmlFilePropertiesDialog_entryTitleText);

                FontData fontData3 = entryTitle.getFont().getFontData()[0];
                Font font3 = new Font(fProperties.getDisplay(), new FontData(fontData3.getName(), fontData3.getHeight(), SWT.BOLD));
                entryTitle.setFont(font3);

                Composite currentPathComposite = new Composite(fProperties, SWT.NONE);
                currentPathComposite.setLayout(XmlAnalysisManagerUtils.createGridLayout(3, 5, 5));

                Label currentPath = new Label(currentPathComposite, SWT.NONE);
                currentPath.setText(Messages.XmlFilePropertiesDialog_currentPathText);

                final Label currentPathValue = new Label(currentPathComposite, SWT.BORDER);
                final String initialValue = child.getAttributes().getNamedItem(TmfXmlUiStrings.PATH).getNodeValue();
                currentPathValue.setText(initialValue);
                currentPathValue.setLayoutData(new GridData(200, 20));

                Button buildPath = new Button(currentPathComposite, SWT.PUSH);
                buildPath.setText(Messages.XmlFilePropertiesDialog_buildPathButtonText);

                buildPath.addSelectionListener(new SelectionListener() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Button button = (Button) e.widget;
                        final Node oldNode = (Node) button.getData(Messages.NodeKey);
                        final File xmlFile = (File) root.getUserData(Messages.FileKey);

                        final StateSystemPathBuilderViewer path = new StateSystemPathBuilderViewer(fParent.getShell());
                        // Check if the user have an active trace before
                        // opening
                        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();

                        if (trace != null) {
                            int returnCode = path.open();
                            if (returnCode == Window.OK) {
                                currentPathValue.setText(path.getBuildPath());
                                addModif(button.hashCode(), new Runnable() {

                                    @Override
                                    public void run() {
                                        try {
                                            XmlUtils.setNewAttribute(xmlFile, oldNode, TmfXmlUiStrings.PATH, path.getBuildPath());
                                        } catch (ParserConfigurationException | SAXException | IOException | TransformerException error) {
                                            Activator.logError(error.getMessage(), error);
                                            return;
                                        }
                                    }
                                });

                            }
                        } else {
                            ErrorDialog.openError(fParent.getShell(), Messages.XmlFilePropertiesDialog_openErrorText,
                                    Messages.XmlFilePropertiesDialog_openErrorSSPB,
                                    new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.XmlFilePropertiesDialog_openErrorSSPBReason));
                        }
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                    }
                });

                createEntryTable(root, child);
                break;
            case TmfXmlStrings.HEAD:
                break;
            case TmfXmlStrings.TRACETYPE:
                break;
            case TmfXmlStrings.ID:
                break;
            case TmfXmlStrings.LABEL:
                Composite labelComposite = new Composite(fProperties, SWT.NONE);
                labelComposite.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 5, 5));
                labelComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

                Label graphTitle = new Label(labelComposite, SWT.NONE);
                graphTitle.setText(Messages.XmlFilePropertiesDialog_graphTitleText);

                Composite textComposite = new Composite(fProperties, SWT.NONE);
                textComposite.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 15, 5));
                textComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

                final Text text = new Text(textComposite, SWT.BORDER);
                final String initialTitle = child.getAttributes().getNamedItem(TmfXmlStrings.VALUE).getNodeValue();
                text.setLayoutData(new GridData(350, 40));
                text.setText(initialTitle);
                text.setData(Messages.NodeKey, child);
                text.addModifyListener(new ModifyTextListener(text, initialTitle, root, TmfXmlStrings.VALUE));
                XmlAnalysisManagerUtils.addBasicMenuToText(text);

                new MenuItem(text.getMenu(), SWT.SEPARATOR);

                MenuItem resetText = new MenuItem(text.getMenu(), SWT.NONE);
                resetText.setText(Messages.XmlFilePropertiesDialog_resetTextText);
                resetText.setData(Messages.NodeKey, child);
                resetText.addSelectionListener(new ResetTextListener(text, initialTitle, root));
                break;
            case TmfXmlStrings.ANALYSIS:
                Composite analysisIDComposite = new Composite(fProperties, SWT.NONE);
                analysisIDComposite.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 5, 5));
                analysisIDComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

                Label analysisID = new Label(analysisIDComposite, SWT.NONE);
                analysisID.setText(Messages.XmlFilePropertiesDialog_analysisIDText);

                Composite analysisIDValueComposite = new Composite(fProperties, SWT.NONE);
                analysisIDValueComposite.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 15, 5));
                analysisIDValueComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

                Text analysisIDValue = new Text(analysisIDValueComposite, SWT.BORDER);
                analysisIDValue.setLayoutData(new GridData(350, 40));
                final String initialTitle2 = child.getAttributes().getNamedItem(TmfXmlStrings.ID).getNodeValue();
                analysisIDValue.setText(initialTitle2);
                analysisIDValue.setData(Messages.NodeKey, child);
                analysisIDValue.addModifyListener(new ModifyTextListener(analysisIDValue, initialTitle2, root, TmfXmlStrings.ID));

                XmlAnalysisManagerUtils.addBasicMenuToText(analysisIDValue);

                new MenuItem(analysisIDValue.getMenu(), SWT.SEPARATOR);

                MenuItem resetText2 = new MenuItem(analysisIDValue.getMenu(), SWT.NONE);
                resetText2.setText(Messages.XmlFilePropertiesDialog_resetTextText);
                resetText2.setData(Messages.NodeKey, root);
                resetText2.addSelectionListener(new ResetTextListener(analysisIDValue, initialTitle2, root));
                break;
            case TmfXmlStrings.DEFINED_VALUE:
                if (fCreateAnotherTable) {
                    Label processStatusTitle = new Label(fProperties, SWT.NONE);
                    processStatusTitle.setText(Messages.XmlFilePropertiesDialog_processStatusTitleText);

                    FontData fontData2 = processStatusTitle.getFont().getFontData()[0];
                    Font font2 = new Font(fProperties.getDisplay(), new FontData(fontData2.getName(), fontData2.getHeight(), SWT.BOLD));
                    processStatusTitle.setFont(font2);

                    Composite definedValueTableComposite = new Composite(fProperties, SWT.NONE);
                    definedValueTableComposite.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 15, 5));
                    definedValueTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

                    Table definedValueTable = new Table(definedValueTableComposite, SWT.SINGLE | SWT.BORDER);
                    definedValueTable.setLinesVisible(true);
                    definedValueTable.setHeaderVisible(true);
                    definedValueTable.setLayout(new TableLayout());
                    definedValueTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

                    TableColumn nameColumn = new TableColumn(definedValueTable, SWT.NONE);
                    nameColumn.setText(TmfXmlStrings.NAME);
                    nameColumn.setWidth(275);
                    nameColumn.setResizable(true);

                    TableColumn valueColumn = new TableColumn(definedValueTable, SWT.NONE);
                    valueColumn.setText(TmfXmlStrings.VALUE);
                    valueColumn.setWidth(60);
                    valueColumn.setResizable(true);

                    TableColumn colorColumn = new TableColumn(definedValueTable, SWT.NONE);
                    colorColumn.setText(TmfXmlStrings.COLOR);
                    colorColumn.setWidth(100);
                    colorColumn.setResizable(true);

                    createDefinedValueTableEditor(definedValueTable, root);
                    fCurrentTable[0] = definedValueTable;
                    fCreateAnotherTable = false;
                }

                if (fCurrentTable[0] != null) {
                    addRowDefinedValueTable(fCurrentTable[0], child);
                    fCurrentTable[0].setSize(fCurrentTable[0].computeSize(SWT.DEFAULT, SWT.DEFAULT));
                }

                break;
            case TmfXmlStrings.LOCATION:
                break;
            case TmfXmlStrings.EVENT_HANDLER:
                break;
            case TmfXmlStrings.STATE_ATTRIBUTE:
                break;
            case TmfXmlStrings.STATE_VALUE:
                break;
            case TmfXmlStrings.STATE_CHANGE:
                break;
            case TmfXmlStrings.ELEMENT_FIELD:
                break;
            case TmfXmlStrings.HANDLER_EVENT_NAME:
                break;
            default:
                break;
            }

            NodeList childChildren = child.getChildNodes();
            for (int i = 0; i < childChildren.getLength(); i++) {
                fillCompositeWithRootChildren(root, childChildren.item(i));
            }
        }
    }

    /**
     * Create the table editor for this table.
     *
     * @param definedValueTable
     *            The table
     * @param root
     *            The root node, one between
     *            {@link TmfXmlStrings#STATE_PROVIDER},
     *            {@link TmfXmlUiStrings#TIME_GRAPH_VIEW}
     *            {@link TmfXmlUiStrings#XY_VIEW}
     */
    private void createDefinedValueTableEditor(final Table definedValueTable, final Node root) {
        final TableEditor editor = new TableEditor(definedValueTable);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        definedValueTable.addListener(SWT.MouseDoubleClick, new Listener() {

            @Override
            public void handleEvent(Event event) {
                Rectangle clientArea = definedValueTable.getClientArea();
                Point pt = new Point(event.x, event.y);
                int index = definedValueTable.getTopIndex();
                while (index < definedValueTable.getItemCount()) {
                    boolean visible = false;
                    final TableItem item = definedValueTable.getItem(index);
                    final Node child = (Node) item.getData(Messages.NodeKey);
                    for (int j = 0; j < definedValueTable.getColumnCount(); j++) {
                        Rectangle rect = item.getBounds(j);
                        if (rect.contains(pt)) {
                            final int column = j;
                            final TableColumn tableColumn = definedValueTable.getColumn(j);
                            if (tableColumn.getText().equals(TmfXmlStrings.NAME)) {
                                final Text text = new Text(definedValueTable, SWT.NONE);
                                Listener textListener = new Listener() {

                                    @Override
                                    public void handleEvent(Event e) {
                                        switch (e.type) {
                                        case SWT.FocusOut:
                                            item.setText(column, text.getText());
                                            notifyChange(item, child);
                                            text.dispose();
                                            break;
                                        case SWT.Traverse:
                                            switch (e.detail) {
                                            case SWT.TRAVERSE_RETURN:
                                                item.setText(column, text.getText());
                                                notifyChange(item, child);
                                                //$FALL-THROUGH$
                                            case SWT.TRAVERSE_ESCAPE:
                                                text.dispose();
                                                e.doit = false;
                                                //$FALL-THROUGH$
                                            default:
                                                break;
                                            }
                                            break;
                                        default:
                                            break;
                                        }
                                    }

                                };

                                XmlAnalysisManagerUtils.addBasicMenuToText(text);

                                text.addListener(SWT.FocusOut, textListener);
                                text.addListener(SWT.Traverse, textListener);
                                editor.setEditor(text, item, j);
                                text.setText(item.getText(j));
                                text.selectAll();
                                text.setFocus();
                                return;
                            } else if (tableColumn.getText().equals(TmfXmlStrings.COLOR)) {
                                Color oldColor = item.getBackground(column);

                                ColorDialog dialog = new ColorDialog(Display.getDefault().getActiveShell());
                                dialog.setRGB(oldColor.getRGB());
                                dialog.setText(Messages.XmlFilePropertiesDialog_colorDialogText);

                                final RGB newRgb = dialog.open();
                                if (newRgb != null) {
                                    notifyChange(item, child);
                                    item.setBackground(column, new Color(Display.getDefault(), newRgb));
                                }
                            }
                        }
                        if (!visible && rect.intersects(clientArea)) {
                            visible = true;
                        }
                    }

                    if (!visible) {
                        return;
                    }
                    index++;
                }
            }

            void notifyChange(final TableItem item, final Node child) {
                addModif(item.hashCode(), new Runnable() {

                    @Override
                    public void run() {
                        try {
                            File copyFile = (File) root.getUserData(Messages.FileKey);
                            XmlUtils.setNewAttribute(copyFile, child, TmfXmlStrings.NAME,
                                    item.getText(0));
                            RGB color = item.getBackground(2).getRGB();
                            XmlUtils.setNewAttribute(copyFile, child, TmfXmlStrings.COLOR,
                                    XmlAnalysisManagerUtils.rgbToHexa(color.red, color.green, color.blue));
                        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e1) {
                            Activator.logError(e1.getMessage(), e1);
                            return;
                        }
                    }
                });
            }
        });
    }

    /**
     * Add a row with the child attribute's values
     *
     * @param definedValueTable
     *            The Table
     * @param child
     *            The node
     */
    private void addRowDefinedValueTable(Table definedValueTable, final Node child) {
        TableItem item = new TableItem(definedValueTable, SWT.NONE);
        item.setData(Messages.NodeKey, child);
        final String initialName = child.getAttributes().getNamedItem(TmfXmlStrings.NAME).getNodeValue();
        item.setText(0, initialName);

        item.setText(1, child.getAttributes().getNamedItem(TmfXmlStrings.VALUE).getNodeValue());

        final Node colorNode = child.getAttributes().getNamedItem(TmfXmlStrings.COLOR);
        if (colorNode != null) {
            String stringColor = colorNode.getNodeValue();
            int[] colorComponents = XmlAnalysisManagerUtils.hexaColorToInt(stringColor);
            if(colorComponents != null) {
                final RGB oldRGB = new RGB(colorComponents[0], colorComponents[1],
                        colorComponents[2]);
                item.setBackground(2, new Color(fProperties.getDisplay(), oldRGB));
            }
        }
        definedValueTable.layout(true, true);
    }

    /**
     * This function create a table for an entry node, with the appropriate
     * columns
     *
     * @param root
     *            The root of the entry (timeGraphView or xyView)
     * @param group
     *            The composite of the entry
     * @param entry
     *            The entry node
     */
    private void createEntryTable(final Node root, final Node entry) {
        if (!entry.getNodeName().equals(TmfXmlUiStrings.ENTRY_ELEMENT)) {
            return;
        }

        Composite entryAttributeComposite = new Composite(fProperties, SWT.NONE);
        entryAttributeComposite.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 5, 5));
        entryAttributeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        Label entryAttributeTitle = new Label(entryAttributeComposite, SWT.NONE);
        entryAttributeTitle.setText("Entry attributes:"); //$NON-NLS-1$

        Composite entryAttributeTableComposite = new Composite(fProperties, SWT.NONE);
        entryAttributeTableComposite.setLayout(XmlAnalysisManagerUtils.createGridLayout(1, 15, 5));
        entryAttributeTableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

        final Table entryAttributeTable = new Table(entryAttributeTableComposite, SWT.SINGLE | SWT.BORDER);
        entryAttributeTable.setLinesVisible(true);
        entryAttributeTable.setHeaderVisible(true);
        entryAttributeTable.setLayout(new TableLayout());
        entryAttributeTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

        TableColumn attributeColumn = new TableColumn(entryAttributeTable, SWT.NONE);
        attributeColumn.setText("Attribute"); //$NON-NLS-1$
        attributeColumn.setResizable(true);
        attributeColumn.setWidth(100);

        /* Find all the column headers */
        List<String> columnsHeaders = new ArrayList<>();
        final NodeList entryChildren = entry.getChildNodes();
        for (int j = 0; j < entryChildren.getLength(); j++) {
            if (!entryChildren.item(j).getNodeName().equals("#text")) { //$NON-NLS-1$
                NamedNodeMap childAttributes = entryChildren.item(j).getAttributes();

                for (int k = 0; k < childAttributes.getLength(); k++) {
                    boolean present = false;
                    for (String columnHeader : columnsHeaders) {
                        if (columnHeader.equals(childAttributes.item(k).getNodeName())) {
                            present = true;
                            break;
                        }
                    }
                    if (!present) {
                        columnsHeaders.add(childAttributes.item(k).getNodeName());
                    }
                }
            }
        }
        /* Create all the columns */
        for (int j = 0; j < columnsHeaders.size(); j++) {
            TableColumn column = new TableColumn(entryAttributeTable, SWT.NONE);
            column.setText(columnsHeaders.get(j));
            column.setResizable(true);
            column.setWidth(100);
        }

        /* Fill the table */
        for (int j = 0; j < entryChildren.getLength(); j++) {
            if (!entryChildren.item(j).getNodeName().equals("#text")) { //$NON-NLS-1$
                final List<Pair<String, String>> attNameAndValue = new ArrayList<>();
                TableItem row = new TableItem(entryAttributeTable, SWT.NONE);
                row.setText(0, entryChildren.item(j).getNodeName());
                row.setData(Messages.NodeKey, entryChildren.item(j));

                NamedNodeMap childAttributes = entryChildren.item(j).getAttributes();
                for (int k = 0; k < childAttributes.getLength(); k++) {
                    TableColumn[] columns = entryAttributeTable.getColumns();
                    for (int l = 0; l < columns.length; l++) {
                        if (columns[l].getText().equals(childAttributes.item(k).getNodeName())) {
                            row.setText(l, childAttributes.item(k).getNodeValue());
                            String name = childAttributes.item(k).getNodeName();
                            String value = childAttributes.item(k).getNodeValue();
                            if (name != null && value != null) {
                                attNameAndValue.add(new Pair<>(name,
                                        value));
                            }
                        }
                    }
                }
            }
        }

        final TableEditor editor = new TableEditor(entryAttributeTable);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        entryAttributeTable.addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                Rectangle clientArea = entryAttributeTable.getClientArea();
                Point pt = new Point(event.x, event.y);
                int index = entryAttributeTable.getTopIndex();
                while (index < entryAttributeTable.getItemCount()) {
                    boolean visible = false;
                    final TableItem item = entryAttributeTable.getItem(index);
                    for (int j = 0; j < entryAttributeTable.getColumnCount(); j++) {
                        Rectangle rect = item.getBounds(j);
                        if (rect.contains(pt)) {
                            final int column = j;
                            final TableColumn tableColumn = entryAttributeTable.getColumn(j);
                            if (tableColumn.getText().equals("Attribute")) { //$NON-NLS-1$
                                final Combo types = new Combo(entryAttributeTable, SWT.READ_ONLY | SWT.DROP_DOWN);
                                String rootType = root.getNodeName();
                                String[] possibleTypes = null;
                                if (rootType.equals(TmfXmlUiStrings.TIME_GRAPH_VIEW)) {
                                    possibleTypes = new String[4];
                                    possibleTypes[0] = TmfXmlUiStrings.DISPLAY_ELEMENT;
                                    possibleTypes[1] = TmfXmlStrings.ID;
                                    possibleTypes[2] = TmfXmlUiStrings.PARENT_ELEMENT;
                                    possibleTypes[3] = TmfXmlStrings.NAME;
                                }
                                if (rootType.equals(TmfXmlUiStrings.XY_VIEW)) {
                                    possibleTypes = new String[2];
                                    possibleTypes[0] = TmfXmlUiStrings.DISPLAY_ELEMENT;
                                    possibleTypes[1] = TmfXmlStrings.NAME;
                                }
                                types.setItems(possibleTypes);
                                types.setText(item.getText(j));

                                Listener comboListener = new Listener() {

                                    @Override
                                    public void handleEvent(final Event e) {
                                        switch (e.type) {
                                        case SWT.FocusOut:
                                            item.setText(column, types.getText());
                                            types.dispose();
                                            break;
                                        case SWT.Traverse:
                                            switch (e.detail) {
                                            case SWT.TRAVERSE_RETURN:
                                                item.setText(column, types.getText());
                                                //$FALL-THROUGH$
                                            case SWT.TRAVERSE_ESCAPE:
                                                types.dispose();
                                                e.doit = false;
                                                //$FALL-THROUGH$
                                            default:
                                                break;
                                            }
                                            break;
                                        case SWT.Modify:
                                            addModif(item.hashCode(), new Runnable() {

                                                @Override
                                                public void run() {
                                                    try {
                                                        String newText = item.getText();
                                                        Node entryChild = (Node) item.getData(Messages.NodeKey);
                                                        File copyFile = (File) root.getUserData(Messages.FileKey);

                                                        XmlUtils.setNewAttribute(copyFile, entryChild,
                                                                null, newText);
                                                    } catch (ParserConfigurationException | SAXException | IOException | TransformerException e1) {
                                                        Activator.logError(e1.getMessage(), e1);
                                                    }
                                                }
                                            });
                                            break;
                                        default:
                                            break;

                                        }
                                    }
                                };

                                types.addListener(SWT.FocusOut, comboListener);
                                types.addListener(SWT.Traverse, comboListener);
                                types.addListener(SWT.Modify, comboListener);
                                editor.setEditor(types, item, j);
                                types.setText(item.getText(j));
                                types.setFocus();
                                return;
                            } else if (tableColumn.getText().equals(TmfXmlStrings.VALUE)) {
                                final Text text = new Text(entryAttributeTable, SWT.NONE);
                                Listener textListener = new Listener() {
                                    @Override
                                    public void handleEvent(final Event e) {
                                        switch (e.type) {
                                        case SWT.FocusOut:
                                            item.setText(column, text.getText());
                                            text.dispose();
                                            break;
                                        case SWT.Traverse:
                                            switch (e.detail) {
                                            case SWT.TRAVERSE_RETURN:
                                                item.setText(column, text.getText());
                                                //$FALL-THROUGH$
                                            case SWT.TRAVERSE_ESCAPE:
                                                text.dispose();
                                                e.doit = false;
                                                //$FALL-THROUGH$
                                            default:
                                                break;
                                            }
                                            break;
                                        case SWT.Modify:
                                            addModif(item.hashCode(), new Runnable() {

                                                @Override
                                                public void run() {
                                                    try {
                                                        String newText = text.getText();
                                                        Node entryChild = (Node) item.getData(Messages.NodeKey);
                                                        File copyFile = (File) root.getUserData(Messages.FileKey);

                                                        XmlUtils.setNewAttribute(copyFile, entryChild,
                                                                entryAttributeTable.getColumn(column).getText(), newText);
                                                    } catch (ParserConfigurationException | SAXException | IOException | TransformerException e1) {
                                                        Activator.logError(e1.getMessage(), e1);
                                                    }
                                                }
                                            });
                                            break;
                                        default:
                                            break;
                                        }
                                    }
                                };
                                XmlAnalysisManagerUtils.addBasicMenuToText(text);

                                text.addListener(SWT.FocusOut, textListener);
                                text.addListener(SWT.Traverse, textListener);
                                text.addListener(SWT.Modify, textListener);
                                editor.setEditor(text, item, j);
                                text.setText(item.getText(j));
                                text.selectAll();
                                text.setFocus();
                                return;
                            } else if (tableColumn.getText().equals(TmfXmlStrings.TYPE)) {
                                final Combo types = new Combo(entryAttributeTable, SWT.READ_ONLY);
                                String[] possibleTypes = { TmfXmlStrings.TYPE_CONSTANT, TmfXmlStrings.TYPE_LOCATION,
                                        TmfXmlStrings.TYPE_QUERY, TmfXmlStrings.TYPE_SELF };
                                types.setItems(possibleTypes);
                                types.setText(item.getText(j));

                                Listener comboListener = new Listener() {

                                    @Override
                                    public void handleEvent(final Event e) {
                                        switch (e.type) {
                                        case SWT.FocusOut:
                                            item.setText(column, types.getText());
                                            types.dispose();
                                            break;
                                        case SWT.Traverse:
                                            switch (e.detail) {
                                            case SWT.TRAVERSE_RETURN:
                                                item.setText(column, types.getText());
                                                //$FALL-THROUGH$
                                            case SWT.TRAVERSE_ESCAPE:
                                                types.dispose();
                                                e.doit = false;
                                                //$FALL-THROUGH$
                                            default:
                                                break;
                                            }
                                            break;
                                        case SWT.Modify:
                                            addModif(item.hashCode(), new Runnable() {

                                                @Override
                                                public void run() {
                                                    try {
                                                        String newText = item.getText(column);
                                                        Node entryChild = (Node) item.getData(Messages.NodeKey);
                                                        File copyFile = (File) root.getUserData(Messages.FileKey);

                                                        XmlUtils.setNewAttribute(copyFile, entryChild,
                                                                entryAttributeTable.getColumn(column).getText(), newText);
                                                    } catch (ParserConfigurationException | SAXException | IOException | TransformerException e1) {
                                                        Activator.logError(e1.getMessage(), e1);
                                                    }
                                                }
                                            });
                                            break;
                                        default:
                                            break;

                                        }
                                    }
                                };

                                types.addListener(SWT.FocusOut, comboListener);
                                types.addListener(SWT.Traverse, comboListener);
                                types.addListener(SWT.Modify, comboListener);
                                editor.setEditor(types, item, j);
                                types.setText(item.getText(j));
                                types.setFocus();
                                return;
                            }
                        }
                        if (!visible && rect.intersects(clientArea)) {
                            visible = true;
                        }
                    }
                    if (!visible) {
                        return;
                    }
                    index++;
                }
            }
        });
    }

    /**
     * Add a modification to the map. This function must only be used by this
     * class, or in a <code>Listener</code> of one of its controls.
     *
     * @param id
     *            The ID associate with this method. In facts, this ID must be
     *            the one of the widget on which the event occured
     * @param newValueMethod
     *            The method to be called later
     * @return Whether a method with this ID is actually present or not
     */
    private boolean addModif(int id, Runnable newValueMethod) {
        Runnable ret = fUnappliedModif.put(id, newValueMethod);
        if (fUnappliedModif.size() == 1) {
            fSaveChanges.setEnabled(true);
        }
        return ret == null ? false : true;
    }

    /**
     * This method execute all the changes on the XML file at one time. It
     * clears the map after the execution.
     */
    private void applyAllModifs() {
        for (Runnable method : fUnappliedModif.values()) {
            method.run();
        }
        // update the roots node
        XmlAnalysisManagerParser parser = null;
        try {
            parser = new XmlAnalysisManagerParser(fXmlFile.getPath());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Activator.logError(e.getMessage(), e);
        }
        if (parser != null) {
            List<Node> roots = parser.getRoots();
            TreeItem[] treeItems = fTree.getItems();
            for (int i = 0; i < roots.size() && i < treeItems.length; i++) {
                Node root = roots.get(i);
                root.setUserData(Messages.FileKey, fXmlFile, null);
                treeItems[i].setData(Messages.NodeKey, roots.get(i));
            }
        }
        fUnappliedModif.clear();
        fSaveChanges.setEnabled(false);
    }

    /**
     * @return True if there are changes in standby, false otherwise
     */
    private boolean modifsInStandby() {
        return !fUnappliedModif.isEmpty();
    }

    /**
     * Clear all the changes in standby, without applying them
     */
    private void clearModifs() {
        fUnappliedModif.clear();
        if (!fSaveChanges.isDisposed()) {
            fSaveChanges.setEnabled(false);
        }
    }

    /**
     * Restore all the default values by reloading the original XML file and
     * replace the content of the temp file
     */
    private void fRestoreDefaults() {
        try {
            Files.copy(fOriginalXmlFile.toPath(), fXmlFile.toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            Activator.logError(e.getMessage(), e);
            return;
        }

        TreeItem[] items = fTree.getItems();
        for (int i = 0; i < items.length; i++) {
            items[i].dispose();
        }

        XmlAnalysisManagerParser parser = null;
        try {
            parser = new XmlAnalysisManagerParser(fXmlFile.getPath());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Activator.logError(e.getMessage(), e);
            return;
        }
        List<Node> roots = parser.getRoots();
        for (Node root : roots) {
            root.setUserData(Messages.FileKey, fXmlFile, null);
            fInitialValues.add(root);
            TreeItem item = new TreeItem(fTree, SWT.NONE);
            item.setText(root.getNodeName());
            item.setData(Messages.NodeKey, root);
        }

        fUnappliedModif.clear();

        Control[] children = fProperties.getChildren();
        for (int i = 0; i < children.length; i++) {
            children[i].dispose();
        }
        fTree.select(fTree.getItem(fLastSelectedItemIndex));
        Event event = new Event();
        event.item = fTree.getItem(fLastSelectedItemIndex);
        fTree.notifyListeners(SWT.Selection, event);
    }

    private class ModifyTextListener implements ModifyListener {

        private Text ftext;
        private String ftitle;
        private Node froot;
        private String fattributeType;

        /**
         * @param text
         *            The Text where occured the listener
         * @param initialTitle
         *            The initial text value of the Text
         * @param root
         *            The root node
         * @param attributeType
         *            The attribute type to change the value in the XML file
         */
        public ModifyTextListener(final Text text, final String initialTitle, final Node root, final String attributeType) {
            ftext = text;
            ftitle = initialTitle;
            froot = root;
            fattributeType = attributeType;
        }

        @Override
        public void modifyText(ModifyEvent e) {
            final Node oldNode = (Node) ftext.getData(Messages.NodeKey);
            final File xmlFile = (File) froot.getUserData(Messages.FileKey);

            if (!ftitle.equals(ftext.getText())) {
                addModif(ftext.hashCode(), new Runnable() {

                    @Override
                    public void run() {
                        try {
                            XmlUtils.setNewAttribute(xmlFile, oldNode, fattributeType, ftext.getText());
                        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e1) {
                            Activator.logError(e1.getMessage(), e1);
                        }
                    }
                });
            }
        }

    }

    private class ResetTextListener implements SelectionListener {

        private Text ftext;
        private String ftitle;
        private Node froot;

        /**
         * This listener reset the <code>Text</code> to its initial value
         *
         * @param text
         *            The text to set the new attribute
         * @param initialTitle
         *            The initialTitle
         * @param root
         *            The root node
         */
        public ResetTextListener(final Text text, final String initialTitle, final Node root) {
            ftext = text;
            ftitle = initialTitle;
            froot = root;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            MenuItem menuItem = (MenuItem) e.widget;
            final Node oldNode = (Node) menuItem.getData(Messages.NodeKey);
            final File xmlFile = (File) froot.getUserData(Messages.FileKey);

            if (!ftitle.equals(ftext.getText())) {
                ftext.setText(ftitle);
                addModif(ftext.hashCode(), new Runnable() {

                    @Override
                    public void run() {
                        try {
                            XmlUtils.setNewAttribute(xmlFile, oldNode, TmfXmlStrings.VALUE, ftext.getText());
                        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e1) {
                            Activator.logError(e1.getMessage(), e1);
                        }
                    }
                });
            }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }

    }

    private class CloseShellListener implements Listener {

        private int fbuttonPressed;

        /**
         * @param buttonPressed
         *            The button being pressed : one between
         *            {@link IDialogConstants#OK_ID} or
         *            {@link IDialogConstants#CANCEL_ID}
         */
        public CloseShellListener(final int buttonPressed) {
            fbuttonPressed = buttonPressed;
        }

        @Override
        public void handleEvent(Event event) {
            if (modifsInStandby()) {
                if (fbuttonPressed == IDialogConstants.OK_ID) {
                    MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(),
                            Messages.XmlFilePropertiesDialog_closingWindowDialogText, null,
                            Messages.XmlFilePropertiesDialog_closingWindowsDialogQuestion1,
                            MessageDialog.QUESTION, new String[] { Messages.XmlFilePropertiesDialog_xmlUnsavedChangesOption1,
                                    Messages.XmlFilePropertiesDialog_xmlUnsavedChangesOption2,
                                    Messages.XmlFilePropertiesDialog_xmlUnsavedChangesOption3 },
                            0);
                    int returnCode = dialog.open();
                    switch (returnCode) {
                    // close without saving
                    case 0:
                        clearModifs();
                        fShell.close();
                        break;
                    // cancel
                    case 1:
                        // do nothing
                        break;
                    // save and close
                    case 2:
                        applyAllModifs();
                        fShell.close();
                        break;
                    // same as cancel
                    default:
                        // do nothing
                        break;
                    }
                } else if (fbuttonPressed == IDialogConstants.CANCEL_ID) {
                    MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), Messages.XmlFilePropertiesDialog_closingWindowDialogText, null,
                            Messages.XmlFilePropertiesDialog_closingWindowDialogQuestion2, MessageDialog.QUESTION,
                            new String[] { Messages.XmlFilePropertiesDialog_closingWindowDialogYes,
                                    Messages.XmlFilePropertiesDialog_closingWindowDialogNo },
                            0);
                    int returnCode = dialog.open();
                    switch (returnCode) {
                    // yes
                    case 0:
                        clearModifs();
                        fShell.close();
                        break;
                    // no
                    default:
                        break;
                    }
                }
            } else {
                if (!fShell.isDisposed()) {
                    fShell.close();
                }
            }
        }

    }
}
