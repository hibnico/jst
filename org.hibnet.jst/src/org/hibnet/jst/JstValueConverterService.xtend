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
package org.hibnet.jst

import org.eclipse.xtext.xbase.conversion.XbaseValueConverterService
import org.eclipse.xtext.conversion.ValueConverter
import org.eclipse.xtext.conversion.IValueConverter
import javax.inject.Inject
import org.eclipse.xtext.conversion.impl.KeywordAlternativeConverter
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.nodemodel.INode

class JstValueConverterService extends XbaseValueConverterService {

    @Inject RendererIDValueConverter rendererIDValueConverter

    @ValueConverter(rule = "RendererID")
    def IValueConverter<String> RenderID() {
        return rendererIDValueConverter;
    }

    @ValueConverter(rule = "RendererValidID")
    def IValueConverter<String> RenderValidID() {
        return rendererIDValueConverter;
    }
}

class RendererIDValueConverter extends KeywordAlternativeConverter {

    override toString(String value) throws ValueConverterException {
        super.toString(value).substring(6).toFirstLower
    }
    
    override toValue(String string, INode node) throws ValueConverterException {
        "render" + super.toValue(string, node).toFirstUpper
    }
    
}
