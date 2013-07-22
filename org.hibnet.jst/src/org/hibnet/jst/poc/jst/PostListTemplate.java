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

import java.util.List;

import org.hibnet.jst.poc.model.Post;

public class PostListTemplate implements Template {

    public List<Post> posts;

    public PostTemplate postTemplate = new PostTemplate();

    @Override
    public void render() {
        System.out.println("<ul class='postlist'>");
        for (Post post : posts) {
            System.out.println("<li>");
            // #render postRenderer(post = post)
            postTemplate.post = post;
            postTemplate.render();
            System.out.println("</li>");
        }
        System.out.println("</ul>");
    }

    public static void render(List<Post> posts) {
        PostListTemplate postListTemplate = new PostListTemplate();
        postListTemplate.posts = posts;
        postListTemplate.render();
    }
}
