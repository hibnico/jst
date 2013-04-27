/*
 *  Copyright 2013 JST contributors
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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

                // add an attribute to map the expressions to their printability
                EAttribute printables = EcoreFactory.eINSTANCE.createEAttribute();
                printables.setEType(EcorePackage.eINSTANCE.getEBoolean());
                printables.setName("printables");
                printables.setUnique(false);
                printables.setLowerBound(0);
                printables.setUpperBound(-1);
                c.getEStructuralFeatures().add(printables);

                // add an attribute to the current state of printability of the expression added
                EAttribute printable = EcoreFactory.eINSTANCE.createEAttribute();
                printable.setEType(EcorePackage.eINSTANCE.getEBoolean());
                printable.setName("printable");
                printable.setUnique(false);
                printable.setLowerBound(1);
                printable.setUpperBound(1);
                printable.setDefaultValue(true);
                c.getEStructuralFeatures().add(printable);
            }
        }
    }

}
