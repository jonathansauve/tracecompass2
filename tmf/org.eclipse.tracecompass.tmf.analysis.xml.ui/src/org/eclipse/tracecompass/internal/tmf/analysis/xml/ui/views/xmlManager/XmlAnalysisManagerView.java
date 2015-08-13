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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * This view displays the imported XML analysis. It allows the user to import,
 * export and remove analysis. Also, the edit function opens a dialog to easily
 * modifiate attribute from the analysis.
 *
 * @author Jonathan Sauvé
 */
public class XmlAnalysisManagerView extends TmfView {

    private final static String ID = "org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.xmlManager"; //$NON-NLS-1$
    XmlAnalysisManagerViewer fViewer;

    /**
     * Constructor
     */
    public XmlAnalysisManagerView() {
        super(ID);
    }

    @Override
    public void createPartControl(Composite parent) {
        fViewer = new XmlAnalysisManagerViewer(parent);
    }

    @Override
    public void setFocus() {
        fViewer.setFocus();
    }
}
