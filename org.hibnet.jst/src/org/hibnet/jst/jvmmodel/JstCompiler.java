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

import java.util.List;

import javax.inject.Inject;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XStringLiteral;
import org.eclipse.xtext.xbase.compiler.XbaseCompiler;
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable;
import org.eclipse.xtext.xbase.controlflow.IEarlyExitComputer;
import org.eclipse.xtext.xbase.featurecalls.IdentifiableSimpleNameProvider;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.hibnet.jst.jst.JstFile;
import org.hibnet.jst.jst.JstOption;
import org.hibnet.jst.jst.RichString;
import org.hibnet.jst.jst.RichStringDoWhileLoop;
import org.hibnet.jst.jst.RichStringForLoop;
import org.hibnet.jst.jst.RichStringIf;
import org.hibnet.jst.jst.RichStringInlineExpr;
import org.hibnet.jst.jst.RichStringLiteral;
import org.hibnet.jst.jst.RichStringRender;
import org.hibnet.jst.jst.RichStringTemplateRender;
import org.hibnet.jst.jst.RichStringWhileLoop;

public class JstCompiler extends XbaseCompiler {

    @Inject
    private IdentifiableSimpleNameProvider featureNameProvider;

    @Inject
    private IEarlyExitComputer earlyExitComputer;

    @Override
    protected void doInternalToJavaStatement(final XExpression expr, final ITreeAppendable it,
            final boolean isReferenced) {

        if (expr instanceof RichString) {
            RichString richString = (RichString) expr;
            int i = 0;
            for (XExpression nestedExpression : richString.getExpressions()) {
                boolean printable = richString.getPrintables().get(i) && isPrintable(nestedExpression);
                this.generatePrintExpr(nestedExpression, it, printable, isEscape(nestedExpression));
                i++;
            }
        } else if (expr instanceof RichStringIf) {
            RichStringIf richStringIf = (RichStringIf) expr;
            internalToJavaStatement(richStringIf.getIf(), it, true);
            it.newLine();
            it.append("if (");
            this.internalToJavaExpression(richStringIf.getIf(), it);
            it.append(") {").increaseIndentation().newLine();
            generatePrintExpr(richStringIf.getThen(), it);
            it.decreaseIndentation().newLine().append("}");
            XExpression elseExpr = richStringIf.getElse();
            if (richStringIf.getElse() != null) {
                it.append(" else {").increaseIndentation().newLine();
                internalToJavaStatement(elseExpr, it, false);
                it.decreaseIndentation().newLine().append("}");
            }
        } else if (expr instanceof RichStringForLoop) {
            RichStringForLoop richStringForLoop = (RichStringForLoop) expr;
            internalToJavaStatement(richStringForLoop.getForExpression(), it, true);
            it.newLine();
            JvmTypeReference paramType = getTypeProvider().getTypeForIdentifiable(richStringForLoop.getDeclaredParam());
            it.append("for (final ");
            serialize(paramType, richStringForLoop, it);
            String forParam = it.declareVariable(richStringForLoop.getDeclaredParam(),
                    makeJavaIdentifier(richStringForLoop.getDeclaredParam().getName()));
            it.append(" ").append(forParam).append(" : ");
            internalToJavaExpression(richStringForLoop.getForExpression(), it);
            it.append(") {").increaseIndentation();
            generatePrintExpr(richStringForLoop.getEachExpression(), it);
            it.decreaseIndentation().newLine().append("}");
        } else if (expr instanceof RichStringWhileLoop) {
            RichStringWhileLoop richStringWhileLoop = (RichStringWhileLoop) expr;
            internalToJavaStatement(richStringWhileLoop.getPredicate(), it, true);
            String varName = it.declareSyntheticVariable(expr, "_while");
            it.newLine().append("boolean ").append(varName).append(" = ");
            internalToJavaExpression(richStringWhileLoop.getPredicate(), it);
            it.append(";");
            it.newLine().append("while (");
            it.append(varName);
            it.append(") {").increaseIndentation();
            it.openPseudoScope();
            generatePrintExpr(richStringWhileLoop.getBody(), it);
            internalToJavaStatement(richStringWhileLoop.getPredicate(), it, true);
            it.newLine();
            if (!earlyExitComputer.isEarlyExit(richStringWhileLoop.getBody())) {
                it.append(varName).append(" = ");
                internalToJavaExpression(richStringWhileLoop.getPredicate(), it);
                it.append(";");
            }
            it.closeScope();
            it.decreaseIndentation().newLine().append("}");
        } else if (expr instanceof RichStringDoWhileLoop) {
            RichStringDoWhileLoop richStringDoWhileLoop = (RichStringDoWhileLoop) expr;
            String variable = it.declareSyntheticVariable(expr, "_dowhile");
            it.newLine().append("boolean ").append(variable).append(" = false;");
            it.newLine().append("do {").increaseIndentation();
            generatePrintExpr(richStringDoWhileLoop.getBody(), it);
            internalToJavaStatement(richStringDoWhileLoop.getPredicate(), it, true);
            it.newLine();
            if (!earlyExitComputer.isEarlyExit(richStringDoWhileLoop.getBody())) {
                it.append(variable).append(" = ");
                internalToJavaExpression(richStringDoWhileLoop.getPredicate(), it);
                it.append(";");
            }
            it.decreaseIndentation().newLine().append("} while(");
            it.append(variable);
            it.append(");");
        } else if (expr instanceof RichStringInlineExpr) {
            RichStringInlineExpr richStringInlineExpr = (RichStringInlineExpr) expr;
            internalToJavaStatement(richStringInlineExpr.getExpr(), it, true);
            it.newLine();
            if (richStringInlineExpr.isUnescape() || richStringInlineExpr.isElvisUnescape()) {
                it.append(getEscapeMethod(null) + "(out, ");
            } else if (richStringInlineExpr.getEscape() != null) {
                it.append(getEscapeMethod(richStringInlineExpr.getEscape()) + "(out, ");
            } else if (richStringInlineExpr.getElvisEscape() != null) {
                it.append(getEscapeMethod(richStringInlineExpr.getElvisEscape()) + "(out, ");
            } else {
                String escape = getEscapeOption((JstFile) EcoreUtil.getRootContainer(expr));
                it.append(getEscapeMethod(escape));
                it.append("(out, ");
            }
            internalToJavaExpression(richStringInlineExpr.getExpr(), it);
            it.append(", ");
            it.append(Boolean.valueOf(
                    richStringInlineExpr.isElvis() || richStringInlineExpr.isElvisUnescape()
                            || richStringInlineExpr.getElvisEscape() != null).toString());
            it.append(");");
        } else if (expr instanceof RichStringRender) {
            RichStringRender richStringRender = (RichStringRender) expr;
            String name = featureNameProvider.getSimpleName(richStringRender.getFeature());
            it.append(name);
            it.append("(out");
            List<XExpression> arguments = richStringRender.getFeatureCallArguments();
            if (!arguments.isEmpty()) {
                it.append(", ");
                this.appendArguments(arguments, it, true);
            }
            it.append(");");
        } else if (expr instanceof RichStringTemplateRender) {
            RichStringTemplateRender richStringTemplateRender = (RichStringTemplateRender) expr;
            XExpression receiver = richStringTemplateRender.getMemberCallTarget();
            prepareExpression(receiver, it);
            List<XExpression> arguments = richStringTemplateRender.getMemberCallArguments();
            for (XExpression arg : arguments) {
                prepareExpression(arg, it);
            }
            it.newLine();
            internalToJavaExpression(receiver, it);
            it.append(".");
            // TODO
            // appendTypeArgument(richStringTemplateRender, it)
            String name = featureNameProvider.getSimpleName(richStringTemplateRender.getFeature());
            it.append(name);
            it.append("(out");
            if (!arguments.isEmpty()) {
                it.append(", ");
                this.appendArguments(arguments, it, true);
            }
            it.append(");");
        } else {
            super.doInternalToJavaStatement(expr, it, isReferenced);
        }
    }

