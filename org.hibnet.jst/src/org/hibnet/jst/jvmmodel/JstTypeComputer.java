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
package org.hibnet.jst.jvmmodel;

import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.annotations.typesystem.XbaseWithAnnotationsTypeComputer;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputationState;
import org.hibnet.jst.jst.RichString;
import org.hibnet.jst.jst.RichStringCall;
import org.hibnet.jst.jst.RichStringDoWhileLoop;
import org.hibnet.jst.jst.RichStringForLoop;
import org.hibnet.jst.jst.RichStringIf;
import org.hibnet.jst.jst.RichStringInlineExpr;
import org.hibnet.jst.jst.RichStringLiteral;
import org.hibnet.jst.jst.RichStringWhileLoop;

public class JstTypeComputer extends XbaseWithAnnotationsTypeComputer {

    @Override
    public void computeTypes(XExpression expression, ITypeComputationState state) {
        if (expression instanceof RichString) {
            _computeTypes((RichString) expression, state);
        } else if (expression instanceof RichStringLiteral) {
            _computeTypes((RichStringLiteral) expression, state);
        } else if (expression instanceof RichStringIf) {
            _computeTypes((RichStringIf) expression, state);
        } else if (expression instanceof RichStringInlineExpr) {
            _computeTypes((RichStringInlineExpr) expression, state);
        } else if (expression instanceof RichStringCall) {
            _computeTypes((RichStringCall) expression, state);
        } else if (expression instanceof RichStringDoWhileLoop) {
            _computeTypes((RichStringDoWhileLoop) expression, state);
        } else if (expression instanceof RichStringForLoop) {
            _computeTypes((RichStringForLoop) expression, state);
        } else if (expression instanceof RichStringWhileLoop) {
            _computeTypes((RichStringWhileLoop) expression, state);
        } else {
            super.computeTypes(expression, state);
        }
    }

    protected void _computeTypes(RichString expression, ITypeComputationState state) {
        super._computeTypes(expression, state);
        state.acceptActualType(getTypeForName(Void.class, state));
    }

    protected void _computeTypes(RichStringLiteral expression, ITypeComputationState state) {
        super._computeTypes(expression, state);
    }

    protected void _computeTypes(RichStringIf expression, ITypeComputationState state) {
        super._computeTypes(expression, state);
    }

    protected void _computeTypes(RichStringInlineExpr expression, ITypeComputationState state) {
        computeTypes(expression.getExpr(), state);
    }

    protected void _computeTypes(RichStringCall expression, ITypeComputationState state) {
        for (XExpression arg : expression.getFeatureCallArguments()) {
            computeTypes(arg, state);
        }
        state.acceptActualType(getTypeForName(Void.class, state));
    }

    protected void _computeTypes(RichStringDoWhileLoop expression, ITypeComputationState state) {
        super._computeTypes(expression, state);
        state.acceptActualType(getTypeForName(Void.class, state));
    }

    protected void _computeTypes(RichStringForLoop expression, ITypeComputationState state) {
        super._computeTypes(expression, state);
        state.acceptActualType(getTypeForName(Void.class, state));
    }

    protected void _computeTypes(RichStringWhileLoop expression, ITypeComputationState state) {
        super._computeTypes(expression, state);
        state.acceptActualType(getTypeForName(Void.class, state));
    }
}
