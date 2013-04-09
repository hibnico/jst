package org.hibnet.jst.test;

import java.io.StringWriter;
import java.io.Writer;

public class CachedUserJstTemplate implements IUserJstTemplate {

	private IUserJstTemplate actualTemplate;

	private Cache cache;

	public CachedUserJstTemplate(IUserJstTemplate actualTemplate, Cache cache) {
		this.actualTemplate = actualTemplate;
		this.cache = cache;
	}

	@Override
	public void render(Writer out, User user) throws Exception {
		String userHtml = (String) cache.get(user.id);
		if (userHtml == null) {
			StringWriter w = new StringWriter();
			actualTemplate.render(w, user);
			userHtml = w.toString();
			cache.set(user.id, userHtml);
		}
		out.append(userHtml);
	}

}
