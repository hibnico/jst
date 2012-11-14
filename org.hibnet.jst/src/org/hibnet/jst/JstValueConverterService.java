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
package org.hibnet.jst;

import javax.inject.Inject;

import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.xbase.conversion.XbaseValueConverterService;

public class JstValueConverterService extends XbaseValueConverterService {

	@Inject
	private RendererIDValueConverter rendererIDValueConverter;

	@Inject
	private DoubleTokenValueConverter doubleTokenValueConverter;

	@Inject
	private EchoEscapeValueConverter echoEscapeValueConverter;

	@Inject
	private EchoElvisEscapeValueConverter echoElvisEscapeValueConverter;

	@ValueConverter(rule = "RendererID")
	public IValueConverter<String> RenderID() {
		return this.rendererIDValueConverter;
	}

	@ValueConverter(rule = "RendererValidID")
	public IValueConverter<String> RenderValidID() {
		return this.rendererIDValueConverter;
	}

	@ValueConverter(rule = "DOLLAR")
	public IValueConverter<String> DOLLAR() {
		return this.doubleTokenValueConverter;
	}

	@ValueConverter(rule = "SHARP")
	public IValueConverter<String> SHARP() {
		return this.doubleTokenValueConverter;
	}

	@ValueConverter(rule = "DIRECTIVE_ECHO_ESCAPE")
	public IValueConverter<String> DIRECTIVE_ECHO_ESCAPE() {
		return this.echoEscapeValueConverter;
	}

	@ValueConverter(rule = "DIRECTIVE_ECHO_ELVIS_ESCAPE")
	public IValueConverter<String> DIRECTIVE_ECHO_ELVIS_ESCAPE() {
		return this.echoElvisEscapeValueConverter;
	}
}
