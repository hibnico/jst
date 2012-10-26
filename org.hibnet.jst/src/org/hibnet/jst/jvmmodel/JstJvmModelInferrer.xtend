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
import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.XBlockExpression
import org.eclipse.xtext.EcoreUtil2$EClassTypeHierarchyComparator
import org.eclipse.xtext.EcoreUtil2
import org.hibnet.jst.jst.RichString
import org.hibnet.jst.jst.RichStringScript
import java.util.Collections

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
                    popupRichStringScripts(function.body)
                    body = function.body
                ]
            }
		]
	}

    /**
     * The RichStringScript shouldn't encapsulated in its own block but should ba part of the encosing block
     */
    def private void popupRichStringScripts(XBlockExpression root) {
        val richStrings = EcoreUtil2::eAllOfType(root, typeof(RichString))
        for (richString : richStrings) {
            val newExpressions = <XExpression>newArrayList()
            for (expr : richString.expressions) {
                if (expr instanceof RichStringScript) {
                    for (nested : (expr as RichStringScript).expressions) {
                        newExpressions += nested;
                        richString.printables += false
                    }
                } else {
                    newExpressions += expr;
                    richString.printables += true
                }
            }
            richString.expressions.retainAll(Collections::emptyList)
            richString.expressions.addAll(newExpressions)
        }
    }
}
