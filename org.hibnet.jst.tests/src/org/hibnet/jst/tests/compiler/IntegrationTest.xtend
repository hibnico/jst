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
import java.io.PrintStream
import java.io.ByteArrayOutputStream

@RunWith(typeof(XtextRunner))
@InjectWith(typeof(JstInjectorProvider))
class IntegrationTest {
	
	@Inject extension CompilationTestHelper
	@Inject extension ReflectExtensions
	
	@Test def void testParseAndCompile_Simple() {
		'''
			#template render()
			Hello World
			#end
		'''.compile [
            val out = new ByteArrayOutputStream();
		    val p = new PrintStream(out)
			compiledClass.newInstance.invoke('render', p)
			assertEquals('Hello World', new String(out.toByteArray).trim)
		]
	}
	
    @Test def void testParseAndCompile_If() {
        ''' #template render()
            #if(true)
              <h1>Hello</h1>
            #end
            #end
        '''.compile [
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            compiledClass.newInstance.invoke('render', p)
            assertEquals('<h1>Hello</h1>', new String(out.toByteArray).trim)
        ]
    }
    
    @Test def void testParseAndCompile_IfElse() {
        ''' #template render()
                #if(true)
                    <p>ok</p>
                #else
                    <p>nok</p>
                #end
            #end
        '''.compile [
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            compiledClass.newInstance.invoke('render', p)
            assertEquals('<p>ok</p>', new String(out.toByteArray).trim)
        ]
    }
    
    @Test def void testParseAndCompile_IfElseIf() {
        ''' #template render()
                #if(false)
                  <h1>nok</h1>
                #elseif(true)
                  <h2>ok</h2>
                #else
                  <p>nok</p>
                #end
            #end
        '''.compile [
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            compiledClass.newInstance.invoke('render', p)
            assertEquals('<h2>ok</h2>', new String(out.toByteArray).trim)
        ]
    }
    
    @Test def void testParseAndCompile_Inline() {
        ''' #template render()
              <i>$("Hello")</i>
            #end
        '''.compile [
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            compiledClass.newInstance.invoke('render', p)
            assertEquals('<i>Hello</i>', new String(out.toByteArray).trim)
        ]
    }

    @Test def void testParseAndCompile_Script() {
        ''' #template render()
              #( var name = "Foo";)
              <title>$(name)</title>
            #end
        '''.compile [
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            compiledClass.newInstance.invoke('render', p)
            assertEquals('<title>Foo</title>', new String(out.toByteArray).trim)
        ]
    }

    @Test def void testParseAndCompile_InlineElvis() {
        ''' #template render()
              #( var name = "Foo";)
              #( var name2 = null;)
              <h1>$(name)</h1>
              <h2>$!(name)</h2>
              <h3>$(name2)</h3>
              <h4>$!(name2)</h4>
            #end
        '''.compile [
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            compiledClass.newInstance.invoke('render', p)
            assertEquals('''<h1>Foo</h1>
              <h2>Foo</h2>
              <h3>null</h3>
              <h4></h4>'''.toString, new String(out.toByteArray).trim)
        ]
    }

    @Test def void testParseAndCompile_MultiLineComment() {
        ''' #template render()
              #* some comment *#
              Hello World
              #* some
              multiline 
              comment *#
            #end
        '''.compile [
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            compiledClass.newInstance.invoke('render', p)
            assertEquals('''Hello World'''.toString, new String(out.toByteArray).trim)
        ]
    }

    @Test def void testParseAndCompile_SingleLineComment() {
        ''' #template render()
              #- some comment
              Hello World
              #- some other comment
            #end
        '''.compile [
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            compiledClass.newInstance.invoke('render', p)
            assertEquals('''Hello World'''.toString, new String(out.toByteArray).trim)
        ]
    }

    @Test def void testParseAndCompile_For() {
        ''' #template render()
             #( var list = newArrayList("one", "two", "three", "four"); )
            <html>
              #for(String element : list)
                <p>$(element)</p>
              #end
            </html>
            #end
        '''.compile [
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            compiledClass.newInstance.invoke('render', p)
            assertEquals('''
                <html>
                      <p>one</p>
                      <p>two</p>
                      <p>three</p>
                      <p>four</p>
                </html>'''.toString.replaceAll("( |\n)", ""), new String(out.toByteArray).replaceAll("( |\n)", ""))
        ]
    }
 
    @Test def void testParseAndCompile_Complex() {
        ''' #template render()
             #( var nullString = null;
                var name = "Foo";
                var list = newArrayList("one", "two", "three", "four"); )
            <html>
              <i>$(nullString)</i>
              <b>$!(nullString)</b>
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
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            compiledClass.newInstance.invoke('render', p)
            assertEquals('''
                <html>
                  <i>null</i>
                  <b></b>
                  <title>Foo</title>
                      <h1>one</h1>
                      <h2>two</h2>
                      <p>three</p>
                      <p>four</p>
                </html>'''.toString.replaceAll("( |\n)", ""), new String(out.toByteArray).replaceAll("( |\n)", ""))
        ]
    }
    
    @Test def void testParseAndCompile_Import() {
        ''' import java.io.File;
            #template render()
                #( var file = new File('testimport');
                   var name = file.getName();
                 )
                $(name)
            #end
        '''.compile [
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            compiledClass.newInstance.invoke('render', p)
            assertEquals('testimport', new String(out.toByteArray).trim)
        ]
    }

    @Test def void testParseAndCompile_Field() {
        ''' String field = "testfield";
            #template render()
                $(field)
            #end
        '''.compile [
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            compiledClass.newInstance.invoke('render', p)
            assertEquals('testfield', new String(out.toByteArray).trim)
        ]
    }

    @Test def void testParseAndCompile_Method() {
        ''' String getString() { return "testmethod"; }
            #template render()
                #( var v = getString(); )
                $(v)
            #end
        '''.compile [
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            compiledClass.newInstance.invoke('render', p)
            assertEquals('testmethod', new String(out.toByteArray).trim)
        ]
    }

    @Test def void testParseAndCompile_Implements() {
        ''' template implements java.io.Serializable;
            #template render()
               testimplements
            #end
        '''.compile [
            val template = compiledClass.newInstance
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            template.invoke('render', p)
            assertTrue(template instanceof java.io.Serializable)
            assertEquals('testimplements', new String(out.toByteArray).trim)
        ]
    }

    @Test def void testParseAndCompile_ImportImplements() {
        ''' import java.io.Serializable;
            template implements Serializable;
            #template render()
               testimportimplements
            #end
        '''.compile [
            val template = compiledClass.newInstance
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            template.invoke('render', p)
            assertTrue(template instanceof java.io.Serializable)
            assertEquals('testimportimplements', new String(out.toByteArray).trim)
        ]
    }

    @Test def void testParseAndCompile_Extends() {
        ''' template extends java.util.ArrayList<String>;
            #template render()
               #( add("test");
                  add("extends"); )
               #for(text : this)
                  <p>$(text)</p>
               #end
            #end
        '''.compile [
            val template = compiledClass.newInstance
            val out = new ByteArrayOutputStream();
            val p = new PrintStream(out)
            template.invoke('render', p)
            assertTrue(template instanceof java.io.Serializable)
            assertEquals('<p>test</p><p>extends</p>', new String(out.toByteArray).replaceAll("( |\n)", ""))
        ]
    }
}