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
package org.hibnet.jst.poc;

import javax.annotation.Resource;

import org.hibnet.jst.poc.jst.PostListTemplate;
import org.hibnet.jst.poc.jst.PostListTemplateFactory;
import org.hibnet.jst.poc.jst.PostTemplate;
import org.hibnet.jst.poc.jst.PostTemplateFactory;
import org.hibnet.jst.poc.jst.ThemeLayoutTemplate;
import org.hibnet.jst.poc.jst.ThemeLayoutTemplateFactory;
import org.hibnet.jst.poc.model.Theme;
import org.hibnet.jst.poc.model.User;

public class ThemePostListTemplate2 extends AbstractTemplate2 {

    @Resource
    PostTemplateFactory postTemplateFactory;

    @Resource
    PostListTemplateFactory postListTemplateFactory;

    @Resource
    ThemeLayoutTemplateFactory themeLayoutTemplateFactory;

    public void render(User connectedUser, Theme t) {
        PostTemplate postTemplate = postTemplateFactory.build(null);
        PostListTemplate postListTemplate = postListTemplateFactory.build(t.posts, postTemplate);
        ThemeLayoutTemplate themeLayoutTemplate = themeLayoutTemplateFactory.build(postListTemplate, t);
        AbstractPageLayoutTemplate pageLayoutTemplate = getPageTemplate(themeLayoutTemplate, connectedUser);
        pageLayoutTemplate.render();
    }

}
