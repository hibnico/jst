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
package org.hibnet.jst.jvmmodel;

import java.io.Writer;
import java.util.Arrays;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmFeature;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer;
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver;
import org.hibnet.jst.jst.AbstractRenderer;
import org.hibnet.jst.jst.Field;
import org.hibnet.jst.jst.JstFile;
import org.hibnet.jst.jst.Method;
import org.hibnet.jst.jst.Renderer;
import org.hibnet.jst.jst.RendererParameter;

import com.google.inject.Inject;

/**
 * <p>
 * Infers a JVM model from the source model.
 * </p>
 */
public class JstJvmModelInferrer extends AbstractModelInferrer {

    @Inject
    private JvmTypesBuilder jvmTypesBuilder;

    @Inject
    private IBatchTypeResolver typeResolver;

    @Inject
    private TypesFactory typesFactory;

    public static String getClassName(JstFile jstFile) {
        String fileName = jstFile.eResource().getURI().trimFileExtension().lastSegment();
        int i = fileName.lastIndexOf('.');
        if (i != -1) {
            fileName = fileName.substring(0, i);
        }
        String simpleName = StringExtensions.toFirstUpper(fileName) + "JstTemplate";
        return simpleName;
    }

    protected void _infer(final JstFile jstFile, final IJvmDeclaredTypeAcceptor acceptor,
            final boolean isPreIndexingPhase) {
        String simpleName = getClassName(jstFile);

        String qualifiedName;
        String packageName = jstFile.getPackageName();
        if (packageName != null) {
            qualifiedName = simpleName;
        } else {
            qualifiedName = packageName + "." + simpleName;
        }
        JvmGenericType templateClass = jvmTypesBuilder.toClass(jstFile, qualifiedName);
        templateClass.setPackageName(packageName);

        acceptor.<JvmGenericType> accept(templateClass).initializeLater(new Procedure1<JvmGenericType>() {
            @Override
            public void apply(final JvmGenericType it) {
                it.setVisibility(JvmVisibility.PUBLIC);
                it.setAbstract(jstFile.isAbstract());

                for (JvmParameterizedTypeReference superType : jstFile.getSuperTypes()) {
                    it.getSuperTypes().add(jvmTypesBuilder.cloneWithProxies(superType));
                }
                it.getSuperTypes().add(jvmTypesBuilder.newTypeRef(jstFile, "org.hibnet.jst.runtime.JstTemplate"));

                // copy fields and methods
                EList<JvmFeature> members = jstFile.getMembers();
                for (JvmFeature member : members) {
                    if (member instanceof Field) {
                        final Field field = (Field) member;
                        JvmField f = jvmTypesBuilder.toField(member, field.getIdentifier(), field.getType(),
                                new Procedure1<JvmField>() {
                                    @Override
                                    public void apply(JvmField f) {
                                        jvmTypesBuilder.translateAnnotationsTo(field.getFieldAnnotations(), f);
                                        f.setStatic(field.isStatic());
                                        f.setVisibility(field.getVisibility());
                                        if (field.getInitialValue() != null) {
                                            jvmTypesBuilder.setInitializer(f, field.getInitialValue());
                                        }
                                    }
                                });
                        it.getMembers().add(f);
                    } else if (member instanceof Method) {
                        final Method method = (Method) member;
                        JvmOperation m = jvmTypesBuilder.toMethod(member, method.getSimpleName(),
                                method.getReturnType(), new Procedure1<JvmOperation>() {
                                    @Override
                                    public void apply(JvmOperation p) {
                                        p.setStatic(method.isStatic());
                                        p.setAbstract(method.isAbstract());
                                        p.setVisibility(method.getVisibility());
                                        jvmTypesBuilder.setBody(p, method.getExpression());
                                        jvmTypesBuilder.translateAnnotationsTo(method.getMethodAnnotations(), p);
                                        p.getExceptions().addAll(method.getExceptions());
                                    }
                                });
                        it.getMembers().add(m);
                    }
                }

                // create renderer methods
                for (final Renderer renderer : jstFile.getRenderers()) {
                    JvmTypeReference voidTypeRef = jvmTypesBuilder.newTypeRef(jstFile, Void.TYPE);
                    String name = renderer.getSimpleName();
                    if (name == null) {
                        name = "render";
                    }
                    JvmOperation method = jvmTypesBuilder.toMethod(jstFile, name, voidTypeRef,
                            new Procedure1<JvmOperation>() {
                                @Override
                                public void apply(JvmOperation op) {
                                    if (renderer.getSimpleName() != null) {
                                        op.setVisibility(JvmVisibility.PRIVATE);
                                    }
                                    JvmTypeReference writerTypeRef = jvmTypesBuilder.newTypeRef(renderer, Writer.class);
                                    JvmFormalParameter param = jvmTypesBuilder.toParameter(renderer, "out",
                                            writerTypeRef);
                                    op.getParameters().add(param);
                                    for (RendererParameter parameter : renderer.getParameters()) {
                                        JvmTypeReference paramType = parameter.getParameterType();
                                        param = jvmTypesBuilder.toParameter(renderer, parameter.getParameterName(),
                                                paramType);
                                        op.getParameters().add(param);
                                    }
                                    op.getExceptions().add(jvmTypesBuilder.newTypeRef(renderer, Exception.class));
                                    if (renderer instanceof AbstractRenderer) {
                                        op.setAbstract(true);
                                    } else {
                                        jvmTypesBuilder.setBody(op, renderer.getExpression());
                                    }
                                }
                            });
                    it.getMembers().add(method);
                }
            }
        });
    }

    @Override
    public void infer(EObject element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
        if (element instanceof JstFile) {
            _infer((JstFile) element, acceptor, isPreIndexingPhase);
            return;
        } else if (element != null) {
            _infer(element, acceptor, isPreIndexingPhase);
            return;
        } else {
            throw new IllegalArgumentException("Unhandled parameter types: "
                    + Arrays.<Object> asList(element, acceptor, isPreIndexingPhase).toString());
        }
    }
}
