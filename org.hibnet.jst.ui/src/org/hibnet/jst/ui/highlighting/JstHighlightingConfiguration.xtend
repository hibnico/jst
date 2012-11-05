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

import org.eclipse.xtext.xbase.ui.highlighting.XbaseHighlightingConfiguration
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor
import org.eclipse.xtext.ui.editor.utils.TextStyle
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.SWT

class JstHighlightingConfiguration extends XbaseHighlightingConfiguration {

    public static val TEXT = 'jst.text'

    public static val DIRECTIVES = 'jst.directives'

    public static val ESCAPED = 'jst.escaped'

	override configure(IHighlightingConfigurationAcceptor acceptor) {
        acceptor.acceptDefaultHighlighting(TEXT, 'Text', staticText)
        acceptor.acceptDefaultHighlighting(DIRECTIVES, 'Directives', staticDirectives)
        acceptor.acceptDefaultHighlighting(ESCAPED, 'Escaped', staticEscapedText)
		super.configure([id, name, style| 
			style.backgroundColor = new RGB(240, 240, 240)
			acceptor.acceptDefaultHighlighting(id, name, style)
		])
	}

    def TextStyle staticText() {
        defaultTextStyle.copy => [
            color = new RGB(0, 0, 0)
        ]
    }

    def TextStyle staticDirectives() {
        defaultTextStyle.copy => [
            color = new RGB(0, 20, 90)
            backgroundColor = new RGB(240, 240, 240)
            style = SWT::BOLD;
        ]
    }

    def TextStyle staticEscapedText() {
        defaultTextStyle.copy => [
            color = new RGB(0, 0, 0)
            style = SWT::BOLD;
        ]
    }

}
