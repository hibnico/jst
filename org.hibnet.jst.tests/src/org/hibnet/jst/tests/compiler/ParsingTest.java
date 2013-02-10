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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.junit.Assert;
import org.junit.Test;

public class ParsingTest extends AbstractTest {

	@Test
	public void testParseAndCompile_Simple() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("Hello World");
		template.newLine();
		template.append("#end");

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("Hello World", out.trim());
	}

	@Test
	public void testParseAndCompile_Dollar() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("$$ ##");
		template.newLine();
		template.append("#end");

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("$ #", out.trim());
	}

	@Test
	public void testParseAndCompile_If() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#if(true)");
		template.newLine();
		template.append("<h1>Hello</h1>");
		template.newLine();
		template.append("#end");
		template.newLine();
		template.append("#end");

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("<h1>Hello</h1>", out.trim());
	}

	@Test
	public void testParseAndCompile_IfElse() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#if(true)");
		template.newLine();
		template.append("<p>ok</p>");
		template.newLine();
		template.append("#else");
		template.newLine();
		template.append("<p>nok</p>");
		template.newLine();
		template.append("#end");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("<p>ok</p>", out.trim());
	}

	@Test
	public void testParseAndCompile_IfElseIf() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#if(false)");
		template.newLine();
		template.append("<h1>nok</h1>");
		template.newLine();
		template.append("#elseif(true)");
		template.newLine();
		template.append("<h2>ok</h2>");
		template.newLine();
		template.append("#else");
		template.newLine();
		template.append("<p>nok</p>");
		template.newLine();
		template.append("#end");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("<h2>ok</h2>", out.trim());
	}

	@Test
	public void testParseAndCompile_Inline() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("<i>$(\"Hello\")</i>");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("<i>Hello</i>", out.trim());
	}

	@Test
	public void testParseAndCompile_Param() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main(String name)");
		template.newLine();
		template.append("Hello $(name)");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain", "John");

		Assert.assertEquals("Hello John", out.trim());
	}

	@Test
	public void testParseAndCompile_Script() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var name = \"Foo\";}");
		template.newLine();
		template.append("<title>$(name)</title>");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("<title>Foo</title>", out.trim());
	}

	@Test
	public void testParseAndCompile_InlineElvis() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var name = \"Foo\";}");
		template.newLine();
		template.append("#{ var name2 = null;}");
		template.newLine();
		template.append("<h1>$(name)</h1>");
		template.newLine();
		template.append("<h2>$?(name)</h2>");
		template.newLine();
		template.append("<h3>$(name2)</h3>");
		template.newLine();
		template.append("<h4>$?(name2)</h4>");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		StringConcatenation expected = new StringConcatenation();
		expected.append("<h1>Foo</h1>");
		expected.newLine();
		expected.append("<h2>Foo</h2>");
		expected.newLine();
		expected.append("<h3>null</h3>");
		expected.newLine();
		expected.append("<h4></h4>");

		Assert.assertEquals(expected.toString(), out.trim());
	}

	@Test
	public void testParseAndCompile_InlineUnescape() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var name = \"Foo\";}");
		template.newLine();
		template.append("#{ var name2 = null;}");
		template.newLine();
		template.append("<h1>$\\\\(name)</h1>");
		template.newLine();
		template.append("<h2>$?\\\\(name)</h2>");
		template.newLine();
		template.append("<h3>$\\\\(name2)</h3>");
		template.newLine();
		template.append("<h4>$?\\\\(name2)</h4>");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		StringConcatenation expected = new StringConcatenation();
		expected.append("<h1>Foo</h1>");
		expected.newLine();
		expected.append("<h2>Foo</h2>");
		expected.newLine();
		expected.append("<h3>null</h3>");
		expected.newLine();
		expected.append("<h4></h4>");
		Assert.assertEquals(expected.toString(), out.trim());
	}

	@Test
	public void testParseAndCompile_InlineEscape() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var html = \"<&\u00E9a>a\";}");
		template.newLine();
		template.append("#{ var xml = \"<&\u00E9a>a\";}");
		template.newLine();
		template.append("#{ var js = \"call(\'\')\";}");
		template.newLine();
		template.append("<html>$\\\\(html)</html>");
		template.newLine();
		template.append("<html>$\\html(html)</html>");
		template.newLine();
		template.append("<xml>$\\\\(xml)</xml>");
		template.newLine();
		template.append("<xml>$\\xml(xml)</xml>");
		template.newLine();
		template.append("<js>$\\\\(js)</js>");
		template.newLine();
		template.append("<js>$\\js(js)</js>");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		StringConcatenation expected = new StringConcatenation();
		expected.append("<html><&\u00E9a>a</html>");
		expected.newLine();
		expected.append("<html>&lt;&amp;&eacute;a&gt;a</html>");
		expected.newLine();
		expected.append("<xml><&\u00E9a>a</xml>");
		expected.newLine();
		expected.append("<xml>&lt;&amp;&#233;a&gt;a</xml>");
		expected.newLine();
		expected.append("<js>call(\'\')</js>");
		expected.newLine();
		expected.append("<js>call(\\\'\\\')</js>");
		expected.newLine();

		assertSameOuput(expected, out);
	}

	@Test
	public void testParseAndCompile_Escape() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("template with escape = \'html\';");
		template.newLine();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var html = \"<&\u00E9a>a\";}");
		template.newLine();
		template.append("<html>$\\\\(html)</html>");
		template.newLine();
		template.append("<html>$\\html(html)</html>");
		template.newLine();
		template.append("<html>$(html)</html>");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		StringConcatenation expected = new StringConcatenation();
		expected.append("<html><&\u00E9a>a</html>");
		expected.newLine();
		expected.append("<html>&lt;&amp;&eacute;a&gt;a</html>");
		expected.newLine();
		expected.append("<html>&lt;&amp;&eacute;a&gt;a</html>");
		expected.newLine();

		assertSameOuput(expected, out);
	}

	@Test
	public void testParseAndCompile_MultiLineComment() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#* some comment *#");
		template.newLine();
		template.append("Hello World");
		template.newLine();
		template.append("#* some");
		template.newLine();
		template.append("multiline ");
		template.newLine();
		template.append("comment *#");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("Hello World", out.trim());
	}

	@Test
	public void testParseAndCompile_SingleLineComment() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append(" ");
		template.append("#renderer main()");
		template.newLine();
		template.append("#- some comment");
		template.newLine();
		template.append("Hello World");
		template.newLine();
		template.append("#- some other comment");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("Hello World", out.trim());
	}

	@Test
	public void testParseAndCompile_For() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var list = newArrayList(\"one\", \"two\", \"three\", \"four\"); }");
		template.newLine();
		template.append("<html>");
		template.newLine();
		template.append("#for(String element : list)");
		template.newLine();
		template.append("<p>$(element)</p>");
		template.newLine();
		template.append("#end");
		template.newLine();
		template.append("</html>");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		StringConcatenation expected = new StringConcatenation();
		expected.append("<html>");
		expected.newLine();
		expected.append("<p>one</p>");
		expected.newLine();
		expected.append("<p>two</p>");
		expected.newLine();
		expected.append("<p>three</p>");
		expected.newLine();
		expected.append("<p>four</p>");
		expected.newLine();
		expected.append("</html>");

		assertSameOuput(expected, out);
	}

	@Test
	public void testParseAndCompile_While() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var count = 0; }");
		template.newLine();
		template.append("<html>");
		template.newLine();
		template.append("#while(count < 3)");
		template.newLine();
		template.append("<p>$(count)</p>");
		template.newLine();
		template.append("#{ count = count + 1; }");
		template.newLine();
		template.append("#end");
		template.newLine();
		template.append("</html>");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		StringConcatenation expected = new StringConcatenation();
		expected.append("<html>");
		expected.newLine();
		expected.append("<p>0</p>");
		expected.newLine();
		expected.append("<p>1</p>");
		expected.newLine();
		expected.append("<p>2</p>");
		expected.newLine();
		expected.append("</html>");

		assertSameOuput(expected, out);
	}

	@Test
	public void testParseAndCompile_DoWhile() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var count = 0; }");
		template.newLine();
		template.append("<html>");
		template.newLine();
		template.append("#do");
		template.newLine();
		template.append("<p>$(count)</p>");
		template.newLine();
		template.append("#{ count = count + 1; }");
		template.newLine();
		template.append("#end #while(count < 3)");
		template.newLine();
		template.append("</html>");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		StringConcatenation expected = new StringConcatenation();
		expected.append("<html>");
		expected.newLine();
		expected.append("<p>0</p>");
		expected.newLine();
		expected.append("<p>1</p>");
		expected.newLine();
		expected.append("<p>2</p>");
		expected.newLine();
		expected.append("</html>");

		assertSameOuput(expected, out);
	}

	@Test
	public void testParseAndCompile_Complex() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var nullString = null;");
		template.newLine();
		template.append("var name = \"Foo\";");
		template.newLine();
		template.append("var list = newArrayList(\"one\", \"two\", \"three\", \"four\"); }");
		template.newLine();
		template.append("<html>");
		template.newLine();
		template.append("<i>$(nullString)</i>");
		template.newLine();
		template.append("<b>$?(nullString)</b>");
		template.newLine();
		template.append("<title>$(name)</title>");
		template.newLine();
		template.append("#for(String element : list)");
		template.newLine();
		template.append("#if(element.equals(\"one\"))");
		template.newLine();
		template.append("<h1>$(element)</h1>");
		template.newLine();
		template.append("#elseif(element.equals(\"two\"))");
		template.newLine();
		template.append("<h2>$(element)</h2>");
		template.newLine();
		template.append("#else");
		template.newLine();
		template.append("<p>$(element)</p>");
		template.newLine();
		template.append("#end");
		template.newLine();
		template.append("#end");
		template.newLine();
		template.append("</html>");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		StringConcatenation expected = new StringConcatenation();
		expected.append("<html>");
		expected.newLine();
		expected.append("<i>null</i>");
		expected.newLine();
		expected.append("<b></b>");
		expected.newLine();
		expected.append("<title>Foo</title>");
		expected.newLine();
		expected.append("<h1>one</h1>");
		expected.newLine();
		expected.append("<h2>two</h2>");
		expected.newLine();
		expected.append("<p>three</p>");
		expected.newLine();
		expected.append("<p>four</p>");
		expected.newLine();
		expected.append("</html>");

		assertSameOuput(expected, out);
	}

	@Test
	public void testParseAndCompile_Import() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append(" ");
		template.append("import java.io.File;");
		template.newLine();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var file = new File(\'testimport\');");
		template.newLine();
		template.append("var name = file.getName();");
		template.newLine();
		template.append("}");
		template.newLine();
		template.append("$(name)");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("testimport", out.trim());
	}

	@Test
	public void testParseAndCompile_Field() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append(" ");
		template.append("String field = \"testfield\";");
		template.newLine();
		template.append("#renderer main()");
		template.newLine();
		template.append("$(field)");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("testfield", out.trim());
	}

	@Test
	public void testParseAndCompile_Method() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append(" ");
		template.append("String getString() { return \"testmethod\"; }");
		template.newLine();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var v = getString(); }");
		template.newLine();
		template.append("$(v)");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("testmethod", out.trim());
	}

	@Test
	public void testParseAndCompile_FieldAnnotation() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append(" ");
		template.append("@javax.inject.Inject");
		template.newLine();
		template.append("String field = \"testfield\";");
		template.newLine();
		template.append("#renderer main()");
		template.newLine();
		template.append("$(field)");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("testfield", out.trim());
	}

	@Test
	public void testParseAndCompile_MethodAnnotation() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append(" ");
		template.append("@javax.inject.Inject");
		template.newLine();
		template.append("String getString() { return \"testmethod\"; }");
		template.newLine();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ var v = getString(); }");
		template.newLine();
		template.append("$(v)");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("testmethod", out.trim());
	}

	@Test
	public void testParseAndCompile_Implements() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append(" ");
		template.append("template implements java.io.Serializable;");
		template.newLine();
		template.append("#renderer main()");
		template.newLine();
		template.append("testimplements");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("testimplements", out.trim());
	}

	@Test
	public void testParseAndCompile_ImportImplements() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append(" ");
		template.append("import java.io.Serializable;");
		template.newLine();
		template.append("template implements Serializable;");
		template.newLine();
		template.append("#renderer main()");
		template.newLine();
		template.append("testimportimplements");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		Assert.assertEquals("testimportimplements", out.trim());
	}

	@Test
	public void testParseAndCompile_Extends() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append(" ");
		template.append("template extends java.util.ArrayList<String>;");
		template.newLine();
		template.append("#renderer main()");
		template.newLine();
		template.append("#{ add(\"test\");");
		template.newLine();
		template.append("add(\"extends\"); }");
		template.newLine();
		template.append("#for(text : this)");
		template.newLine();
		template.append("<p>$(text)</p>");
		template.newLine();
		template.append("#end");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		assertSameOuput("<p>test</p><p>extends</p>", out);
	}

	@Test
	public void testParseAndCompile_Render() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append(" ");
		template.append("#renderer main()");
		template.newLine();
		template.append("#render strong(\"testrender\")");
		template.newLine();
		template.append("#end");
		template.newLine();
		template.append("#renderer strong(String item)");
		template.newLine();
		template.append("<strong>$(item)</strong>");
		template.newLine();
		template.append("#end");
		template.newLine();

		String out = callTemplate(template, "renderMain");

		assertSameOuput("<strong>testrender</strong>", out);
	}

	@Test
	public void testParseAndCompile_Abstract() throws Exception {
		StringConcatenation template = new StringConcatenation();
		template.append(" ");
		template.append("abstract template;");
		template.newLine();
		template.append("#renderer main()");
		template.newLine();
		template.append("#render strong(\"testrender\")");
		template.newLine();
		template.append("#end");
		template.newLine();
		template.append("#renderer abstract strong(String item)");
		template.newLine();

		Class<?> cl = compileTemplate(template);

		final Method[] methods = cl.getDeclaredMethods();
		boolean renderStringFound = false;
		for (final Method method : methods) {
			if (method.getName().equals("renderStrong")) {
				renderStringFound = true;
				Assert.assertEquals(Modifier.ABSTRACT + Modifier.PUBLIC, method.getModifiers());
			}
		}
		Assert.assertTrue(renderStringFound);
	}
}
