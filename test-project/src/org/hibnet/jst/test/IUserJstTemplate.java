package org.hibnet.jst.test;

import java.io.Writer;

public interface IUserJstTemplate {

	public void render(Writer out, User user) throws Exception;
}
