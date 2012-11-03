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
package org.hibnet.jst.jvmmodel

import com.google.inject.Inject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.common.types.JvmTypeReference
import org.eclipse.xtext.common.types.util.TypeReferences
import org.eclipse.xtext.xbase.typing.XbaseTypeProvider
import org.hibnet.jst.jst.RichString
import org.hibnet.jst.jst.RichStringForLoop

import static org.eclipse.xtext.xbase.XbasePackage$Literals.*
import com.google.inject.Singleton

@Singleton
class JstTypeProvider extends XbaseTypeProvider {
	
	@Inject TypeReferences typeReferences
	
	def dispatch type(RichString string, JvmTypeReference typeRef, boolean isRawTypes) {
		typeReferences.getTypeForName(Void::TYPE, string)
	}
	
	def dispatch type(RichStringForLoop string, JvmTypeReference typeRef, boolean isRawTypes) {
		typeReferences.getTypeForName(Void::TYPE, string)
	}
	
	def dispatch expectedType(RichStringForLoop container, EReference reference, int index, boolean rawType) {
		if (reference == XFOR_LOOP_EXPRESSION__EACH_EXPRESSION)
			typeReferences.getTypeForName(typeof(Object), container)
		else
			super._expectedType(container,reference,index, rawType)
	}
}