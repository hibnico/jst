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

@RunWith(typeof(XtextRunner))
@InjectWith(typeof(JstInjectorProvider))
class IntegrationTest {
	
	@Inject extension CompilationTestHelper
	@Inject extension ReflectExtensions
	
	@Test def void testParseAndCompile_01() {
		'''
			<!--<< >>-->
			Hello World
		'''.repl.compile [
			val result = compiledClass.newInstance.invoke('generate', null)
			assertEquals('Hello World',result)
		]
	}
	
	@Test def void testParseAndCompile_02() {
		'''
			<!--<<
				param name = 'Foo'
				param nullString
				param list = newArrayList('one', 'two', 'three', 'four')
			>>-->
			<html><<nullString>>
			  <title><<name>></title>
			  <<FOR element : list>>
			    <<IF element == 'one'>>
			      <h1><<element>></h1>
			    <<ELSEIF element == 'two'>>
			      <h2><<element>></h2>
			    <<ELSE>>
			      <p><<element>></p>
			    <<ENDIF>>
			  <<ENDFOR>>
			</html>
		'''.repl.compile [
			val result = compiledClass.newInstance.invoke('generate', null)
			assertEquals('''
				<html>
				  <title>Foo</title>
				      <h1>one</h1>
				      <h2>two</h2>
				      <p>three</p>
				      <p>four</p>
				</html>'''.toString,result.toString)
		]
	}
	
	def repl(CharSequence s) {
		s.toString.replace('<<','«').replace('>>','»')
	}
}