/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Jonathan Sauvé - Adding functionnalities for the XML Manager
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Class containing some utilities for the XML plug-in packages: for example, it
 * manages the XML files and validates them
 *
 * @author Geneviève Bastien
 */
public class XmlUtils {

    /** Sub-directory of the plug-in where XML files are stored */
    private static final String XML_DIRECTORY = "xml_files"; //$NON-NLS-1$

    /** Name of the XSD schema file */
    private static final String XSD = "xmlDefinition.xsd"; //$NON-NLS-1$

    /** Make this class non-instantiable */
    private XmlUtils() {

    }

    /**
     * Get the path where the XML files are stored. Create it if it does not
     * exist
     *
     * @return path to XML files
     */
    public static IPath getXmlFilesPath() {
        IPath path = Activator.getDefault().getStateLocation();
        path = path.addTrailingSeparator().append(XML_DIRECTORY);

        /* Check if directory exists, otherwise create it */
        File dir = path.toFile();
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
        }
        return path;
    }

    /**
     * Validate the XML file input with the XSD schema
     *
     * @param xmlFile
     *            XML file to validate
     * @return True if the XML validates
     */
    public static IStatus xmlValidate(File xmlFile) {
        URL url = XmlUtils.class.getResource(XSD);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source xmlSource = new StreamSource(xmlFile);
        try {
            Schema schema = schemaFactory.newSchema(url);
            Validator validator = schema.newValidator();
            validator.validate(xmlSource);
        } catch (SAXParseException e) {
            String error = NLS.bind(Messages.XmlUtils_XmlParseError, e.getLineNumber(), e.getLocalizedMessage());
            Activator.logError(error);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        } catch (SAXException e) {
            String error = NLS.bind(Messages.XmlUtils_XmlValidationError, e.getLocalizedMessage());
            Activator.logError(error);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        } catch (IOException e) {
            String error = Messages.XmlUtils_XmlValidateError;
            Activator.logError("IO exception occurred", e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        }
        return Status.OK_STATUS;
    }

    /**
     * Adds an XML file to the plugin's path. The XML file should have been
     * validated using the {@link XmlUtils#xmlValidate(File)} method before
     * calling this method.
     *
     * @param fromFile
     *            The XML file to add
     * @return Whether the file was successfully added
     */
    public static IStatus addXmlFile(File fromFile) {

        /* Copy file to path */
        File toFile = getXmlFilesPath().addTrailingSeparator().append(fromFile.getName()).toFile();

        try {
            if (!toFile.exists()) {
                toFile.createNewFile();
            }
        } catch (IOException e) {
            String error = Messages.XmlUtils_ErrorCopyingFile;
            Activator.logError(error, e);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        }

        try (FileInputStream fis = new FileInputStream(fromFile);
                FileOutputStream fos = new FileOutputStream(toFile);
                FileChannel source = fis.getChannel();
                FileChannel destination = fos.getChannel();) {
            destination.transferFrom(source, 0, source.size());
        } catch (IOException e) {
            String error = Messages.XmlUtils_ErrorCopyingFile;
            Activator.logError(error, e);
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, error, e);
        }
        return Status.OK_STATUS;
    }

    /**
     * Deletes an XML file from the plugin's path
     *
     * @param toDelete
     *            The XML file to delete
     * @return Whether the file was successfully deleted
     * @since 2.0
     */
    public static IStatus removeXmlFile(File toDelete) {
        if (!xmlValidate(toDelete).isOK()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "The XML file is not an active XML Analysis"); //$NON-NLS-1$ ;
        }

        toDelete.delete();
        return Status.OK_STATUS;
    }

    /**
     * Get only the XML element children of an XML element.
     *
     * @param parent
     *            The parent element to get children from
     * @return The list of children Element of the parent
     */
    public static List<Element> getChildElements(Element parent) {
        NodeList childNodes = parent.getChildNodes();
        List<Element> childElements = new ArrayList<>();
        for (int index = 0; index < childNodes.getLength(); index++) {
            if (childNodes.item(index).getNodeType() == Node.ELEMENT_NODE) {
                childElements.add((Element) childNodes.item(index));
            }
        }
        return childElements;
    }

    /**
     * Get the XML children element of an XML element, but only those of a
     * certain type
     *
     * @param parent
     *            The parent element to get the children from
     * @param elementTag
     *            The tag of the elements to return
     * @return The list of children {@link Element} of the parent
     */
    public static List<Element> getChildElements(Element parent, String elementTag) {
        /* get the state providers and find the corresponding one */
        NodeList nodes = parent.getElementsByTagName(elementTag);
        List<Element> childElements = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if (node.getParentNode().equals(parent)) {
                childElements.add(node);
            }
        }
        return childElements;
    }

    /**
     * Return the node element corresponding to the requested type in the file.
     *
     * TODO: Nothing prevents from having duplicate type -> id in a same file.
     * That should not be allowed. If you want an element with the same ID as
     * another one, it should be in a different file and we should check it at
     * validation time.
     *
     * @param filePath
     *            The absolute path to the XML file
     * @param elementType
     *            The type of top level element to search for
     * @param elementId
     *            The ID of the desired element
     * @return The XML element or <code>null</code> if not found
     */
    public static Element getElementInFile(String filePath, @NonNull String elementType, @NonNull String elementId) {

        if (filePath == null) {
            return null;
        }

        IPath path = new Path(filePath);
        File file = path.toFile();
        if (file == null || !file.exists() || !file.isFile() || !xmlValidate(file).isOK()) {
            return null;
        }

        try {
            /* Load the XML File */
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;

            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            /* get the state providers and find the corresponding one */
            NodeList nodes = doc.getElementsByTagName(elementType);
            Element foundNode = null;

            for (int i = 0; i < nodes.getLength(); i++) {
                Element node = (Element) nodes.item(i);
                String id = node.getAttribute(TmfXmlStrings.ID);
                if (id.equals(elementId)) {
                    foundNode = node;
                }
            }
            return foundNode;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            return null;
        }
    }

    /**
     * This function allows to save a new value for an attribute in the file
     * passed as parameter.
     *
     * @param copyFile
     *            The XML file
     * @param node
     *            The node to set the new value
     * @param attribute
     *            The attribute to change. If its <code>null</code>, we need to
     *            change the node name
     * @param value
     *            The new value for the attribute, or the node name
     * @throws ParserConfigurationException
     *             Parsing exception
     * @throws IOException
     *             IO exception
     * @throws SAXException
     *             SAX exception
     * @throws TransformerException
     *             Transform exception
     * @return Whether the attribute was successfully setted
     * @since 2.0
     */
    public static IStatus setNewAttribute(File copyFile, Node node, String attribute, String value) throws ParserConfigurationException, SAXException, IOException, TransformerException {

        // Parse the files
        DocumentBuilderFactory dbFact = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFact.newDocumentBuilder();
        Document doc = dBuilder.parse(copyFile);

        // Find the node to be modified
        boolean docChanged = false;
        NodeList nodes = doc.getElementsByTagName(node.getNodeName());
        for (int i = 0; i < nodes.getLength(); i++) {
            Node currentNode = nodes.item(i);
            if (nodes.item(i).isEqualNode(node)) {
                if (attribute != null) {
                    if (nodes.item(i).getAttributes().getNamedItem(attribute) != null) {
                        nodes.item(i).getAttributes().getNamedItem(attribute).setNodeValue(value);
                    }
                } else {
                    Node parent = currentNode.getParentNode();
                    // create a new node with the new NodeName value
                    Element newChild = doc.createElement(value);
                    // copy all the attributes from the oldNode
                    NamedNodeMap attributes = currentNode.getAttributes();
                    for (int j = 0; j < attributes.getLength(); j++) {
                        Node att = attributes.item(j);
                        newChild.setAttribute(att.getNodeName(), att.getNodeValue());
                    }
                    // replace the node by the new one in the parent
                    parent.replaceChild(newChild, currentNode);
                }
                docChanged = true;
                break;
            }
        }

        // update the files
        if (docChanged) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(copyFile.getAbsolutePath()));
            transformer.transform(source, result);
            return new Status(IStatus.OK, Activator.PLUGIN_ID, "The new attribute is set"); //$NON-NLS-1$
        }

        return new Status(IStatus.INFO, Activator.PLUGIN_ID, "Attribute not found - No changes"); //$NON-NLS-1$
    }
}