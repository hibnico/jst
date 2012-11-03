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

import org.eclipse.xtext.xbase.XExpression
import org.eclipse.xtext.xbase.compiler.XbaseCompiler
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable
import org.hibnet.jst.jst.RichString
import org.hibnet.jst.jst.RichStringIf
import org.hibnet.jst.jst.RichStringForLoop
import org.hibnet.jst.jst.RichStringInlineExpr

class JstCompiler extends XbaseCompiler {
	
	override protected doInternalToJavaStatement(XExpression expr, ITreeAppendable it, boolean isReferenced) {
		switch expr {
			RichString : {
			    var i = 0;
				for (nestedExpression : expr.expressions) {
				    val printable = expr.printables.get(i) && isPrintable(nestedExpression)
				    generatePrintExpr(nestedExpression, it, printable)
                    i = i + 1
				}
			}

            RichStringIf : {
                expr.getIf.internalToJavaStatement(it, true)
                newLine
                append('if (')
                expr.getIf.internalToJavaExpression(it)
                append(') {').increaseIndentation.newLine
                generatePrintExpr(expr.then, it)
                decreaseIndentation.newLine.append("}")
                val elseExpr = expr.getElse()
                if (elseExpr != null) {
                    append(' else {').increaseIndentation.newLine
                    elseExpr.internalToJavaStatement(it, false)
                    decreaseIndentation.newLine.append('}')
                }
            }

			RichStringForLoop : {
				expr.forExpression.internalToJavaStatement(it, true)
                newLine
				val paramType = typeProvider.getTypeForIdentifiable(expr.declaredParam)
				append('''for (final ''')
				serialize(paramType, expr, it);
				val forParam = declareVariable(expr.declaredParam, makeJavaIdentifier(expr.declaredParam.name))
				append(''' «forParam» : ''')
				internalToJavaExpression(expr.forExpression, it)
				append(") {").increaseIndentation
			    generatePrintExpr(expr.eachExpression, it)
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

	def private isPrintable(XExpression e) {
	    switch e {
            RichString : false
            RichStringIf : false
            RichStringForLoop : false
            RichStringInlineExpr : false
            default: true
	    }
	}

    def private generatePrintExpr(XExpression e, ITreeAppendable it) {
        generatePrintExpr(e, it, isPrintable(e))
    }

	def private generatePrintExpr(XExpression e, ITreeAppendable it, boolean printable) {
        e.internalToJavaStatement(it, printable)
        newLine
        if (printable) {
            append('out.print(')
            e.internalToJavaExpression(it)
            append(');')
            newLine
        }
	}

	override protected internalToConvertedExpression(XExpression obj, ITreeAppendable it) {
		if (hasName(obj))
			append(getName(obj))
		else 
			super.internalToConvertedExpression(obj, it) 
	}
	
}