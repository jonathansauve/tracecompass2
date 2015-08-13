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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.TmfXmlUiStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * An utility class to parse an XML analysis file and get the root nodes. Root
 * nodes are ones of the following: {@link TmfXmlStrings#STATE_PROVIDER},
 * {@link TmfXmlUiStrings#TIME_GRAPH_VIEW} and {@link TmfXmlUiStrings#XY_VIEW}.
 *
 * @author Jonathan Sauvé
 *
 */
public class XmlAnalysisManagerParser {

    private static List<Node> fRoot = new ArrayList<>();
    private static NodeList fStateProviderNodes;
    private static NodeList fXyViewNodes;
    private static NodeList fTimeGraphViewNodes;

    /**
     * The XMLParser constructor
     *
     * @param uri
     *            The XML file to parse
     * @throws ParserConfigurationException
     *             Parsing exception
     * @throws SAXException
     *             SAX exception
     * @throws IOException
     *             IO exception
     */
    public XmlAnalysisManagerParser(final String uri) throws ParserConfigurationException, SAXException, IOException {
        IPath path = new Path(uri);
        File xmlFile = path.toFile();
        DocumentBuilderFactory dbFact = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFact.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.normalize();

        fRoot = new ArrayList<>();
        fStateProviderNodes = doc.getDocumentElement().getElementsByTagName(TmfXmlStrings.STATE_PROVIDER);
        fXyViewNodes = doc.getDocumentElement().getElementsByTagName(TmfXmlUiStrings.XY_VIEW);
        fTimeGraphViewNodes = doc.getDocumentElement().getElementsByTagName(TmfXmlUiStrings.TIME_GRAPH_VIEW);

        // Join the nodeLists
        for (int i = 0; fStateProviderNodes != null && i < fStateProviderNodes.getLength(); i++) {
            fRoot.add(fStateProviderNodes.item(i));
        }
        for (int i = 0; fXyViewNodes != null && i < fXyViewNodes.getLength(); i++) {
            fRoot.add(fXyViewNodes.item(i));
        }
        for (int i = 0; fTimeGraphViewNodes != null && i < fTimeGraphViewNodes.getLength(); i++) {
            fRoot.add(fTimeGraphViewNodes.item(i));
        }
    }

    /**
     * Gets a list of the root nodes
     *
     * @return The list of nodes
     */
    public List<Node> getRoots() {
        if (fRoot == null) {
            return null;
        }
        return fRoot;
    }
}
