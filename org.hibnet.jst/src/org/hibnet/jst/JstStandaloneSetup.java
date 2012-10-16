/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.hibnet.jst;

/**
 * Initialization support for running Xtext languages without equinox extension registry
 */
public class JstStandaloneSetup extends JstStandaloneSetupGenerated {

	public static void doSetup() {
		new JstStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}
