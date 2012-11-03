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

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static java.util.Arrays.*;
import static org.eclipse.xtext.util.Strings.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.xtext.common.types.JvmGenericType;
import org.eclipse.xtext.common.types.access.impl.ClasspathTypeProvider;
import org.eclipse.xtext.common.types.access.impl.IndexedJvmTypeAccess;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.mwe.NameBasedFilter;
import org.eclipse.xtext.mwe.PathTraverser;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.impl.ResourceSetBasedResourceDescriptions;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.compiler.JvmModelGenerator;
import org.hibnet.jst.JstStandaloneSetup;
import org.hibnet.jst.jst.JstFile;
import org.hibnet.jst.jst.JstPackage;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

public class JstBatchCompiler {

	public final static class SeverityFilter implements Predicate<Issue> {

		public static final SeverityFilter WARNING = new SeverityFilter(Severity.WARNING);

		public static final SeverityFilter ERROR = new SeverityFilter(Severity.ERROR);

		private Severity severity;

		private SeverityFilter(Severity severity) {
			this.severity = severity;
		}

		@Override
		public boolean apply(Issue issue) {
			return this.severity == issue.getSeverity();
		}
	}

	private Logger log;

	protected static final FileFilter ACCEPT_ALL_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return true;
		}
	};

	public static interface Logger {

		boolean isDebugEnabled();

		boolean isInfoEnabled();

		void debug(String message);

		void info(String message);

		void warn(String message);

		void error(String message);
	}

	@Inject
	protected Provider<ResourceSet> resourceSetProvider;
	@Inject
	protected Provider<JavaIoFileSystemAccess> javaIoFileSystemAccessProvider;
	@Inject
	protected FileExtensionProvider fileExtensionProvider;
	@Inject
	protected Provider<ResourceSetBasedResourceDescriptions> resourceSetDescriptionsProvider;
	@Inject
	private JvmModelGenerator generator;
	@Inject
	private IQualifiedNameProvider qualifiedNameProvider;
	@Inject
	private IndexedJvmTypeAccess indexedJvmTypeAccess;

	protected Writer outputWriter;
	protected Writer errorWriter;
	protected String sourcePath;
	protected String classPath;
	protected boolean useCurrentClassLoaderAsParent;
	protected String outputPath;
	protected String fileEncoding;
	protected String complianceLevel = "1.5";
	protected boolean verbose = false;
	protected String tempDirectory = System.getProperty("java.io.tmpdir");
	protected boolean deleteTempDirectory = true;
	protected List<File> tempFolders = Lists.newArrayList();

	public void setLog(Logger log) {
		this.log = log;
	}

	public void setUseCurrentClassLoaderAsParent(boolean useCurrentClassLoaderAsParent) {
		this.useCurrentClassLoaderAsParent = useCurrentClassLoaderAsParent;
	}

	public String getTempDirectory() {
		return tempDirectory;
	}

	public void setTempDirectory(String tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	public boolean isDeleteTempDirectory() {
		return deleteTempDirectory;
	}

	public void setDeleteTempDirectory(boolean deletetempDirectory) {
		this.deleteTempDirectory = deletetempDirectory;
	}

	public Writer getOutputWriter() {
		if (outputWriter == null) {
			outputWriter = new Writer() {
				@Override
				public void write(char[] data, int offset, int count) throws IOException {
					String message = String.copyValueOf(data, offset, count);
					if (!Strings.isEmpty(message.trim())) {
						log.debug(message);
					}
				}

				@Override
				public void flush() throws IOException {
				}

				@Override
				public void close() throws IOException {
				}
			};
		}
		return outputWriter;
	}

	public void setOutputWriter(Writer ouputWriter) {
		this.outputWriter = ouputWriter;
	}

	public Writer getErrorWriter() {
		if (errorWriter == null) {
			errorWriter = new Writer() {
				@Override
				public void write(char[] data, int offset, int count) throws IOException {
					String message = String.copyValueOf(data, offset, count);
					if (!Strings.isEmpty(message.trim())) {
						log.debug(message);
					}
				}

				@Override
				public void flush() throws IOException {
				}

				@Override
				public void close() throws IOException {
				}
			};
		}
		return errorWriter;
	}

	public void setErrorWriter(Writer errorWriter) {
		this.errorWriter = errorWriter;
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	protected String getComplianceLevel() {
		return complianceLevel;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	protected boolean isVerbose() {
		return verbose;
	}

	public String getFileEncoding() {
		return fileEncoding;
	}

	public void setFileEncoding(String encoding) {
		this.fileEncoding = encoding;
	}

	public boolean compile() {
		try {
			ResourceSet resourceSet = loadXtendFiles();
			File sourceDirectory = createStubs(resourceSet);
			File classDirectory = createTempDir("classes");
			if (!preCompileStubs(sourceDirectory, classDirectory)) {
				log.debug("Compilation of stubs and existing Java code had errors. This is expected and usually is not a probblem.");
			}
			installJvmTypeProvider(resourceSet, classDirectory);
			EcoreUtil.resolveAll(resourceSet);
			List<Issue> issues = validate(resourceSet);
			boolean hasError = false;
			for (Issue issue : issues) {
				String message = createIssueMessage(issue).toString();
				switch (issue.getSeverity()) {
					case INFO:
						log.info(message);
						break;
					case WARNING:
						log.warn(message);
						break;
					case ERROR:
						hasError = true;
						log.error(message);
						break;
				}
			}
			if (hasError) {
				return false;
			}
			generateJavaFiles(resourceSet);
		} finally {
			if (isDeleteTempDirectory()) {
				deleteTmpFolders();
			}
		}
		return true;
	}

	protected ResourceSet loadXtendFiles() {
		final ResourceSet resourceSet = resourceSetProvider.get();
		resourceSet.getLoadOptions().put(XtextResource.OPTION_ENCODING, getFileEncoding());
		final NameBasedFilter nameBasedFilter = new NameBasedFilter();
		nameBasedFilter.setExtension(fileExtensionProvider.getPrimaryFileExtension());
		PathTraverser pathTraverser = new PathTraverser();
		pathTraverser.resolvePathes(getSourcePathDirectories(), new Predicate<URI>() {
			@Override
			public boolean apply(URI input) {
				boolean matches = nameBasedFilter.matches(input);
				if (matches) {
					if (log.isDebugEnabled()) {
						log.debug("load xtend file '" + input + "'");
					}
					resourceSet.getResource(input, true);
				}
				return matches;
			}
		});
		return resourceSet;
	}

	protected File createStubs(ResourceSet resourceSet) {
		File outputDirectory = createTempDir("stubs");
		JavaIoFileSystemAccess fileSystemAccess = javaIoFileSystemAccessProvider.get();
		fileSystemAccess.setOutputPath(outputDirectory.toString());
		for (Resource resource : resourceSet.getResources()) {
			JstFile jstFile = getJstFile(resource);
			StringBuilder classSignatureBuilder = new StringBuilder();
			if (!Strings.isEmpty(jstFile.getPackageName())) {
				classSignatureBuilder.append("package " + jstFile.getPackageName() + ";");
				classSignatureBuilder.append("\n");
			}
			classSignatureBuilder.append("public class " + jstFile.getSimpleName() + "{}");
			if (log.isDebugEnabled()) {
				log.debug("create java stub '" + getJavaFileName(jstFile) + "'");
			}
			fileSystemAccess.generateFile(getJavaFileName(jstFile), classSignatureBuilder.toString());
		}
		return outputDirectory;
	}

	protected boolean preCompileStubs(File tmpSourceDirectory, File classDirectory) {
		List<String> commandLine = Lists.newArrayList();
		// todo args
		if (isVerbose()) {
			commandLine.add("-verbose");
		}
		if (!isEmpty(classPath)) {
			commandLine.add("-cp \"" + concat(File.pathSeparator, getClassPathEntries()) + "\"");
		}
		commandLine.add("-d \"" + classDirectory.toString() + "\"");
		commandLine.add("-" + getComplianceLevel());
		commandLine.add("-proceedOnError");
		List<String> sourceDirectories = newArrayList(getSourcePathDirectories());
		sourceDirectories.add(tmpSourceDirectory.toString());
		commandLine.add(concat(" ", transform(sourceDirectories, new Function<String, String>() {

			@Override
			public String apply(String path) {
				return "\"" + path + "\"";
			}
		})));
		if (log.isDebugEnabled()) {
			log.debug("invoke batch compiler with '" + concat(" ", commandLine) + "'");
		}
		return BatchCompiler.compile(concat(" ", commandLine), new PrintWriter(getOutputWriter()), new PrintWriter(
				getErrorWriter()), null);
	}

	protected List<Issue> validate(ResourceSet resourceSet) {
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

	protected void installJvmTypeProvider(ResourceSet resourceSet, File tmpClassDirectory) {
		Iterable<String> classPathEntries = concat(getClassPathEntries(), getSourcePathDirectories(),
				asList(tmpClassDirectory.toString()));
		classPathEntries = filter(classPathEntries, new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return !Strings.isEmpty(input.trim());
			}
		});
		Iterable<URL> classPathUrls = Iterables.transform(classPathEntries, new Function<String, URL>() {

			@Override
			public URL apply(String from) {
				try {
					return new File(from).toURI().toURL();
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}
		});
		if (log.isDebugEnabled()) {
			log.debug("classpath used for Xtend compilation : " + classPathUrls);
		}
		URLClassLoader urlClassLoader = new URLClassLoader(toArray(classPathUrls, URL.class),
				useCurrentClassLoaderAsParent ? getClass().getClassLoader() : null);
		new ClasspathTypeProvider(urlClassLoader, resourceSet, indexedJvmTypeAccess);
		((XtextResourceSet) resourceSet).setClasspathURIContext(urlClassLoader);
	}

	public StringBuilder createIssueMessage(Issue issue) {
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
		return issueBuilder;
	}

	protected void generateJavaFiles(ResourceSet resourceSet) {
		JavaIoFileSystemAccess javaIoFileSystemAccess = javaIoFileSystemAccessProvider.get();
		javaIoFileSystemAccess.setOutputPath(outputPath);
		ResourceSetBasedResourceDescriptions resourceDescriptions = getResourceDescriptions(resourceSet);
		Iterable<IEObjectDescription> exportedObjectsByType = resourceDescriptions
				.getExportedObjectsByType(JstPackage.eINSTANCE.getJstFile());
		if (log.isInfoEnabled()) {
			int size = Iterables.size(exportedObjectsByType);
			if (size == 0) {
				log.info("No sources to compile in '" + sourcePath + "'");
			} else {
				log.info("Compiling " + size + " source " + (size == 1 ? "file" : "files") + " to " + outputPath);
			}
		}
		for (IEObjectDescription eObjectDescription : exportedObjectsByType) {
			JstFile jstFile = (JstFile) eObjectDescription.getEObjectOrProxy();
			JvmGenericType jvmGenericType = jstFile;
			CharSequence generatedType = generator.generateType(jvmGenericType);
			if (log.isDebugEnabled()) {
				log.debug("write '" + outputPath + File.separator + getJavaFileName(jstFile) + "'");
			}
			javaIoFileSystemAccess.generateFile(getJavaFileName(jstFile), generatedType);
		}
	}

	protected ResourceSetBasedResourceDescriptions getResourceDescriptions(ResourceSet resourceSet) {
		ResourceSetBasedResourceDescriptions resourceDescriptions = resourceSetDescriptionsProvider.get();
		resourceDescriptions.setContext(resourceSet);
		resourceDescriptions.setRegistry(IResourceServiceProvider.Registry.INSTANCE);
		return resourceDescriptions;
	}

	private String getJavaFileName(JstFile jstFile) {
		return Strings.concat("/", getFullyQualifiedName(jstFile).getSegments()) + ".java";
	}

	private QualifiedName getFullyQualifiedName(JstFile jstFile) {
		return qualifiedNameProvider.getFullyQualifiedName(jstFile);
	}

	protected JstFile getJstFile(Resource resource) {
		XtextResource xtextResource = (XtextResource) resource;
		IParseResult parseResult = xtextResource.getParseResult();
		if (parseResult != null) {
			EObject model = parseResult.getRootASTElement();
			if (model instanceof JstFile) {
				JstFile jstFile = (JstFile) model;
				return jstFile;
			}
		}
		return null;
	}

	protected List<String> getClassPathEntries() {
		return getDirectories(classPath);
	}

	protected List<String> getSourcePathDirectories() {
		return getDirectories(sourcePath);
	}

	protected List<String> getDirectories(String path) {
		if (Strings.isEmpty(path)) {
			return Lists.newArrayList();
		}
		final List<String> split = split(emptyIfNull(path), File.pathSeparator);
		return transform(split, new Function<String, String>() {
			@Override
			public String apply(String from) {
				try {
					return new File(from).getCanonicalPath();
				} catch (IOException e) {
					throw new WrappedException(e);
				}
			}
		});
	}

	protected File createTempDir(String prefix) {
		File tempDir = new File(getTempDirectory(), prefix + System.nanoTime());
		cleanFolder(tempDir, ACCEPT_ALL_FILTER, true, true);
		if (!tempDir.mkdirs()) {
			throw new RuntimeException("Error creating temp directory '" + tempDir.getAbsolutePath() + "'");
		}
		tempFolders.add(tempDir);
		return tempDir;
	}

	protected void deleteTmpFolders() {
		for (File file : tempFolders) {
			cleanFolder(file, ACCEPT_ALL_FILTER, true, true);
		}
	}

	// FIXME: use Files#cleanFolder after the maven distro availability of version 2.2.x
	protected boolean cleanFolder(File parentFolder, FileFilter filter, boolean continueOnError,
			boolean deleteParentFolder) {
		if (!parentFolder.exists()) {
			return true;
		}
		if (filter == null)
			filter = ACCEPT_ALL_FILTER;
		log.debug("Cleaning folder " + parentFolder.toString());
		final File[] contents = parentFolder.listFiles(filter);
		for (int j = 0; j < contents.length; j++) {
			final File file = contents[j];
			if (file.isDirectory()) {
				if (!cleanFolder(file, filter, continueOnError, true) && !continueOnError)
					return false;
			} else {
				if (!file.delete()) {
					log.error("Couldn't delete " + file.getAbsolutePath());
					if (!continueOnError)
						return false;
				}
			}
		}
		if (deleteParentFolder) {
			if (parentFolder.list().length == 0 && !parentFolder.delete()) {
				log.error("Couldn't delete " + parentFolder.getAbsolutePath());
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		Injector injector = new JstStandaloneSetup().createInjectorAndDoEMFRegistration();
		JstBatchCompiler compiler = injector.getInstance(JstBatchCompiler.class);
		if ((args == null) || (args.length == 0)) {
			printUsage();
			return;
		}
		Iterator<String> arguments = Arrays.asList(args).iterator();
		while (arguments.hasNext()) {
			String argument = arguments.next();
			if ("-d".equals(argument.trim())) {
				compiler.setOutputPath(arguments.next().trim());
			} else if ("-classpath".equals(argument.trim()) || "-cp".equals(argument.trim())) {
				compiler.setClassPath(arguments.next().trim());
			} else if ("-tempdir".equals(argument.trim()) || "-td".equals(argument.trim())) {
				compiler.setTempDirectory(arguments.next().trim());
			} else if ("-encoding".equals(argument.trim())) {
				compiler.setFileEncoding(arguments.next().trim());
			} else {
				compiler.setSourcePath(argument);
			}
		}
		compiler.compile();
	}
	
	private static void printUsage() {
		System.out.println("Usage: Main <options> <source directories>");
		System.out.println("where possible options include:");
		System.out.println("-d <directory>             Specify where to place generated xtend files");
		System.out.println("-tp <path>                 Temp directory to hold generated stubs and classes");
		System.out.println("-cp <path>                 Specify where to find user class files");
		System.out.println("-encoding <encoding>       Specify character encoding used by source files");
	}

}
