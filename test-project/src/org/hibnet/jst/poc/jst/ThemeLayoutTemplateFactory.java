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
package org.hibnet.jst.poc.jst;

import javax.annotation.Resource;

import org.hibnet.jst.poc.model.Theme;
import org.hibnet.jst.poc.spring.CommonAnnotationBeanPostProcessor;

public class ThemeLayoutTemplateFactory {

    @Resource
    private CommonAnnotationBeanPostProcessor postProcessor;

    public ThemeLayoutTemplate build(Template contentTemplate, Theme theme) {
        ThemeLayoutTemplate t = new ThemeLayoutTemplate(contentTemplate, theme);
        postProcessor.postProcessAfterInstantiation(t, t.getClass().getName());
        return t;
    }

}
