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
package org.hibnet.jst.jvmmodel;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.common.types.JvmFormalParameter;
import org.eclipse.xtext.common.types.JvmMember;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmVisibility;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.xbase.XExpression;
import org.eclipse.xtext.xbase.XFeatureCall;
import org.eclipse.xtext.xbase.XMemberFeatureCall;
import org.eclipse.xtext.xbase.XStringLiteral;
import org.eclipse.xtext.xbase.XbaseFactory;
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable;
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer;
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor;
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.hibnet.jst.jst.AbstractRenderer;
import org.hibnet.jst.jst.Field;
import org.hibnet.jst.jst.JstFile;
import org.hibnet.jst.jst.JstOption;
import org.hibnet.jst.jst.Method;
import org.hibnet.jst.jst.Renderer;
import org.hibnet.jst.jst.RendererParameter;
import org.hibnet.jst.jst.RichStringRender;
import org.hibnet.jst.jst.RichStringTemplateRender;

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
	private TypesFactory typesFactory;

	protected void _infer(final JstFile element, final IJvmDeclaredTypeAcceptor acceptor,
			final boolean isPreIndexingPhase) {
		String simpleName = StringExtensions.toFirstUpper(element.eResource().getURI().trimFileExtension()
				.lastSegment())
				+ "JstTemplate";
		element.setSimpleName(simpleName);

		acceptor.<JstFile> accept(element).initializeLater(new Procedure1<JstFile>() {
			@Override
			public void apply(final JstFile it) {
				element.setVisibility(JvmVisibility.PUBLIC);
				EList<JvmMember> members = element.getMembers();
				for (JvmMember member : members) {
					if (member instanceof Field) {
						final Field field = (Field) member;
						if (field.getInitialValue() != null) {
							jvmTypesBuilder.setInitializer(field, field.getInitialValue());
						}
					} else {
						if (member instanceof Method) {
							Method method = (Method) member;
							jvmTypesBuilder.setBody(method, method.getExpression());
						}
					}
				}
				for (final Renderer renderer : element.getRenderers()) {
					JvmTypeReference voidTypeRef = jvmTypesBuilder.newTypeRef(element, Void.TYPE);
					JvmOperation method = jvmTypesBuilder.toMethod(element, renderer.getSimpleName(), voidTypeRef,
							new Procedure1<JvmOperation>() {
								@Override
								public void apply(JvmOperation op) {
									op.setVisibility(JvmVisibility.PUBLIC);
									JvmTypeReference writerTypeRef = jvmTypesBuilder.newTypeRef(renderer, Writer.class);
									JvmFormalParameter param = jvmTypesBuilder.toParameter(element, "out",
											writerTypeRef);
									op.getParameters().add(param);
									for (RendererParameter parameter : renderer.getParameters()) {
										param = jvmTypesBuilder.toParameter(element, parameter.getName(),
												parameter.getParameterType());
										op.getParameters().add(param);
									}
									op.getExceptions().add(jvmTypesBuilder.newTypeRef(element, Exception.class));
									if (renderer instanceof AbstractRenderer) {
										op.setAbstract(true);
									} else {
										jvmTypesBuilder.setBody(op, renderer.getExpression());
									}
								}
							});
					members.add(method);
				}

				members.add(buildWriteMethod(element, "unescape", null));
				members.add(buildWriteMethod(element, "escape_xml",
						"org.apache.commons.lang.StringEscapeUtils.escapeXml("));
				members.add(buildWriteMethod(element, "escape_html",
						"org.apache.commons.lang.StringEscapeUtils.escapeHtml("));
				members.add(buildWriteMethod(element, "escape_js",
						"org.apache.commons.lang.StringEscapeUtils.escapeJavaScript("));
				members.add(buildWriteMethod(element, "escape_java",
						"org.apache.commons.lang.StringEscapeUtils.escapeJava("));
				members.add(buildWriteMethod(element, "escape_csv",
						"org.apache.commons.lang.StringEscapeUtils.escapeCsv("));
				members.add(buildWriteMethod(element, "escape_sql",
						"org.apache.commons.lang.StringEscapeUtils.escapeSql("));

				members.add(jvmTypesBuilder.toMethod(element, "_jst_write_escape",
						jvmTypesBuilder.newTypeRef(element, Void.TYPE), new Procedure1<JvmOperation>() {
							@Override
							public void apply(final JvmOperation op) {
								op.setVisibility(JvmVisibility.PRIVATE);
								op.getParameters().add(
										jvmTypesBuilder.toParameter(element, "out",
												jvmTypesBuilder.newTypeRef(element, Writer.class)));
								op.getParameters().add(
										jvmTypesBuilder.toParameter(element, "object",
												jvmTypesBuilder.newTypeRef(element, Object.class)));
								op.getParameters().add(
										jvmTypesBuilder.toParameter(element, "elvis",
												jvmTypesBuilder.newTypeRef(element, Boolean.TYPE)));
								op.getExceptions().add(jvmTypesBuilder.newTypeRef(element, IOException.class));

								jvmTypesBuilder.setBody(op, new Procedure1<ITreeAppendable>() {
									@Override
									public void apply(ITreeAppendable it) {
										String escape = getEscapeOption(element);
										if (escape == null) {
											it.append("_jst_write_unescape");
										} else {
											it.append("_jst_write_escape_");
											it.append(escape);
										}
										it.append("(out, object, elvis);");
									}
								});
							}
						}));
			}
		});
	}

	private JvmOperation buildWriteMethod(final JstFile element, String escapeName, final String escapeFunction) {
		return jvmTypesBuilder.toMethod(element, "_jst_write_" + escapeName,
				jvmTypesBuilder.newTypeRef(element, Void.TYPE), new Procedure1<JvmOperation>() {
					@Override
					public void apply(JvmOperation op) {
						op.setVisibility(JvmVisibility.PRIVATE);
						EList<JvmFormalParameter> parameters = op.getParameters();
						parameters.add(jvmTypesBuilder.toParameter(element, "out",
								jvmTypesBuilder.newTypeRef(element, Writer.class)));
						parameters.add(jvmTypesBuilder.toParameter(element, "object",
								jvmTypesBuilder.newTypeRef(element, Object.class)));
						parameters.add(jvmTypesBuilder.toParameter(element, "elvis",
								jvmTypesBuilder.newTypeRef(element, Boolean.TYPE)));
						op.getExceptions().add(jvmTypesBuilder.newTypeRef(element, IOException.class));

						jvmTypesBuilder.setBody(op, new Procedure1<ITreeAppendable>() {
							@Override
							public void apply(ITreeAppendable it) {
								it.append("if (object != null) {").increaseIndentation().newLine();
								it.append("out.append(");
								if (escapeFunction != null) {
									it.append(escapeFunction);
								}
								it.append("object.toString()");
								if (escapeFunction != null) {
									it.append(")");
								}
								it.append(");");
								it.decreaseIndentation().newLine().append("} else if (!elvis) {").increaseIndentation()
										.newLine();
								it.append("out.append(\"null\");");
								it.decreaseIndentation().newLine().append("}");
							}
						});
					}
				});
	}

	private String getEscapeOption(JstFile element) {
		for (JstOption option : element.getOptions()) {
			if (option.getKey().equals("escape") && option.getValue() instanceof XStringLiteral) {
				return ((XStringLiteral) option.getValue()).getValue();
			}
		}
		return null;
	}

	protected void _infer(RichStringRender render, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		XFeatureCall call = XbaseFactory.eINSTANCE.createXFeatureCall();
		call.setFeature(render.getFeature());
		call.getFeatureCallArguments().add(getOutParam(render));
		call.getFeatureCallArguments().addAll(render.getFeatureCallArguments());
	}

	protected void _infer(RichStringTemplateRender render, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		XMemberFeatureCall call = XbaseFactory.eINSTANCE.createXMemberFeatureCall();
		call.setMemberCallTarget(render.getMemberCallTarget());
		call.setFeature(render.getFeature());
		call.getMemberCallArguments().add(this.getOutParam(render));
		call.getMemberCallArguments().addAll(render.getMemberCallArguments());
	}

	private XExpression getOutParam(final EObject element) {
		XFeatureCall featureCall = XbaseFactory.eINSTANCE.createXFeatureCall();
		JvmFormalParameter out = typesFactory.createJvmFormalParameter();
		out.setName("out");
		out.setParameterType(jvmTypesBuilder.cloneWithProxies(jvmTypesBuilder.newTypeRef(element, Writer.class)));
		featureCall.setFeature(out);
		return featureCall;
	}

	@Override
	public void infer(EObject element, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
		if (element instanceof JstFile) {
			_infer((JstFile) element, acceptor, isPreIndexingPhase);
			return;
		} else if (element instanceof RichStringRender) {
			_infer((RichStringRender) element, acceptor, isPreIndexingPhase);
			return;
		} else if (element instanceof RichStringTemplateRender) {
			_infer((RichStringTemplateRender) element, acceptor, isPreIndexingPhase);
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
