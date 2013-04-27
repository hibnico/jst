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
package org.hibnet.jst.parser.antlr.internal;

import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.hibnet.jst.jst.JstPackage;
import org.hibnet.jst.jst.RichString;
import org.hibnet.jst.services.JstGrammarAccess;

public class InternalJstParserImpl extends InternalJstParser {

    public InternalJstParserImpl(TokenStream input) {
        super(input);
    }

    public InternalJstParserImpl(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public InternalJstParserImpl(TokenStream input, JstGrammarAccess grammarAccess) {
        super(input, grammarAccess);
    }

    @Override
    protected void associateNodeWithAstElement(ICompositeNode node, EObject astElement) {
        super.associateNodeWithAstElement(node, astElement);
        // listen to the add of expressions to a rich string to maintain the printability state of them
        if (astElement instanceof RichString) {
            astElement.eAdapters().add(new AdapterImpl() {
                @Override
                public void notifyChanged(Notification notification) {
                    RichString richString = (RichString) notification.getNotifier();
                    if (notification.getEventType() == Notification.ADD) {
                        if (notification.getFeatureID(null) == JstPackage.RICH_STRING__EXPRESSIONS) {
                            richString.getPrintables().add(richString.isPrintable());
                        } else if (notification.getFeatureID(null) == JstPackage.RICH_STRING__START_SCRIPT) {
                            richString.setPrintable(false);
                        } else if (notification.getFeatureID(null) == JstPackage.RICH_STRING__END_SCRIPT) {
                            richString.setPrintable(true);
                        }
                    }
                }
            });
        }
    }
}
