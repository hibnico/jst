/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.hibnet.jst;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.xtext.GeneratedMetamodel;
import org.eclipse.xtext.xtext.ecoreInference.IXtext2EcorePostProcessor;

public class RichStringEclassPostProcessor implements IXtext2EcorePostProcessor {

	@Override
	public void process(GeneratedMetamodel metamodel) {
		for (EClassifier eclass : metamodel.getEPackage().getEClassifiers()) {
			if (eclass instanceof EClass && eclass.getName().equals("RichString")) {
				EClass c = (EClass) eclass;
				EAttribute printables = EcoreFactory.eINSTANCE.createEAttribute();
				printables.setName("printables");
				printables.setUnique(false);
				printables.setLowerBound(0);
				printables.setUpperBound(-1);
				printables.setEType(EcorePackage.eINSTANCE.getEBoolean());
				c.getEStructuralFeatures().add(printables);
			}
		}
	}

}
