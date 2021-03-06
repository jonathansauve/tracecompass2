/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.pcap.core.tests.perf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Run all performance test suites.
 *
 * @author Vincent Perot
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        org.eclipse.tracecompass.pcap.core.tests.perf.trace.AllTests.class
})
public class AllPerfTests {

}