    private String getEscapeMethod(String escape) {
        if (escape == null) {
            return "org.hibnet.jst.runtime.JstEscape.unescape";
        }
        return "org.hibnet.jst.runtime.JstEscape.escape" + StringExtensions.toFirstUpper(escape);
    }

    private String getEscapeOption(JstFile jstFile) {
        String fileName = jstFile.eResource().getURI().trimFileExtension().lastSegment();
        final String defaultEscape;
        int i = fileName.lastIndexOf('.');
        if (i != -1) {
            defaultEscape = fileName.substring(i + 1, fileName.length());
            fileName = fileName.substring(0, i);
        } else {
            defaultEscape = null;
        }

        if (defaultEscape != null) {
            return defaultEscape;
        }

        for (JstOption option : jstFile.getOptions()) {
            if (option.getKey().equals("escape") && option.getValue() instanceof XStringLiteral) {
                return ((XStringLiteral) option.getValue()).getValue();
            }
        }
        return null;
    }

    private boolean isPrintable(XExpression e) {
        if (e instanceof RichString || e instanceof RichStringIf || e instanceof RichStringForLoop
                || e instanceof RichStringWhileLoop || e instanceof RichStringDoWhileLoop
                || e instanceof RichStringInlineExpr || e instanceof RichStringRender
                || e instanceof RichStringTemplateRender) {
            return false;
        }
        return true;
    }

    private boolean isEscape(XExpression e) {
        if (e instanceof RichStringLiteral) {
            return false;
        }
        return true;
    }

    private void generatePrintExpr(XExpression e, ITreeAppendable it) {
        generatePrintExpr(e, it, isPrintable(e), isEscape(e));
    }

    private void generatePrintExpr(XExpression e, ITreeAppendable it, boolean printable, boolean doEscape) {
        internalToJavaStatement(e, it, printable);
        it.newLine();
        if (printable) {
            String escape = null;
            if (doEscape) {
                escape = getEscapeOption((JstFile) EcoreUtil.getRootContainer(e));
            }
            it.append(getEscapeMethod(escape));
            it.append("(out, ");
            internalToJavaExpression(e, it);
            it.append(", false);");
            it.newLine();
        }
    }

    @Override
    protected void internalToConvertedExpression(final XExpression obj, final ITreeAppendable it) {
        if (it.hasName(obj)) {
            it.append(it.getName(obj));
        } else {
            super.internalToConvertedExpression(obj, it);
        }
    }
}
