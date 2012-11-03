/*
 *  Copyright 2012 JST contributors
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
		]
	}

}
