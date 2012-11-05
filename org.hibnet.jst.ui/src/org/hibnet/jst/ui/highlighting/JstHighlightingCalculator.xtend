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
package org.hibnet.jst.ui.highlighting

import com.google.inject.Inject
import org.eclipse.xtext.RuleCall
import org.eclipse.xtext.TerminalRule
import org.eclipse.xtext.nodemodel.ILeafNode
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.xbase.ui.highlighting.XbaseHighlightingCalculator
import org.hibnet.jst.services.JstGrammarAccess

import static org.hibnet.jst.ui.highlighting.JstHighlightingConfiguration.*

class JstHighlightingCalculator extends XbaseHighlightingCalculator {

	@Inject JstGrammarAccess grammarAccess

    def isRule(ILeafNode node, TerminalRule... expecteds) {
        for (expected : expecteds) {
            if (node.getGrammarElement instanceof RuleCall) {
                if ((node.getGrammarElement as RuleCall).rule == expected) {
                    return true;
                }
            }
        }
        return false;
    }

	override doProvideHighlightingFor(XtextResource resource, IHighlightedPositionAcceptor acceptor) {
		super.doProvideHighlightingFor(resource, acceptor)
		for (leafNode : resource.parseResult.rootNode.leafNodes) {

			if (isRule(leafNode, grammarAccess.TEXTRule)) {

				acceptor.addPosition(leafNode.offset, leafNode.length, TEXT)

            } else if (isRule(leafNode, grammarAccess.ABSTRACTRule)) {

                acceptor.addPosition(leafNode.offset, leafNode.length, KEYWORD)

            } else if (isRule(leafNode, grammarAccess.DIRECTIVE_ELSERule,
                                        grammarAccess.DIRECTIVE_ENDRule,
                                        grammarAccess.DIRECTIVE_RENDERERRule,
                                        grammarAccess.DIRECTIVE_RENDERRule)) {

                acceptor.addPosition(leafNode.offset, leafNode.length, DIRECTIVE)

            } else if (isRule(leafNode, grammarAccess.DIRECTIVE_SCRIPTRule,
                                        grammarAccess.DIRECTIVE_ECHORule,
                                        grammarAccess.DIRECTIVE_ECHO_ELVISRule,
                                        grammarAccess.DIRECTIVE_ECHO_UNESCAPERule,
                                        grammarAccess.DIRECTIVE_ECHO_ELVIS_UNESCAPERule,
                                        grammarAccess.DIRECTIVE_ECHO_ESCAPERule,
                                        grammarAccess.DIRECTIVE_ECHO_ELVIS_ESCAPERule,
                                        grammarAccess.DIRECTIVE_ELSEIFRule,
                                        grammarAccess.DIRECTIVE_FORRule,
                                        grammarAccess.DIRECTIVE_IFRule)) {

                acceptor.addPosition(leafNode.offset, leafNode.length - 1, DIRECTIVE)
                acceptor.addPosition(leafNode.offset + leafNode.length - 1, 1, DELIMITER)

            } else if (isRule(leafNode, grammarAccess.PARENTHESE_OPENRule,
                                        grammarAccess.PARENTHESE_CLOSERule,
                                        grammarAccess.CURLY_BRACKET_OPENRule,
                                        grammarAccess.CURLY_BRACKET_CLOSERule)) {

                acceptor.addPosition(leafNode.offset, leafNode.length, DELIMITER)

            } else if (isRule(leafNode, grammarAccess.DOLLARRule,
                                        grammarAccess.SHARPRule)) {

                acceptor.addPosition(leafNode.offset, leafNode.length, ESCAPED)

            }
		}
	}
}
