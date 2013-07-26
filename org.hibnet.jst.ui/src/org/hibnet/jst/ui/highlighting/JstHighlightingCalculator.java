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
package org.hibnet.jst.ui.highlighting;

import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightedPositionAcceptor;
import org.eclipse.xtext.xbase.ui.highlighting.XbaseHighlightingCalculator;
import org.hibnet.jst.services.JstGrammarAccess;

import com.google.inject.Inject;

public class JstHighlightingCalculator extends XbaseHighlightingCalculator {
    @Inject
    private JstGrammarAccess grammarAccess;

    public boolean isRule(ILeafNode node, TerminalRule... expecteds) {
        for (final TerminalRule expected : expecteds) {
            if (node.getGrammarElement() instanceof RuleCall) {
                if (((RuleCall) node.getGrammarElement()).getRule() == expected) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void doProvideHighlightingFor(final XtextResource resource, final IHighlightedPositionAcceptor acceptor) {
        super.doProvideHighlightingFor(resource, acceptor);
        for (final ILeafNode leafNode : resource.getParseResult().getRootNode().getLeafNodes()) {
            if (isRule(leafNode, grammarAccess.getTEXTRule())) {
                acceptor.addPosition(leafNode.getOffset(), leafNode.getLength(), JstHighlightingConfiguration.TEXT);
            } else if (isRule(leafNode, grammarAccess.getABSTRACTRule())) {
                acceptor.addPosition(leafNode.getOffset(), leafNode.getLength(), JstHighlightingConfiguration.KEYWORD);
            } else if (isRule(leafNode, grammarAccess.getDIRECTIVE_ELSERule(), grammarAccess.getDIRECTIVE_ENDRule(),
                    grammarAccess.getDIRECTIVE_DEFRule(), grammarAccess.getDIRECTIVE_MAINRule(),
                    grammarAccess.getDIRECTIVE_RENDERRule())) {
                acceptor.addPosition(leafNode.getOffset(), leafNode.getLength(), JstHighlightingConfiguration.DIRECTIVE);
            } else if (isRule(leafNode, grammarAccess.getDIRECTIVE_SCRIPTRule(), grammarAccess.getDIRECTIVE_ECHORule(),
                    grammarAccess.getDIRECTIVE_ECHO_ELVISRule(), grammarAccess.getDIRECTIVE_ECHO_UNESCAPERule(),
                    grammarAccess.getDIRECTIVE_ECHO_ELVIS_UNESCAPERule(), grammarAccess.getDIRECTIVE_ECHO_ESCAPERule(),
                    grammarAccess.getDIRECTIVE_ECHO_ELVIS_ESCAPERule(), grammarAccess.getDIRECTIVE_ELSEIFRule(),
                    grammarAccess.getDIRECTIVE_FORRule(), grammarAccess.getDIRECTIVE_WHILERule(),
                    grammarAccess.getDIRECTIVE_DORule(), grammarAccess.getDIRECTIVE_IFRule())) {
                acceptor.addPosition(leafNode.getOffset(), leafNode.getLength() - 1,
                        JstHighlightingConfiguration.DIRECTIVE);
                acceptor.addPosition(leafNode.getOffset() + leafNode.getLength() - 1, 1,
                        JstHighlightingConfiguration.DELIMITER);
            } else if (isRule(leafNode, grammarAccess.getPARENTHESE_OPENRule(),
                    grammarAccess.getPARENTHESE_CLOSERule(), grammarAccess.getCURLY_BRACKET_OPENRule(),
                    grammarAccess.getCURLY_BRACKET_CLOSERule())) {
                acceptor.addPosition(leafNode.getOffset(), leafNode.getLength(), JstHighlightingConfiguration.DELIMITER);
            } else if (isRule(leafNode, grammarAccess.getDOLLARRule(), grammarAccess.getSHARPRule())) {
                acceptor.addPosition(leafNode.getOffset(), leafNode.getLength(), JstHighlightingConfiguration.ESCAPED);
            }
        }
    }
}
