/*******************************************************************************
 * Copyright (c) 2015 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.editors;

import com.google.common.collect.ImmutableSet;


/**
 * Constants for the TMF UI components
 *
 * @noimplement
 */
public interface ITmfEventsEditorConstants {

    /**
     * The trace editor input type.
     */
    String TRACE_EDITOR_INPUT_TYPE = "editorInputType.trace"; //$NON-NLS-1$

    /**
     * The trace editor input type.
     */
    String EXPERIMENT_EDITOR_INPUT_TYPE = "editorInputType.experiment"; //$NON-NLS-1$

    /**
     * Expected editor input type constants for a resource (trace)
     * Some constants are there to make it possible to reopen traces
     * that were opened using Linux Tools and early Trace Compass
     * versions.
     */
    ImmutableSet<String> TRACE_INPUT_TYPE_CONSTANTS =
            ImmutableSet.of(TRACE_EDITOR_INPUT_TYPE,
                            "org.eclipse.linuxtools.tmf.core.trace.TmfTrace", //$NON-NLS-1$
                            "org.eclipse.tracecompass.tmf.core.trace.TmfTrace"); //$NON-NLS-1$
    /**
     * Expected editor input type constants for a resource (experiment)
     * Some constants are there to make it possible to reopen traces
     * that were opened using Linux Tools and early Trace Compass
     * versions.
     */
    ImmutableSet<String> EXPERIMENT_INPUT_TYPE_CONSTANTS =
            ImmutableSet.of(EXPERIMENT_EDITOR_INPUT_TYPE,
            "org.eclipse.linuxtools.tmf.core.trace.TmfExperiment", //$NON-NLS-1$
            "org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment", //$NON-NLS-1$
            "org.eclipse.tracecompass.tmf.core.trace.TmfExperiment"); //$NON-NLS-1$

}
