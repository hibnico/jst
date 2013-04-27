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
package org.hibnet.jst;

import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.conversion.impl.KeywordAlternativeConverter;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.xbase.lib.StringExtensions;

public class RendererIDValueConverter extends KeywordAlternativeConverter {

    @Override
    public String toString(final String value) throws ValueConverterException {
        return StringExtensions.toFirstLower(super.toString(value).substring(6));
    }

    @Override
    public String toValue(final String string, final INode node) throws ValueConverterException {
        String value = super.toValue(string, node);
        if ("_".equals(value)) {
            return "render";
        }
        return "render" + StringExtensions.toFirstUpper(value);
    }
}
