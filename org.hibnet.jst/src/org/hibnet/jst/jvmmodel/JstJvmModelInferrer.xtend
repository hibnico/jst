/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.hibnet.jst.jvmmodel

import com.google.inject.Inject
import java.io.PrintStream
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.hibnet.jst.jst.JstFile

/**
 * <p>Infers a JVM model from the source model.</p> 
 */
class JstJvmModelInferrer extends AbstractModelInferrer {

	@Inject extension JvmTypesBuilder

   	def dispatch void infer(JstFile element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {

   		val simpleName = element.eResource.URI.trimFileExtension.lastSegment.toFirstUpper
   		val qualifiedName = if(element.getPackage != null) 
   				element.getPackage + "." + simpleName
   			else 
   				simpleName

		val javaClass = element.toClass(qualifiedName)

   		acceptor.accept(javaClass).initializeLater [
            for (function : element.functions) {
                members += element.toMethod(function.name, element.newTypeRef(Void::TYPE)) [
                    parameters += element.toParameter(
                        "out",
                        element.newTypeRef(typeof(PrintStream))
                    )
                    body = function.body
                ]
            }
		]
	}

}
