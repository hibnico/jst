/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.hibnet.jst.jvmmodel

import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.compiler.XbaseCompiler
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable
import org.hibnet.jst.jst.RichString
import org.hibnet.jst.jst.RichStringForLoop
import org.hibnet.jst.jst.RichStringInlineExpr

class JstCompiler extends XbaseCompiler {
	
	override protected doInternalToJavaStatement(XExpression expr, ITreeAppendable it, boolean isReferenced) {
		switch expr {
			RichString : {
				for (nestedExpression : expr.expressions) {
					nestedExpression.internalToJavaStatement(it, true)
					newLine
					append('out.print(')
					nestedExpression.internalToJavaExpression(it)
					append(');')
				}
			}
			
			RichStringForLoop : {
				expr.forExpression.internalToJavaStatement(it, true)
                newLine
				val paramType = typeProvider.getTypeForIdentifiable(expr.declaredParam)
				append('''for (final ''')
				serialize(paramType, expr, it);
				append(''' «declareVariable(expr.declaredParam, makeJavaIdentifier(expr.declaredParam.name))» : ''')
				internalToJavaExpression(expr.forExpression, it)
				append(") {").increaseIndentation
					expr.eachExpression.internalToJavaStatement(it, true)
					newLine
					append('out.print(')
					expr.eachExpression.internalToJavaExpression(it)
					append(');')
				decreaseIndentation.newLine.append("}")
			}

            RichStringInlineExpr : {
                expr.expr.internalToJavaStatement(it, true)
                newLine
                append('out.print(')
                if (expr.elvis) {
                    append('org.eclipse.xtext.xbase.lib.ObjectExtensions.operator_elvis(')
                    expr.expr.internalToJavaExpression(it)
                    append(', "")')
                } else {
                    expr.expr.internalToJavaExpression(it)
                }
                append(');')
            }

			default :
				super.doInternalToJavaStatement(expr, it, isReferenced)
		}
	}
	
	override protected internalToConvertedExpression(XExpression obj, ITreeAppendable it) {
		if (hasName(obj))
			append(getName(obj))
		else 
			super.internalToConvertedExpression(obj, it) 
	}
	
}