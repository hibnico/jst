/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
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
			#function render()
			Hello World
			#end
		'''.compile [
            val out = new ByteArrayOutputStream();
		    val p = new PrintStream(out)
			compiledClass.newInstance.invoke('render', p)
			assertEquals('Hello World', new String(out.toByteArray))
		]
	}
	
    @Test def void testParseAndCompile_If() {
        ''' #function render()
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
        ''' #function render()
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
        ''' #function render()
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
        ''' #function render()
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
        ''' #function render()
              #( var name = "Foo")
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
        ''' #function render()
              #( var name = "Foo")
              #( var name2 = null)
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

    @Test def void testParseAndCompile_For() {
        ''' #function render()
             #( var list = newArrayList("one", "two", "three", "four") )
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
                </html>'''.toString, new String(out.toByteArray))
        ]
    }
 
    @Test def void testParseAndCompile_Complex() {
        ''' #function render()
             #( var nullString = null
                var name = "Foo"
                var list = newArrayList("one", "two", "three", "four") )
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
                </html>'''.toString, new String(out.toByteArray))
        ]
    }
    
}