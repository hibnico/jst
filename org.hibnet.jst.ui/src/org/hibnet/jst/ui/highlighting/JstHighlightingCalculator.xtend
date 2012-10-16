/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.hibnet.jst.ui.highlighting

import com.google.inject.Inject
import org.eclipse.xtext.RuleCall
import org.eclipse.xtext.nodemodel.ILeafNode
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.xbase.ui.highlighting.XbaseHighlightingCalculator
import org.hibnet.jst.services.JstGrammarAccess

import static org.hibnet.jst.ui.highlighting.JstHighlightingConfiguration.*

class JstHighlightingCalculator extends XbaseHighlightingCalculator {

	@Inject JstGrammarAccess grammarAccess

	def isText(ILeafNode node) {
		switch grammarElement: node.getGrammarElement {
			RuleCall: 
				grammarElement.rule == grammarAccess.TEXTRule
			default: 
				false
		}
	}

	override doProvideHighlightingFor(XtextResource resource, IHighlightedPositionAcceptor acceptor) {
		super.doProvideHighlightingFor(resource, acceptor)
		acceptor.addPosition(0, 4, TEXT)
		acceptor.addPosition(4, 1, ESCAPE)
		for (leafNode : resource.parseResult.rootNode.leafNodes) {
			if (isText(leafNode)) {
				acceptor.addPosition(leafNode.offset, 1, ESCAPE)
				acceptor.addPosition(leafNode.offset + 1, leafNode.length - 2, TEXT)
				acceptor.addPosition((leafNode.offset + leafNode.length) - 1, 1, ESCAPE)
			}
		}
	}
}
