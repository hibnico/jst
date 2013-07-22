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

import org.hibnet.jst.poc.model.Post;

public class PostTemplate implements Template {

    public Post post;

    @Override
    public void render() {
        System.out.println("render post: " + post.content);
    }

    public static void render(final Post post) {
        PostTemplate postTemplate = new PostTemplate();
        postTemplate.post = post;
        postTemplate.render();
    }
}
