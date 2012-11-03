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
