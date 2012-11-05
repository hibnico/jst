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
package org.hibnet.jst.tests.compiler

import com.google.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.xbase.compiler.CompilationTestHelper
import org.eclipse.xtext.xbase.lib.util.ReflectExtensions
import org.hibnet.jst.JstInjectorProvider
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.*
import java.lang.reflect.Modifier
import java.io.StringWriter

@RunWith(typeof(XtextRunner))
@InjectWith(typeof(JstInjectorProvider))
class IntegrationTest {

    @Inject extension CompilationTestHelper
    @Inject extension ReflectExtensions

    @Test def void testParseAndCompile_Simple() {
        '''
            #renderer main()
            Hello World
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('Hello World', out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_Dollar() {
        '''
            #renderer main()
            $$ ##
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('$ #', out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_If() {
        ''' #renderer main()
            #if(true)
              <h1>Hello</h1>
            #end
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('<h1>Hello</h1>', out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_IfElse() {
        ''' #renderer main()
                #if(true)
                    <p>ok</p>
                #else
                    <p>nok</p>
                #end
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('<p>ok</p>', out.toString.trim)
        ]
    }
    
    @Test def void testParseAndCompile_IfElseIf() {
        ''' #renderer main()
                #if(false)
                  <h1>nok</h1>
                #elseif(true)
                  <h2>ok</h2>
                #else
                  <p>nok</p>
                #end
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('<h2>ok</h2>', out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_Inline() {
        ''' #renderer main()
              <i>$("Hello")</i>
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('<i>Hello</i>', out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_Param() {
        '''
            #renderer main(String name)
            Hello $(name)
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out, "John")
            assertEquals('Hello John', out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_Script() {
        ''' #renderer main()
              #{ var name = "Foo";}
              <title>$(name)</title>
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('<title>Foo</title>', out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_InlineElvis() {
        ''' #renderer main()
              #{ var name = "Foo";}
              #{ var name2 = null;}
              <h1>$(name)</h1>
              <h2>$?(name)</h2>
              <h3>$(name2)</h3>
              <h4>$?(name2)</h4>
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('''<h1>Foo</h1>
              <h2>Foo</h2>
              <h3>null</h3>
              <h4></h4>'''.toString, out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_InlineUnescape() {
        ''' #renderer main()
              #{ var name = "Foo";}
              #{ var name2 = null;}
              <h1>$\\(name)</h1>
              <h2>$?\\(name)</h2>
              <h3>$\\(name2)</h3>
              <h4>$?\\(name2)</h4>
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('''<h1>Foo</h1>
              <h2>Foo</h2>
              <h3>null</h3>
              <h4></h4>'''.toString, out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_InlineEscape() {
        ''' #renderer main()
              #{ var html = "<&éa>a";}
              #{ var xml = "<&éa>a";}
              #{ var js = "call('')";}
              <html>$\\(html)</html>
              <html>$\html(html)</html>
              <xml>$\\(xml)</xml>
              <xml>$\xml(xml)</xml>
              <js>$\\(js)</js>
              <js>$\js(js)</js>
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('''
                <html><&éa>a</html>
                <html>&lt;&amp;&eacute;a&gt;a</html>
                <xml><&éa>a</xml>
                <xml>&lt;&amp;&#233;a&gt;a</xml>
                <js>call('')</js>
                <js>call(\'\')</js>
              '''.toString.replaceAll("( |\n)", ""), out.toString.replaceAll("( |\n)", ""))
        ]
    }

    @Test def void testParseAndCompile_MultiLineComment() {
        ''' #renderer main()
              #* some comment *#
              Hello World
              #* some
              multiline 
              comment *#
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('''Hello World'''.toString, out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_SingleLineComment() {
        ''' #renderer main()
              #- some comment
              Hello World
              #- some other comment
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('''Hello World'''.toString, out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_For() {
        ''' #renderer main()
             #{ var list = newArrayList("one", "two", "three", "four"); }
            <html>
              #for(String element : list)
                <p>$(element)</p>
              #end
            </html>
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('''
                <html>
                      <p>one</p>
                      <p>two</p>
                      <p>three</p>
                      <p>four</p>
                </html>'''.toString.replaceAll("( |\n)", ""), out.toString.replaceAll("( |\n)", ""))
        ]
    }
 
    @Test def void testParseAndCompile_Complex() {
        ''' #renderer main()
             #{ var nullString = null;
                var name = "Foo";
                var list = newArrayList("one", "two", "three", "four"); }
            <html>
              <i>$(nullString)</i>
              <b>$?(nullString)</b>
              <title>$(name)</title>
              #for(String element : list)
                #if(element.equals("one"))
                  <h1>$(element)</h1>
                #elseif(element.equals("two"))
                  <h2>$(element)</h2>
                #else
                  <p>$(element)</p>
                #end
              #end
            </html>
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('''
                <html>
                  <i>null</i>
                  <b></b>
                  <title>Foo</title>
                      <h1>one</h1>
                      <h2>two</h2>
                      <p>three</p>
                      <p>four</p>
                </html>'''.toString.replaceAll("( |\n)", ""), out.toString.replaceAll("( |\n)", ""))
        ]
    }

    @Test def void testParseAndCompile_Import() {
        ''' import java.io.File;
            #renderer main()
                #{ var file = new File('testimport');
                   var name = file.getName();
                 }
                $(name)
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('testimport', out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_Field() {
        ''' String field = "testfield";
            #renderer main()
                $(field)
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('testfield', out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_Method() {
        ''' String getString() { return "testmethod"; }
            #renderer main()
                #{ var v = getString(); }
                $(v)
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('testmethod', out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_Implements() {
        ''' template implements java.io.Serializable;
            #renderer main()
               testimplements
            #end
        '''.compile [
            val template = compiledClass.newInstance
            val out = new StringWriter()
            template.invoke('renderMain', out)
            assertTrue(template instanceof java.io.Serializable)
            assertEquals('testimplements', out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_ImportImplements() {
        ''' import java.io.Serializable;
            template implements Serializable;
            #renderer main()
               testimportimplements
            #end
        '''.compile [
            val template = compiledClass.newInstance
            val out = new StringWriter()
            template.invoke('renderMain', out)
            assertTrue(template instanceof java.io.Serializable)
            assertEquals('testimportimplements', out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_Extends() {
        ''' template extends java.util.ArrayList<String>;
            #renderer main()
               #{ add("test");
                  add("extends"); }
               #for(text : this)
                  <p>$(text)</p>
               #end
            #end
        '''.compile [
            val template = compiledClass.newInstance
            val out = new StringWriter()
            template.invoke('renderMain', out)
            assertTrue(template instanceof java.io.Serializable)
            assertEquals('<p>test</p><p>extends</p>', out.toString.replaceAll("( |\n)", ""))
        ]
    }

    @Test def void testParseAndCompile_Render() {
        ''' #renderer main()
               #render strong("testrender")
            #end
            #renderer strong(String item)
               <strong>$(item)</strong>
            #end
        '''.compile [
            val out = new StringWriter()
            compiledClass.newInstance.invoke('renderMain', out)
            assertEquals('<strong>testrender</strong>', out.toString.trim)
        ]
    }

    @Test def void testParseAndCompile_Abstract() {
        ''' abstract template;
            #renderer main()
               #render strong("testrender")
            #end
            #renderer abstract strong(String item)
        '''.compile [
            val methods = compiledClass.declaredMethods;
            var renderStringFound = false;
            for (method : methods) {
                if (method.name.equals("renderStrong")) {
                    renderStringFound = true;
                    assertEquals(Modifier::ABSTRACT + Modifier::PUBLIC, method.modifiers)                    
                }
            }
            assertTrue(renderStringFound)
        ]
    }
}