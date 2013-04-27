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
package org.hibnet.jst.tests.compiler;

import java.io.StringWriter;

import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.xbase.compiler.CompilationTestHelper;
import org.eclipse.xtext.xbase.compiler.CompilationTestHelper.Result;
import org.eclipse.xtext.xbase.lib.util.ReflectExtensions;
import org.hibnet.jst.JstInjectorProvider;
import org.junit.Assert;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

@RunWith(value = XtextRunner.class)
@InjectWith(value = JstInjectorProvider.class)
public abstract class AbstractTest {

    @Inject
    private CompilationTestHelper compilationTestHelper;

    @Inject
    private ReflectExtensions reflectExtensions;

    protected Class<?> compileTemplate(CharSequence template) throws Exception {
        final Result[] result = new Result[1];
        compilationTestHelper.compile(template, new IAcceptor<Result>() {
            @Override
            public void accept(Result t) {
                result[0] = t;
            }
        });
        return result[0].getCompiledClass();
    }

    protected String callTemplate(CharSequence template, String renderFunction, Object... params) throws Exception {
        StringWriter out = new StringWriter();
        Class<?> cl = compileTemplate(template);
        if (params != null && params.length != 0) {
            Object[] params2 = new Object[params.length + 1];
            params2[0] = out;
            System.arraycopy(params, 0, params2, 1, params.length);
            reflectExtensions.invoke(cl.newInstance(), renderFunction, params2);
        } else {
            reflectExtensions.invoke(cl.newInstance(), renderFunction, out);
        }
        return out.toString();
    }

    protected void assertSameOuput(CharSequence expected, CharSequence output) {
        Assert.assertEquals(expected.toString().replaceAll("( |\n)", ""), output.toString().replaceAll("( |\n)", ""));
    }

}
