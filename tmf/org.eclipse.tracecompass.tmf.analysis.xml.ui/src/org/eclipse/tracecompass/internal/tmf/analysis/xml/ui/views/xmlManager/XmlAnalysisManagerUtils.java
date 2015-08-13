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

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

/**
 * This class contains some utilities for the XmlAnalysisManagerViewer.
 *
 * @author Jonathan Sauvé
 */
public final class XmlAnalysisManagerUtils {
    /** Make this class non-instantiable */
    private XmlAnalysisManagerUtils() {

    }

    /**
     * Convert an hexadecimal color value to an array of integer. array[0] : red
     * component array[1] : green component array[2] : blue component
     *
     * @param s
     *            The hexa color value (of type #AABBCC)
     * @return An array of red, green and blue component, or null if the
     *         conversion fails
     */
    public static int[] hexaColorToInt(String s) {
        Pattern p = Pattern.compile("#([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})"); //$NON-NLS-1$
        Matcher m = p.matcher(s);
        boolean matches = m.matches();

        int[] components = null;
        if (matches == true) {
            components = new int[3];
            for (int i = 1; i <= m.groupCount(); i++) {
                components[i - 1] = Integer.parseInt(m.group(i), 16);
            }
        }
        return components;
    }

    /**
     * Convert rbg color (int) to an hexadecimal string
     *
     * @param r
     *            The red component
     * @param g
     *            The green component
     * @param b
     *            The blue component
     * @return The hexa value of the rgb
     */
    public static String rgbToHexa(int r, int g, int b) {
        Color color = new Color(r, g, b);
        return String.format("#%06x", color.getRGB() & 0x00FFFFFF); //$NON-NLS-1$
    }

    /**
     * Create a new GridLayout
     *
     * @param numColumns
     *            The number of columns
     * @param marginWidth
     *            The number of pixels of horizontal margin that will be placed
     *            along the left and right edges of the layout.
     * @param marginHeight
     *            The number of pixels of vertical margin that will be placed
     *            along the top and bottom edges of the layout.
     * @return The new GridLayout
     */
    public static GridLayout createGridLayout(int numColumns, int marginWidth, int marginHeight) {
        GridLayout grid = new GridLayout(numColumns, false);
        grid.horizontalSpacing = 0;
        grid.verticalSpacing = 0;
        grid.marginWidth = marginWidth;
        grid.marginHeight = marginHeight;
        return grid;
    }

    /**
     * Add a menu to a <code>Text</code> control. Menu elements : Cut, Copy,
     * Paste & SelectAll
     *
     * @param control
     *            The Text
     */
    public static void addBasicMenuToText(final Text control) {
        Menu menu = new Menu(control);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.XmlManagerUtils_cutMenuItemText);
        item.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                control.cut();
            }
        });
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.XmlManagerUtils_copyMenuItemText);
        item.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                control.copy();
            }
        });
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.XmlManagerUtils_pasteMenuItemText);
        item.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                control.paste();
            }
        });
        item = new MenuItem(menu, SWT.PUSH);
        item.setText(Messages.XmlManagerUtils_selectAllMenuItemText);
        item.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                control.selectAll();
            }
        });

        control.setMenu(menu);
    }
}
