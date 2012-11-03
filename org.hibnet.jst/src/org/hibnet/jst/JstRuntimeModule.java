/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.hibnet.jst;

import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.parser.antlr.LexerProvider;
import org.eclipse.xtext.service.SingletonBinding;
import org.eclipse.xtext.xbase.compiler.XbaseCompiler;
import org.eclipse.xtext.xbase.typing.ITypeProvider;
import org.eclipse.xtext.xtext.ecoreInference.IXtext2EcorePostProcessor;
import org.hibnet.jst.jvmmodel.JstBatchCompiler;
import org.hibnet.jst.jvmmodel.JstCompiler;
import org.hibnet.jst.jvmmodel.JstTypeProvider;
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

	@SingletonBinding(eager = true)
	public Class<? extends JstValidator> bindJstValidator() {
		return JstValidator.class;
	}

	@Override
	public Class<? extends ITypeProvider> bindITypeProvider() {
		return JstTypeProvider.class;
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

	public Class<? extends JstBatchCompiler> bindJstBatchCompiler() {
		return JstBatchCompiler.class;
	}
}
