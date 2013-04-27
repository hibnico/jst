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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor;
import org.eclipse.xtext.ui.editor.utils.TextStyle;
import org.eclipse.xtext.xbase.ui.highlighting.XbaseHighlightingConfiguration;

public class JstHighlightingConfiguration extends XbaseHighlightingConfiguration {

    public final static String TEXT = "jst.text";

    public final static String DIRECTIVE = "jst.directive";

    public final static String ESCAPED = "jst.escaped";

    public final static String DELIMITER = "jst.delimiter";

    public final static String KEYWORD = "jst.keyword";

    @Override
    public void configure(final IHighlightingConfigurationAcceptor acceptor) {
        acceptor.acceptDefaultHighlighting(JstHighlightingConfiguration.TEXT, "Text", staticText());
        acceptor.acceptDefaultHighlighting(JstHighlightingConfiguration.DIRECTIVE, "Directive", staticDirective());
        acceptor.acceptDefaultHighlighting(JstHighlightingConfiguration.ESCAPED, "Escaped", staticEscapedText());
        acceptor.acceptDefaultHighlighting(JstHighlightingConfiguration.DELIMITER, "Delimiter", staticDelimiter());
        acceptor.acceptDefaultHighlighting(JstHighlightingConfiguration.KEYWORD, "Keyword", staticKeyword());
        super.configure(new IHighlightingConfigurationAcceptor() {
            @Override
            public void acceptDefaultHighlighting(String id, String name, TextStyle style) {
                style.setBackgroundColor(new RGB(240, 240, 240));
                acceptor.acceptDefaultHighlighting(id, name, style);
            }
        });
    }

    public TextStyle staticText() {
        TextStyle style = defaultTextStyle().copy();
        style.setColor(new RGB(0, 0, 0));
        return style;
    }

    public TextStyle staticDirective() {
        TextStyle style = defaultTextStyle().copy();
        style.setColor(new RGB(0, 20, 90));
        style.setBackgroundColor(new RGB(240, 240, 240));
        style.setStyle(SWT.BOLD);
        return style;
    }

    public TextStyle staticEscapedText() {
        TextStyle style = defaultTextStyle().copy();
        style.setColor(new RGB(0, 0, 0));
        style.setStyle(SWT.BOLD);
        return style;
    }

    public TextStyle staticDelimiter() {
        TextStyle style = defaultTextStyle().copy();
        style.setColor(new RGB(0, 0, 0));
        style.setBackgroundColor(new RGB(240, 240, 240));
        style.setStyle(SWT.BOLD);
        return style;
    }

    public TextStyle staticKeyword() {
        TextStyle style = keywordTextStyle().copy();
        style.setBackgroundColor(new RGB(240, 240, 240));
        return style;
    }
}
