/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event;

import java.util.Collections;
import java.util.Set;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFStream;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInputReader;

/**
 * A lost event definition
 *
 * @author Matthew Khouzam
 * @since 1.0
 */
public class LostEventDeclaration implements IEventDeclaration {

    /**
     * Id of lost events
     *
     * @since 1.0
     */
    public static final long LOST_EVENT_ID = -1L;

    /**
     * Gets a "lost" event. This is a synthetic event that is there to show that
     * there should be something there.
     */
    public static final LostEventDeclaration INSTANCE = new LostEventDeclaration();

    private final StructDeclaration fFields = new StructDeclaration(0);

    private LostEventDeclaration() {
        getFields().addField(CTFStrings.LOST_EVENTS_FIELD, IntegerDeclaration.UINT_32B_DECL);
        getFields().addField(CTFStrings.LOST_EVENTS_DURATION, IntegerDeclaration.UINT_64B_DECL);
    }

    @Override
    public EventDefinition createDefinition(CTFStreamInputReader streamInputReader, BitBuffer input, long timestamp) throws CTFException {
        return null;
    }

    @Override
    public String getName() {
        return CTFStrings.LOST_EVENT_NAME;
    }

    @Override
    public StructDeclaration getFields() {
        return fFields;
    }

    @Override
    public StructDeclaration getContext() {
        return null;
    }

    @Override
    public Long getId() {
        return LOST_EVENT_ID;
    }

    @Override
    public CTFStream getStream() {
        return null;
    }

    @Override
    public long getLogLevel() {
        return 0;
    }

    @Override
    public Set<String> getCustomAttributes() {
        return Collections.<String> emptySet();
    }

    @Override
    public String getCustomAttribute(String key) {
        return null;
    }

}
