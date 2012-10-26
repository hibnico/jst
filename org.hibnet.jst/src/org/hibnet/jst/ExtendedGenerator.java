/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.hibnet.jst;

import org.eclipse.xtext.XtextRuntimeModule;
import org.eclipse.xtext.XtextStandaloneSetup;
import org.eclipse.xtext.generator.Generator;
import org.eclipse.xtext.xtext.ecoreInference.IXtext2EcorePostProcessor;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ExtendedGenerator extends Generator {
	public ExtendedGenerator() {
		new XtextStandaloneSetup() {
			@Override
			public Injector createInjector() {
				return Guice.createInjector(new XtextRuntimeModule() {
					@Override
					public Class<? extends IXtext2EcorePostProcessor> bindIXtext2EcorePostProcessor() {
						return RichStringEclassPostProcessor.class;
					}
				});
			}
		}.createInjectorAndDoEMFRegistration();
	}
}
