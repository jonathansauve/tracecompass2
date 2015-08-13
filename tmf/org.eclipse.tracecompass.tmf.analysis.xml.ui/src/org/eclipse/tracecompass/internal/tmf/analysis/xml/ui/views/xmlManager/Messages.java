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

import org.eclipse.osgi.util.NLS;

/**
 * Externalized messages for the XML Manager package
 *
 * @author Jonathan Sauvé
 *
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.xmlManager.messages"; //$NON-NLS-1$

    /**
     * Key to get the {@link org.w3c.dom.Node} associate with this object using
     * the .getData(String key, Object data) function
     */
    public static String NodeKey;
    /**
     * Key to get the {@link java.io.File} associate with this object using the
     * .getData(String key, Object data) function
     */
    public static String FileKey;

    /** Messages for the {@link StateSystemPathBuilderViewer} class */
    public static String StateSystemPathBuilder_okButtonText;
    public static String StateSystemPathBuilder_shellText;
    public static String StateSystemPathBuilder_quickPathButtonText;
    public static String StateSystemPathBuilder_comboTextText;
    public static String StateSystemPathBuilder_textText;
    public static String StateSystemPathBuilder_customPathButtonText;
    public static String StateSystemPathBuilder_partialPathTextText;
    public static String StateSystemPathBuilder_pointerTextText;
    public static String StateSystemPathBuilder_group1Text;
    public static String StateSystemPathBuilder_addBoxItemText;
    public static String StateSystemPathBuilder_setNewPathText;
    public static String StateSystemPathBuilder_group2Text;
    public static String StateSystemPathBuilder_currentPathTitleText;

    /** Messages for the {@link XmlAnalysisManagerViewer} class */
    public static String XmlManagerViewer_importXmlFileText;
    public static String XmlManagerViewer_exportXmlFileText;
    public static String XmlManagerViewer_removeXmlFileText;
    public static String XmlManagerViewer_editFileText;
    public static String XmlManagerViewer_importFileDialogText;
    public static String XmlManagerViewer_xmlExtension;
    public static String XmlManagerViewer_allFilesExtension;
    public static String XmlManagerViewer_xmlExtensionName;
    public static String XmlManagerViewer_xmlFilterName;
    public static String XmlManagerViewer_allFilesFilterName;
    public static String XmlManagerViewer_importErrorTitle;
    public static String XmlManagerViewer_importErrorMessage;
    public static String XmlManagerViewer_importErrorSameFile;
    public static String XmlManagerViewer_importErrorInvalidFile;
    public static String XmlManagerViewer_removingXmlAnalysisMessage;
    public static String XmlManagerViewer_removingXmlAnalysisText;

    /** Messages for the {@link XmlFilePropertiesDialog} class */
    public static String XmlFilePropertiesDialog_shellText;
    public static String XmlFilePropertiesDialog_xmlUnsavedChangesText;
    public static String XmlFilePropertiesDialog_xmlUnsavedChangesQuestion;
    public static String XmlFilePropertiesDialog_xmlUnsavedChangesOption1;
    public static String XmlFilePropertiesDialog_xmlUnsavedChangesOption2;
    public static String XmlFilePropertiesDialog_xmlUnsavedChangesOption3;
    public static String XmlFilePropertiesDialog_restoreDefaultsButtonText;
    public static String XmlFilePropertiesDialog_restoreDefaultsQuestion;
    public static String XmlFilePropertiesDialog_restoreDefaultsShellText;
    public static String XmlFilePropertiesDialog_saveChangesButtonText;
    public static String XmlFilePropertiesDialog_IDLabelText;
    public static String XmlFilePropertiesDialog_entryTitleText;
    public static String XmlFilePropertiesDialog_currentPathText;
    public static String XmlFilePropertiesDialog_buildPathButtonText;
    public static String XmlFilePropertiesDialog_resetTextText;
    public static String XmlFilePropertiesDialog_openErrorText;
    public static String XmlFilePropertiesDialog_openErrorSSPB;
    public static String XmlFilePropertiesDialog_openErrorSSPBReason;
    public static String XmlFilePropertiesDialog_graphTitleText;
    public static String XmlFilePropertiesDialog_analysisIDText;
    public static String XmlFilePropertiesDialog_processStatusTitleText;
    public static String XmlFilePropertiesDialog_colorDialogText;
    public static String XmlFilePropertiesDialog_closingWindowDialogText;
    public static String XmlFilePropertiesDialog_closingWindowsDialogQuestion1;
    public static String XmlFilePropertiesDialog_closingWindowDialogQuestion2;
    public static String XmlFilePropertiesDialog_closingWindowDialogYes;
    public static String XmlFilePropertiesDialog_closingWindowDialogNo;

    /** Messages for the {@link XmlAnalysisManagerUtils} class */
    public static String XmlManagerUtils_cutMenuItemText;
    public static String XmlManagerUtils_copyMenuItemText;
    public static String XmlManagerUtils_pasteMenuItemText;
    public static String XmlManagerUtils_selectAllMenuItemText;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
