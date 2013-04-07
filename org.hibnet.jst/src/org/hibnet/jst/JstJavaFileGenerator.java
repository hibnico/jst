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
package org.hibnet.jst;

import static com.google.common.collect.Iterables.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.impl.ResourceSetBasedResourceDescriptions;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.compiler.GeneratorConfig;
import org.eclipse.xtext.xbase.compiler.IGeneratorConfigProvider;
import org.eclipse.xtext.xbase.compiler.JvmModelGenerator;
import org.hibnet.jst.jst.JstFile;
import org.hibnet.jst.jst.JstPackage;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

public class JstJavaFileGenerator {

	public static interface Logger {

		boolean isDebugEnabled();

		boolean isInfoEnabled();

		boolean isWarnEnabled();

		void debug(String message);

		void info(String message);

		void warn(String message);

		void error(String message);
	}

	public static final Logger DEFAULT_LOGGER = new Logger() {

		@Override
		public boolean isDebugEnabled() {
			return true;
		}

		@Override
		public boolean isInfoEnabled() {
			return true;
		}

		@Override
		public boolean isWarnEnabled() {
			return true;
		}

		@Override
		public void debug(String message) {
			System.out.println("DEBUG: " + message);
		}

		@Override
		public void info(String message) {
			System.out.println(" INFO: " + message);
		}

		@Override
		public void warn(String message) {
			System.out.println(" WARN: " + message);
		}

		@Override
		public void error(String message) {
			System.out.println("ERROR: " + message);
		}
	};

	@Inject
	private Provider<XtextResourceSet> resourceSetProvider;

	@Inject
	private IResourceFactory resourceFactory;

	@Inject
	private Provider<JavaIoFileSystemAccess> javaIoFileSystemAccessProvider;

	@Inject
	private IQualifiedNameProvider qualifiedNameProvider;

	@Inject
	private JvmModelGenerator generator;

	@Inject
	private IGeneratorConfigProvider generatorConfigProvider;

	@Inject
	private Provider<ResourceSetBasedResourceDescriptions> resourceSetDescriptionsProvider;

	public boolean generate(Iterable<File> jstFiles, String encoding, File outputDir, Logger logger) {
		ResourceSet resourceSet = loadJstFiles(jstFiles, encoding);
		List<Issue> issues = validate(resourceSet);
		boolean hasError = logIssues(logger, issues);
		if (hasError) {
			return false;
		}
		generateJavaFiles(outputDir, resourceSet, logger);
		return true;
	}

	private boolean logIssues(Logger logger, List<Issue> issues) {
		boolean hasError = false;
		for (Issue issue : issues) {
			String message = JstJavaFileGenerator.createIssueMessage(issue).toString();
			switch (issue.getSeverity()) {
				case INFO:
					logger.info(message);
					break;
				case WARNING:
					logger.warn(message);
					break;
				case ERROR:
					hasError = true;
					logger.error(message);
					break;
			}
		}
		return hasError;
	}

	private List<Issue> validate(ResourceSet resourceSet) {
		List<Issue> issues = Lists.newArrayList();
		List<Resource> resources = Lists.newArrayList(resourceSet.getResources());
		for (Resource resource : resources) {
			IResourceServiceProvider resourceServiceProvider = IResourceServiceProvider.Registry.INSTANCE
					.getResourceServiceProvider(resource.getURI());
			if (resourceServiceProvider != null) {
				IResourceValidator resourceValidator = resourceServiceProvider.getResourceValidator();
				List<Issue> result = resourceValidator.validate(resource, CheckMode.ALL, null);
				addAll(issues, result);
			}
		}
		return issues;
	}

	private ResourceSet loadJstFiles(Iterable<File> jstFiles, String encoding) {
		final ResourceSet resourceSet = resourceSetProvider.get();
		resourceSet.getLoadOptions().put(XtextResource.OPTION_ENCODING, encoding);
		for (File jstFile : jstFiles) {
			resourceSet.getResource(URI.createFileURI(jstFile.getAbsolutePath()), true);
		}
		return resourceSet;
	}

