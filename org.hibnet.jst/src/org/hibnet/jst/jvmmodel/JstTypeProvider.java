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
package org.hibnet.jst.jvmmodel;

import java.util.Arrays;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.util.TypeReferences;
import org.eclipse.xtext.xbase.XAbstractFeatureCall;
import org.eclipse.xtext.xbase.XAbstractWhileExpression;
import org.eclipse.xtext.xbase.XAssignment;
import org.eclipse.xtext.xbase.XBinaryOperation;
import org.eclipse.xtext.xbase.XBlockExpression;
import org.eclipse.xtext.xbase.XBooleanLiteral;
import org.eclipse.xtext.xbase.XCasePart;
import org.eclipse.xtext.xbase.XCastedExpression;
import org.eclipse.xtext.xbase.XCatchClause;
import org.eclipse.xtext.xbase.XClosure;
import org.eclipse.xtext.xbase.XConstructorCall;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XForLoopExpression;
import org.eclipse.xtext.xbase.XIfExpression;
import org.eclipse.xtext.xbase.XInstanceOfExpression;
import org.eclipse.xtext.xbase.XNullLiteral;
import org.eclipse.xtext.xbase.XNumberLiteral;
import org.eclipse.xtext.xbase.XReturnExpression;
import org.eclipse.xtext.xbase.XStringLiteral;
import org.eclipse.xtext.xbase.XSwitchExpression;
import org.eclipse.xtext.xbase.XThrowExpression;
import org.eclipse.xtext.xbase.XTryCatchFinallyExpression;
import org.eclipse.xtext.xbase.XTypeLiteral;
import org.eclipse.xtext.xbase.XVariableDeclaration;
import org.eclipse.xtext.xbase.XbasePackage.Literals;
import org.eclipse.xtext.xbase.typing.XbaseTypeProvider;
import org.hibnet.jst.jst.RichString;
import org.hibnet.jst.jst.RichStringForLoop;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class JstTypeProvider extends XbaseTypeProvider {

	@Inject
	private TypeReferences typeReferences;

	protected JvmTypeReference _type(RichString string, JvmTypeReference typeRef, boolean isRawTypes) {
		return typeReferences.getTypeForName(Void.TYPE, string);
	}

	protected JvmTypeReference _type(RichStringForLoop string, JvmTypeReference typeRef, boolean isRawTypes) {
		return typeReferences.getTypeForName(Void.TYPE, string);
	}

	protected JvmTypeReference _expectedType(RichStringForLoop container, EReference reference, int index,
			boolean rawType) {
		if (reference == Literals.XFOR_LOOP_EXPRESSION__EACH_EXPRESSION) {
			return typeReferences.getTypeForName(Object.class, container);
		} else {
			return super._expectedType(container, reference, index, rawType);
		}
	}

	@Override
	public JvmTypeReference type(final XExpression featureCall, final JvmTypeReference rawExpectation,
			final boolean rawType) {
		if (featureCall instanceof XFeatureCall) {
			return _type((XFeatureCall) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof RichString) {
			return _type((RichString) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof RichStringForLoop) {
			return _type((RichStringForLoop) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XAbstractFeatureCall) {
			return _type((XAbstractFeatureCall) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XAbstractWhileExpression) {
			return _type((XAbstractWhileExpression) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XBlockExpression) {
			return _type((XBlockExpression) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XBooleanLiteral) {
			return _type((XBooleanLiteral) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XCastedExpression) {
			return _type((XCastedExpression) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XClosure) {
			return _type((XClosure) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XConstructorCall) {
			return _type((XConstructorCall) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XForLoopExpression) {
			return _type((XForLoopExpression) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XIfExpression) {
			return _type((XIfExpression) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XInstanceOfExpression) {
			return _type((XInstanceOfExpression) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XNullLiteral) {
			return _type((XNullLiteral) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XNumberLiteral) {
			return _type((XNumberLiteral) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XReturnExpression) {
			return _type((XReturnExpression) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XStringLiteral) {
			return _type((XStringLiteral) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XSwitchExpression) {
			return _type((XSwitchExpression) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XThrowExpression) {
			return _type((XThrowExpression) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XTryCatchFinallyExpression) {
			return _type((XTryCatchFinallyExpression) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XTypeLiteral) {
			return _type((XTypeLiteral) featureCall, rawExpectation, rawType);
		} else if (featureCall instanceof XVariableDeclaration) {
			return _type((XVariableDeclaration) featureCall, rawExpectation, rawType);
		} else if (featureCall != null) {
			return _type(featureCall, rawExpectation, rawType);
		} else {
			throw new IllegalArgumentException("Unhandled parameter types: "
					+ Arrays.<Object> asList(featureCall, rawExpectation, rawType).toString());
		}
	}

	@Override
	public JvmTypeReference expectedType(final EObject assignment, final EReference reference, final int index,
			final boolean rawType) {
		if (assignment instanceof XAssignment) {
			return _expectedType((XAssignment) assignment, reference, index, rawType);
		} else if (assignment instanceof XBinaryOperation) {
			return _expectedType((XBinaryOperation) assignment, reference, index, rawType);
		} else if (assignment instanceof RichStringForLoop) {
			return _expectedType((RichStringForLoop) assignment, reference, index, rawType);
		} else if (assignment instanceof XAbstractFeatureCall) {
			return _expectedType((XAbstractFeatureCall) assignment, reference, index, rawType);
		} else if (assignment instanceof XAbstractWhileExpression) {
			return _expectedType((XAbstractWhileExpression) assignment, reference, index, rawType);
		} else if (assignment instanceof XBlockExpression) {
			return _expectedType((XBlockExpression) assignment, reference, index, rawType);
		} else if (assignment instanceof XCasePart) {
			return _expectedType((XCasePart) assignment, reference, index, rawType);
		} else if (assignment instanceof XCastedExpression) {
			return _expectedType((XCastedExpression) assignment, reference, index, rawType);
		} else if (assignment instanceof XClosure) {
			return _expectedType((XClosure) assignment, reference, index, rawType);
		} else if (assignment instanceof XConstructorCall) {
			return _expectedType((XConstructorCall) assignment, reference, index, rawType);
		} else if (assignment instanceof XForLoopExpression) {
			return _expectedType((XForLoopExpression) assignment, reference, index, rawType);
		} else if (assignment instanceof XIfExpression) {
			return _expectedType((XIfExpression) assignment, reference, index, rawType);
		} else if (assignment instanceof XReturnExpression) {
			return _expectedType((XReturnExpression) assignment, reference, index, rawType);
		} else if (assignment instanceof XSwitchExpression) {
			return _expectedType((XSwitchExpression) assignment, reference, index, rawType);
		} else if (assignment instanceof XThrowExpression) {
			return _expectedType((XThrowExpression) assignment, reference, index, rawType);
		} else if (assignment instanceof XTryCatchFinallyExpression) {
			return _expectedType((XTryCatchFinallyExpression) assignment, reference, index, rawType);
		} else if (assignment instanceof XVariableDeclaration) {
			return _expectedType((XVariableDeclaration) assignment, reference, index, rawType);
		} else if (assignment instanceof XCatchClause) {
			return _expectedType((XCatchClause) assignment, reference, index, rawType);
		} else if (assignment != null) {
			return _expectedType(assignment, reference, index, rawType);
		} else {
			throw new IllegalArgumentException("Unhandled parameter types: "
					+ Arrays.<Object> asList(assignment, reference, index, rawType).toString());
		}
	}
}
