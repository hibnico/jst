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
package org.hibnet.jst.ui.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;
import org.hibnet.jst.services.JstGrammarAccess;

import com.google.inject.Inject;

/**
 * see http://www.eclipse.org/Xtext/documentation/latest/xtext.html#contentAssist on how to customize content assistant
 */
public class JstProposalProvider extends AbstractJstProposalProvider {

	@Inject
	public JstGrammarAccess jstGrammarAccess;

	private List<TerminalRule> directiveRules = new ArrayList<>();

	@Inject
	public void computeDirectiveRules() {
		List<AbstractRule> allRules = GrammarUtil.allRules(jstGrammarAccess.getGrammar());
		for (AbstractRule rule : allRules) {
			if (rule instanceof TerminalRule && rule.getName().startsWith("DIRECTIVE")) {
				directiveRules.add((TerminalRule) rule.getAlternatives());
			}
		}
	}

	@Override
	public void complete_TEXT(EObject model, RuleCall ruleCall, ContentAssistContext context,
			ICompletionProposalAcceptor acceptor) {
		acceptor.accept(new CompletionProposal("#{  }", context.getOffset(), 0, 3));
		acceptor.accept(new CompletionProposal("#render ()", context.getOffset(), 0, 8));
		acceptor.accept(new CompletionProposal("#if()\n    \n#end", context.getOffset(), 0, 4, null, "#if() #end", null, null));
		acceptor.accept(new CompletionProposal("#if()\n    \n#else\n    \n#end", context.getOffset(), 0, 4, null, "#if() #else #end", null, null));
		acceptor.accept(new CompletionProposal("#if()\n    \n#elseif()\n    \n#else\n    \n#end", context.getOffset(), 0, 4, null, "#if() #elseif() #else #end", null, null));
		acceptor.accept(new CompletionProposal("#for()\n    \n#end", context.getOffset(), 0, 5, null, "#for() #end", null, null));
		acceptor.accept(new CompletionProposal("#while()\n    \n#end", context.getOffset(), 0, 7, null, "#while() #end", null, null));
		acceptor.accept(new CompletionProposal("#do\n    \n#end #while()", context.getOffset(), 0, 21, null, "#do #end #while()", null, null));
		acceptor.accept(new CompletionProposal("$()", context.getOffset(), 0, 2));
		acceptor.accept(new CompletionProposal("$?()", context.getOffset(), 0, 3));
		acceptor.accept(new CompletionProposal("$\\html()", context.getOffset(), 0, 7));
		acceptor.accept(new CompletionProposal("$?\\html()", context.getOffset(), 0, 8));
		acceptor.accept(new CompletionProposal("$\\js()", context.getOffset(), 0, 5));
		acceptor.accept(new CompletionProposal("$?\\js()", context.getOffset(), 0, 6));
		acceptor.accept(new CompletionProposal("$\\xml()", context.getOffset(), 0, 6));
		acceptor.accept(new CompletionProposal("$?\\xml()", context.getOffset(), 0, 7));
		acceptor.accept(new CompletionProposal("$\\java()", context.getOffset(), 0, 7));
		acceptor.accept(new CompletionProposal("$?\\java()", context.getOffset(), 0, 8));
		acceptor.accept(new CompletionProposal("$\\csv()", context.getOffset(), 0, 6));
		acceptor.accept(new CompletionProposal("$?\\csv()", context.getOffset(), 0, 7));
		acceptor.accept(new CompletionProposal("$\\sql()", context.getOffset(), 0, 6));
		acceptor.accept(new CompletionProposal("$?\\sql()", context.getOffset(), 0, 7));
		acceptor.accept(new CompletionProposal("$\\\\()", context.getOffset(), 0, 4));
		acceptor.accept(new CompletionProposal("$?\\\\()", context.getOffset(), 0, 5));
	}
}
