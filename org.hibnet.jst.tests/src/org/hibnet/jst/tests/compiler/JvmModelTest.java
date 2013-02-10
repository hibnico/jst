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
package org.hibnet.jst.tests.compiler;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.junit.Test;

public class JvmModelTest extends AbstractTest {

	@Test
	public void testIfCondition() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var test = true; var test2 = null; }");
		template.newLine();
		template.append("#if(test)");
		template.newLine();
		template.append("<h1>Hello</h1>");
		template.newLine();
		template.append("#end");
		template.newLine();
		template.append("#if(test2 == null)");
		template.newLine();
		template.append("<h1>World</h1>");
		template.newLine();
		template.append("#end");
		template.newLine();
		template.append("#end");

		String out = callTemplate(template, "renderMain");

		assertSameOuput("<h1>Hello</h1><h1>World</h1>", out);
	}

	@Test
	public void testConditionOnParameter() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main(String v)");
		template.newLine();
		template.append("#if(v == null)");
		template.newLine();
		template.append("$(v)");
		template.newLine();
		template.append("#else");
		template.newLine();
		template.append("<p>no</p>");
		template.newLine();
		template.append("#end");
		template.newLine();
		template.append("#end");

		String out = callTemplate(template, "renderMain", "test");

		assertSameOuput("<p>no</p>", out);
	}

	@Test
	public void testCondition() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var bool = (1 == 1); }");
		template.newLine();
		template.append("$(bool)");
		template.newLine();
		template.append("#end");

		String out = callTemplate(template, "renderMain");

		assertSameOuput("true", out);
	}

	@Test
	public void testDefaultRender() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer _()");
		template.newLine();
		template.append("#{ var bool = (1 == 1); }");
		template.newLine();
		template.append("$(bool)");
		template.newLine();
		template.append("#end");

		String out = callTemplate(template, "render");

		assertSameOuput("true", out);
	}

}
