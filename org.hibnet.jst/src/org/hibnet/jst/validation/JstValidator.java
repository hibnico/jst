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
package org.hibnet.jst.validation;

import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.annotations.validation.XbaseWithAnnotationsJavaValidator;

public class JstValidator extends XbaseWithAnnotationsJavaValidator {

	@Override
	protected boolean isImplicitReturn(final XExpression expr) {
		// workaround for https://bugs.eclipse.org/bugs/show_bugs.cgi?id=382879
		return false;
	}

	@Override
	public void checkInnerExpressions(final XExpression expr) {
		// disabled
	}
}
