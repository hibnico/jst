/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.hibnet.jst.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration;
import org.eclipse.xtext.ui.editor.syntaxcoloring.ISemanticHighlightingCalculator;
import org.hibnet.jst.ui.contentassist.antlr.internal.JstLexerImpl;
import org.hibnet.jst.ui.highlighting.JstHighlightingCalculator;
import org.hibnet.jst.ui.highlighting.JstHighlightingConfiguration;

/**
 * Use this class to register components to be used within the IDE.
 */
public class JstUiModule extends org.hibnet.jst.ui.AbstractJstUiModule {

	public JstUiModule(AbstractUIPlugin plugin) {
		super(plugin);
	}

	@Override
	public Class<? extends ISemanticHighlightingCalculator> bindISemanticHighlightingCalculator() {
		return JstHighlightingCalculator.class;
	}

	@Override
	public Class<? extends IHighlightingConfiguration> bindIHighlightingConfiguration() {
		return JstHighlightingConfiguration.class;
	}

	@Override
	public Class<? extends IContentOutlinePage> bindIContentOutlinePage() {
		return null;
	}

	@Override
	public void configureContentAssistLexerProvider(com.google.inject.Binder binder) {
		binder.bind(org.hibnet.jst.ui.contentassist.antlr.internal.InternalJstLexer.class).toProvider(
				org.eclipse.xtext.parser.antlr.LexerProvider.create(JstLexerImpl.class));
	}

	@Override
	public void configureContentAssistLexer(com.google.inject.Binder binder) {
		binder.bind(org.eclipse.xtext.ui.editor.contentassist.antlr.internal.Lexer.class)
				.annotatedWith(com.google.inject.name.Names.named(org.eclipse.xtext.ui.LexerUIBindings.CONTENT_ASSIST))
				.to(JstLexerImpl.class);
	}
}
