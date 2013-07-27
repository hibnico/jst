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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmAnnotationReference;
import org.eclipse.xtext.common.types.JvmConstructor;
import org.eclipse.xtext.common.types.JvmFeature;
import org.eclipse.xtext.common.types.JvmField;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmParameterizedTypeReference;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable;
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer;
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver;
import org.hibnet.jst.JstOptions;
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
        final JvmConstructor superConstructor = getSuperConstructor(jstFile);

        String simpleName = getClassName(jstFile);

        String qualifiedName;
        String packageName = jstFile.getPackageName();
        if (packageName != null) {
            qualifiedName = simpleName;
        } else {
            qualifiedName = packageName + "." + simpleName;
        }
        final JvmGenericType templateClass = jvmTypesBuilder.toClass(jstFile, qualifiedName);
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
                                        for (JvmFormalParameter param : method.getParameters()) {
                                            p.getParameters().add(
                                                    jvmTypesBuilder.toParameter(method, param.getIdentifier(),
                                                            param.getParameterType()));
                                        }
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
                        for (RendererParameter parameter : renderer.getParameters()) {
                            JvmField f = jvmTypesBuilder.toField(renderer, parameter.getParameterName(),
                                    parameter.getParameterType(), new Procedure1<JvmField>() {
                                        @Override
                                        public void apply(JvmField f) {
                                            f.setVisibility(JvmVisibility.PUBLIC);
                                        }
                                    });
                            it.getMembers().add(f);
                        }
                    }
                    JvmOperation method = jvmTypesBuilder.toMethod(jstFile, name, voidTypeRef,
                            new Procedure1<JvmOperation>() {
                                @Override
                                public void apply(JvmOperation op) {
                                    if (renderer.getSimpleName() != null) {
                                        op.setVisibility(JvmVisibility.PROTECTED);
                                    }
                                    JvmTypeReference writerTypeRef = jvmTypesBuilder.newTypeRef(renderer, Writer.class);
                                    JvmFormalParameter param = jvmTypesBuilder.toParameter(renderer, "out",
                                            writerTypeRef);
                                    op.getParameters().add(param);
                                    if (renderer.getSimpleName() != null) {
                                        for (RendererParameter parameter : renderer.getParameters()) {
                                            JvmTypeReference paramType = parameter.getParameterType();
                                            param = jvmTypesBuilder.toParameter(renderer, parameter.getParameterName(),
                                                    paramType);
                                            op.getParameters().add(param);
                                        }
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

                JvmConstructor constructor = jvmTypesBuilder.toConstructor(jstFile, new Procedure1<JvmConstructor>() {
                    @Override
                    public void apply(JvmConstructor p) {
                        p.getParameters().addAll(getSuperParams(jstFile, superConstructor));
                        p.getParameters().addAll(getParams(jstFile));
                        jvmTypesBuilder.setBody(p, new Procedure1<ITreeAppendable>() {
                            @Override
                            public void apply(ITreeAppendable p) {
                                p.append("super(");
                                Iterator<JvmFormalParameter> itParams = getSuperParams(jstFile, superConstructor)
                                        .iterator();
                                while (itParams.hasNext()) {
                                    p.append(itParams.next().getName());
                                    if (itParams.hasNext()) {
                                        p.append(", ");
                                    }
                                }
                                p.append(");");
                                p.newLine();
                                for (JvmFormalParameter parameter : getParams(jstFile)) {
                                    p.append("this." + parameter.getName() + " = " + parameter.getName() + ";");
                                    p.newLine();
                                }
                            }
                        });
                    }
                });
                it.getMembers().add(constructor);
            }
        });

        String factory = JstOptions.getStringOption(jstFile, "factory");
        if (factory != null && factory.equals("spring")) {
            JvmGenericType templateFactoryClass = jvmTypesBuilder.toClass(jstFile, qualifiedName + "Factory");
            templateFactoryClass.setPackageName(packageName);

            acceptor.<JvmGenericType> accept(templateFactoryClass).initializeLater(new Procedure1<JvmGenericType>() {
                @Override
                public void apply(final JvmGenericType it) {
                    JvmTypeReference processorType = jvmTypesBuilder.newTypeRef(jstFile,
                            "org.springframework.context.annotation.CommonAnnotationBeanPostProcessor");

                    JvmField f = jvmTypesBuilder.toField(jstFile, "postProcessor", processorType,
                            new Procedure1<JvmField>() {
                                @Override
                                public void apply(JvmField f) {
                                    JvmAnnotationReference resourceAnnotation = jvmTypesBuilder.toAnnotation(jstFile,
                                            "javax.annotation.Resource");
                                    f.getAnnotations().add(resourceAnnotation);
                                    f.setVisibility(JvmVisibility.PRIVATE);
                                }
                            });
                    it.getMembers().add(f);

                    JvmTypeReference templateType = jvmTypesBuilder.newTypeRef(templateClass);

                    JvmOperation m = jvmTypesBuilder.toMethod(jstFile, "build", templateType,
                            new Procedure1<JvmOperation>() {
                                @Override
                                public void apply(JvmOperation p) {
                                    p.setVisibility(JvmVisibility.PUBLIC);
                                    p.getParameters().addAll(getSuperParams(jstFile, superConstructor));
                                    p.getParameters().addAll(getParams(jstFile));
                                    jvmTypesBuilder.setBody(p, new Procedure1<ITreeAppendable>() {
                                        @Override
                                        public void apply(ITreeAppendable p) {
                                            p.append(templateClass.getQualifiedName() + " t = new "
                                                    + templateClass.getQualifiedName() + "(");
                                            boolean first = true;
                                            for (JvmFormalParameter param : getSuperParams(jstFile, superConstructor)) {
                                                if (!first) {
                                                    p.append(", ");
                                                }
                                                p.append(param.getName());
                                                first = false;
                                            }
                                            for (JvmFormalParameter param : getParams(jstFile)) {
                                                if (!first) {
                                                    p.append(", ");
                                                }
                                                p.append(param.getName());
                                                first = false;
                                            }
                                            p.append(");");
                                            p.newLine();
                                            p.append("postProcessor.postProcessAfterInstantiation(t, t.getClass().getName());");
                                            p.newLine();
                                            p.append("return t;");
                                            p.newLine();
                                        }
                                    });
                                }
                            });
                    it.getMembers().add(m);
                }
            });
        }
    }

    private JvmConstructor getSuperConstructor(final JstFile jstFile) {
        JvmConstructor superConstructor = null;
        for (JvmParameterizedTypeReference superType : jstFile.getSuperTypes()) {
            if (superType.getType() instanceof JvmGenericType && !((JvmGenericType) superType.getType()).isInterface()) {
                Iterator<JvmConstructor> itConst = ((JvmGenericType) superType.getType()).getDeclaredConstructors()
                        .iterator();
                if (itConst.hasNext()) {
                    superConstructor = itConst.next();
                }
                break;
            }
        }
        return superConstructor;
    }

    private List<JvmFormalParameter> getSuperParams(JstFile jstFile, JvmConstructor superConstructor) {
        List<JvmFormalParameter> superParams = new ArrayList<JvmFormalParameter>();
        if (superConstructor != null) {
            for (JvmFormalParameter parameter : superConstructor.getParameters()) {
                JvmFormalParameter param = jvmTypesBuilder.toParameter(jstFile, parameter.getName(),
                        parameter.getParameterType());
                superParams.add(param);
            }
        }
        return superParams;
    }

    private List<JvmFormalParameter> getParams(JstFile jstFile) {
        List<JvmFormalParameter> params = new ArrayList<JvmFormalParameter>();
        for (final Renderer renderer : jstFile.getRenderers()) {
            if (renderer.getSimpleName() == null) {
                for (RendererParameter parameter : renderer.getParameters()) {
                    JvmFormalParameter param = jvmTypesBuilder.toParameter(renderer, parameter.getParameterName(),
                            parameter.getParameterType());
                    params.add(param);
                }
                break;
            }
        }
        return params;
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
