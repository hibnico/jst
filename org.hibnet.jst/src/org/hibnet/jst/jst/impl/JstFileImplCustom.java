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

import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class JstFileImplCustom extends JstFileImpl {

	protected Iterable<JvmTypeReference> extendedInterfaces;

	@Override
	public Iterable<JvmTypeReference> getExtendedInterfaces() {
		if (extendedInterfaces == null) {
			extendedInterfaces = Iterables.filter(getSuperTypes(), new Predicate<JvmTypeReference>() {
						@Override
						public boolean apply(JvmTypeReference typeReference) {
							JvmType type = typeReference.getType();
							if (type instanceof JvmGenericType) {
								return ((JvmGenericType) type).isInterface();
							}
							return false;
						}
					});
		}
		return extendedInterfaces;
	}

	@Override
	public JvmTypeReference getExtendedClass() {
		for(JvmTypeReference candidate: getSuperTypes()) {
			if (candidate.getType() instanceof JvmGenericType && !((JvmGenericType) candidate.getType()).isInterface())
				return candidate;
		}
		return null;
	}

	@Override
	public boolean isInstantiateable() {
		return !isAbstract() && !isInterface();
	}

	@Override
	public Iterable<JvmConstructor> getDeclaredConstructors() {
		return Iterables.filter(getMembers(), JvmConstructor.class);
	}

}
