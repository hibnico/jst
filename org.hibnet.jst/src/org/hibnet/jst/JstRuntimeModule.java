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

import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.parser.antlr.LexerProvider;
import org.eclipse.xtext.service.SingletonBinding;
import org.eclipse.xtext.xbase.compiler.XbaseCompiler;
import org.eclipse.xtext.xbase.typesystem.computation.ITypeComputer;
import org.eclipse.xtext.xtext.ecoreInference.IXtext2EcorePostProcessor;
import org.hibnet.jst.generator.JstJavaFileGenerator;
import org.hibnet.jst.jvmmodel.JstCompiler;
import org.hibnet.jst.jvmmodel.JstTypeComputer;
import org.hibnet.jst.parser.antlr.internal.InternalJstLexer;
import org.hibnet.jst.parser.antlr.internal.JstLexerImpl;
import org.hibnet.jst.parser.antlr.internal.JstParserImpl;
import org.hibnet.jst.validation.JstValidator;

/**
 * Use this class to register components to be used at runtime / without the Equinox extension registry.
 */
public class JstRuntimeModule extends AbstractJstRuntimeModule {

    @Override
    public Class<? extends IValueConverterService> bindIValueConverterService() {
        return JstValueConverterService.class;
    }

    @Override
    @SingletonBinding(eager = true)
    public Class<? extends JstValidator> bindJstValidator() {
        return JstValidator.class;
    }

    @Override
    public Class<? extends ITypeComputer> bindITypeComputer() {
        return JstTypeComputer.class;
    }

    public Class<? extends XbaseCompiler> bindXbaseCompiler() {
        return JstCompiler.class;
    }

    @Override
    public Class<? extends org.eclipse.xtext.parser.antlr.Lexer> bindLexer() {
        return JstLexerImpl.class;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public com.google.inject.Provider<InternalJstLexer> provideInternalJstLexer() {
        return (LexerProvider) org.eclipse.xtext.parser.antlr.LexerProvider.create(JstLexerImpl.class);
    }

    @Override
    public Class<? extends org.eclipse.xtext.parser.IParser> bindIParser() {
        return JstParserImpl.class;
    }

    public Class<? extends IXtext2EcorePostProcessor> bindIXtext2EcorePostProcessor() {
        return RichStringEclassPostProcessor.class;
    }

    public Class<? extends JstJavaFileGenerator> bindJstJavaFileGenerator() {
        return JstJavaFileGenerator.class;
    }
}
