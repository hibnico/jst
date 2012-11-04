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
package org.hibnet.jst.jst.impl;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.xtext.xbase.XExpression;

public class RichStringTemplateRenderImplCustom extends RichStringTemplateRenderImpl {

	@Override
	public String toString() {
		return getExpressionAsString(getMemberCallTarget()) + "." + getConcreteSyntaxFeatureName()
				+ getExpressionsAsString(getMemberCallArguments(), isExplicitOperationCall());
	}

	@Override
	public EList<XExpression> getExplicitArguments() {
		BasicEList<XExpression> result = new BasicEList<XExpression>();
		if (getMemberCallTarget() != null)
			result.add(getMemberCallTarget());
		result.addAll(getMemberCallArguments());
		return result;
	}

	@Override
	public boolean isExplicitOperationCallOrBuilderSyntax() {
		return super.isExplicitOperationCall() || !getMemberCallArguments().isEmpty();
	}

}