	private List<Issue> generateJavaFiles(File outputDir, ResourceSet resourceSet, Logger logger) {
		List<Issue> issues = new ArrayList<Issue>();
		JavaIoFileSystemAccess javaIoFileSystemAccess = javaIoFileSystemAccessProvider.get();
		javaIoFileSystemAccess.setOutputPath(outputDir.getAbsolutePath());
		ResourceSetBasedResourceDescriptions resourceDescriptions = getResourceDescriptions(resourceSet);
		Iterable<IEObjectDescription> exportedObjectsByType = resourceDescriptions
				.getExportedObjectsByType(JstPackage.eINSTANCE.getJstFile());
		if (logger.isInfoEnabled()) {
			int size = Iterables.size(exportedObjectsByType);
			if (size == 0) {
				logger.info("No sources to generate");
			} else {
				logger.info("Generating " + size + " source " + (size == 1 ? "file" : "files") + " into " + outputDir);
			}
		}
		for (IEObjectDescription eObjectDescription : exportedObjectsByType) {
			JvmDeclaredType jstFile = (JvmDeclaredType) eObjectDescription.getEObjectOrProxy();
			GeneratorConfig config = generatorConfigProvider.get(jstFile);
			CharSequence generatedType = generator.generateType(jstFile, config);
			String javaFileName = getJavaFileName(jstFile);
			if (logger.isDebugEnabled()) {
				logger.debug("write '" + outputDir + File.separator + javaFileName + "'");
			}
			javaIoFileSystemAccess.generateFile(javaFileName, generatedType);
		}
		return issues;
	}

	private ResourceSetBasedResourceDescriptions getResourceDescriptions(ResourceSet resourceSet) {
		ResourceSetBasedResourceDescriptions resourceDescriptions = resourceSetDescriptionsProvider.get();
		resourceDescriptions.setContext(resourceSet);
		resourceDescriptions.setRegistry(IResourceServiceProvider.Registry.INSTANCE);
		return resourceDescriptions;
	}

	private String getJavaFileName(JvmDeclaredType jstFile) {
		return Strings.concat("/", getFullyQualifiedName(jstFile).getSegments()) + ".java";
	}

	private QualifiedName getFullyQualifiedName(JvmDeclaredType jstFile) {
		return qualifiedNameProvider.getFullyQualifiedName(jstFile);
	}

	public JstFile parse(InputStream in, URI uriToUse, Map<?, ?> options, ResourceSet resourceSet) {
		Resource resource = resourceFactory.createResource(uriToUse);
		resourceSet.getResources().add(resource);
		try {
			resource.load(in, options);
			final JstFile root = (JstFile) (resource.getContents().isEmpty() ? null : resource.getContents().get(0));
			return root;
		} catch (IOException e) {
			throw new WrappedException(e);
		}
	}

	public static void main(String[] args) {
		Injector injector = new JstStandaloneSetup().createInjectorAndDoEMFRegistration();
		JstJavaFileGenerator generator = injector.getInstance(JstJavaFileGenerator.class);
		if ((args == null) || (args.length == 0)) {
			printUsage();
			return;
		}
		Iterator<String> arguments = Arrays.asList(args).iterator();
		File outputDir = null;
		String encoding = null;
		List<File> jstFiles = new ArrayList<File>();
		while (arguments.hasNext()) {
			String argument = arguments.next();
			if ("-d".equals(argument.trim())) {
				outputDir = new File(arguments.next().trim());
			} else if ("-encoding".equals(argument.trim())) {
				encoding = arguments.next().trim();
			} else {
				jstFiles.add(new File(argument));
			}
		}
		generator.generate(jstFiles, encoding, outputDir, DEFAULT_LOGGER);
	}

	public static String createIssueMessage(Issue issue) {
		StringBuilder issueBuilder = new StringBuilder("\n");
		issueBuilder.append(issue.getSeverity()).append(": \t");
		URI uriToProblem = issue.getUriToProblem();
		if (uriToProblem != null) {
			URI resourceUri = uriToProblem.trimFragment();
			issueBuilder.append(resourceUri.lastSegment()).append(" - ");
			if (resourceUri.isFile()) {
				issueBuilder.append(resourceUri.toFileString());
			}
		}
		issueBuilder.append("\n").append(issue.getLineNumber()).append(": ").append(issue.getMessage());
		return issueBuilder.toString();
	}

	private static void printUsage() {
		System.out.println("Usage: Main <options> <jst files>");
		System.out.println("where possible options include:");
		System.out.println("-d <directory>             Specify where to place generated java files");
		System.out.println("-encoding <encoding>       Specify character encoding used by source files");
	}
}
