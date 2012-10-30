/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.hibnet.jst.parser.antlr.internal;

import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.hibnet.jst.parser.antlr.JstParser;

public class JstParserImpl extends JstParser {

	@Override
	protected InternalJstParser createParser(XtextTokenStream stream) {
		return new InternalJstParserImpl(stream, getGrammarAccess());
	}
}
