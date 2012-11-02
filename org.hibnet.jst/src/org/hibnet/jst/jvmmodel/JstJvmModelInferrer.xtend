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
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import org.hibnet.jst.jst.Field
import org.hibnet.jst.jst.JstFile
import org.hibnet.jst.jst.Method
import org.hibnet.jst.jst.Template

/**
 * <p>Infers a JVM model from the source model.</p> 
 */
class JstJvmModelInferrer extends AbstractModelInferrer {

	@Inject extension JvmTypesBuilder

   	def dispatch void infer(JstFile element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {

   		val simpleName = element.eResource.URI.trimFileExtension.lastSegment.toFirstUpper + "JstTemplate"
   		element.simpleName = simpleName

   		acceptor.accept(element).initializeLater [
   		    element.setVisibility(JvmVisibility::PUBLIC)
   		    for (member : element.members) {
   		        if (member instanceof Field) {
   		            val field = (member as Field)
   		            if (field.initialValue != null) {
   		                field.setInitializer(field.initialValue)
   		            }
                } else if (member instanceof Template) {
                    val template = (member as Template)
                    template.setVisibility(JvmVisibility::PUBLIC)
                    template.parameters.add(0, element.toParameter(
                        "out",
                        template.newTypeRef(typeof(PrintStream))
                    ))
                    template.returnType = element.newTypeRef(Void::TYPE)
                    template.body = template.getExpression()
   		        } else if (member instanceof Method) {
                    val method = (member as Method)
                    method.body = method.getExpression()
   		        }
   		    }
   		    if (element.getExtended() != null) {
   		         superTypes += element.newTypeRef(element.getExtended())
   		    }
   		    for (implemented : element.implemented) {
                 superTypes += element.newTypeRef(implemented)   		        
   		    }
		]
	}

}
