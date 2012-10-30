/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.hibnet.jst.parser.antlr.internal;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

public class JstLexerImpl extends InternalJstLexer {

	private boolean rawText = false;

	private int directiveStackSize = 0;

	private int parenthesisStackSize = 0;

	public JstLexerImpl() {
		super();
	}

	public JstLexerImpl(CharStream input) {
		super(input);
	}

	public JstLexerImpl(CharStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override
	public void mTokens() throws RecognitionException {
		char next = (char) input.LA(1);
		char nextnext = (char) input.LA(2);
		if (rawText && (next != '#' || nextnext == '#') && (next != '$' || nextnext == '$')) {
			actual_mRULE_TEXT();
		} else if (rawText && next == '#' && nextnext == '*') {
			readMultiLineComment();
		} else if (rawText && next == '#' && nextnext == '-') {
			readSingleLineComment();
		} else {
			rawText = false;
			super.mTokens();
			if (next == '(') {
				parenthesisStackSize++;
			} else if (next == ')') {
				parenthesisStackSize--;
				if (parenthesisStackSize == 0) {
					rawText = true;
				}
			}
			switch (state.type) {
				case RULE_DIRECTIVE_ELSE:
					rawText = true;
					break;
				case RULE_DIRECTIVE_FUNCTION:
				case RULE_DIRECTIVE_FOR:
				case RULE_DIRECTIVE_IF:
					directiveStackSize++;
					break;
				case RULE_DIRECTIVE_END:
					directiveStackSize--;
					if (directiveStackSize > 0) {
						rawText = true;
					}
					break;
			}
		}
	}

	private void readSingleLineComment() {
		state.type = RULE_SL_COMMENT;
		state.channel = DEFAULT_TOKEN_CHANNEL;

		// consume #-
		input.consume();
		input.consume();
		while (true) {
			int next = input.LA(1);
			if (next == -1) {
				return;
			}
			input.consume();
			if (next == '\n' || next == '\r') {
				return;
			}
		}
	}

	private void readMultiLineComment() {
		state.type = RULE_ML_COMMENT;
		state.channel = DEFAULT_TOKEN_CHANNEL;

		// consume #*
		input.consume();
		input.consume();
		while (true) {
			int next = input.LA(1);
			if (next == -1) {
				return;
			}
			input.consume();
			if (next != '*') {
				continue;
			}
			int nextNext = input.LA(1);
			if (nextNext == -1) {
				return;
			}
			input.consume();
			if (nextNext == '#') {
				return;
			}
		}
	}

	private void actual_mRULE_TEXT() throws RecognitionException {
		state.type = RULE_TEXT;
		state.channel = DEFAULT_TOKEN_CHANNEL;

		while (true) {
			int next = input.LA(1);
			if (next == -1) {
				return;
			}
			if (next != '#' && next != '$') {
				input.consume();
			} else {
				int nextNext = input.LA(2);
				if (next == '#' && nextNext == '#' || next == '$' && nextNext == '$') {
					input.consume();
					input.consume();
				} else {
					return;
				}
			}
		}
	}

}
