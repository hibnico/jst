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
package org.hibnet.jst.generator;

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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.access.impl.ClasspathTypeProvider;
import org.eclipse.xtext.common.types.access.impl.IndexedJvmTypeAccess;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.mwe.NameBasedFilter;
import org.eclipse.xtext.mwe.PathTraverser;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.impl.ResourceSetBasedResourceDescriptions;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.compiler.IGeneratorConfigProvider;
import org.eclipse.xtext.xbase.compiler.JvmModelGenerator;
import org.eclipse.xtext.xbase.jvmmodel.JvmModelAssociator;
import org.hibnet.jst.jst.JstFile;
import org.hibnet.jst.jvmmodel.JstJvmModelInferrer;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class JstJavaFileGenerator {

    private final static class SeverityFilter implements Predicate<Issue> {
        private static final SeverityFilter WARNING = new SeverityFilter(Severity.WARNING);
        private static final SeverityFilter ERROR = new SeverityFilter(Severity.ERROR);
        private Severity severity;

        private SeverityFilter(Severity severity) {
            this.severity = severity;
        }

        @Override
        public boolean apply(Issue issue) {
            return this.severity == issue.getSeverity();
        }
    }

    private final static Logger log = Logger.getLogger(JstJavaFileGenerator.class.getName());

    protected static final FileFilter ACCEPT_ALL_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return true;
        }
    };

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
    private JvmModelAssociator jvmModelAssociator;
    @Inject
    private IQualifiedNameProvider qualifiedNameProvider;
    @Inject
    private IndexedJvmTypeAccess indexedJvmTypeAccess;
    @Inject
    private IGeneratorConfigProvider generatorConfigprovider;
    //   @Inject
    //    private ProcessorInstanceForJvmTypeProvider annotationProcessorFactory;
    @Inject
    private IEncodingProvider.Runtime encodingProvider;

    protected Writer outputWriter;
    protected Writer errorWriter;
    protected List<File> sourcePath;
    protected List<File> classPath;
    protected boolean useCurrentClassLoaderAsParent;
    protected File outputPath;
    protected String fileEncoding;
    protected String complianceLevel = "1.5";
    protected boolean verbose = false;
    protected File tempDirectory = new File(System.getProperty("java.io.tmpdir"));
    protected boolean deleteTempDirectory = true;
    protected List<File> tempFolders = Lists.newArrayList();
    protected boolean writeTraceFiles = true;

    public void setUseCurrentClassLoaderAsParent(boolean useCurrentClassLoaderAsParent) {
        this.useCurrentClassLoaderAsParent = useCurrentClassLoaderAsParent;
    }

    public void setTempDirectory(File tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public boolean isWriteTraceFiles() {
        return writeTraceFiles;
    }

    public void setWriteTraceFiles(boolean writeTraceFiles) {
        this.writeTraceFiles = writeTraceFiles;
    }

    @Inject
    public void setResourceSetProvider(Provider<ResourceSet> resourceSetProvider) {
        this.resourceSetProvider = resourceSetProvider;
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

    public void setClassPath(List<File> classPath) {
        this.classPath = classPath;
    }

    public void setOutputPath(File outputPath) {
        this.outputPath = outputPath;
    }

    public void setSourcePath(List<File> sourcePath) {
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

    public List<Issue> compile() {
        try {
            ResourceSet resourceSet = loadJstFiles();
            File sourceDirectory = createStubs(resourceSet);
            File classDirectory = createTempDir("classes");
            if (!preCompileStubs(sourceDirectory, classDirectory)) {
                log.debug("Compilation of stubs and existing Java code had errors. This is expected and usually is not a probblem.");
            }
            installJvmTypeProvider(resourceSet, classDirectory);
            EcoreUtil.resolveAll(resourceSet);
            List<Issue> issues = validate(resourceSet);
            Iterable<Issue> errors = Iterables.filter(issues, SeverityFilter.ERROR);
            if (!Iterables.isEmpty(errors)) {
                return issues;
            }
            generateJavaFiles(resourceSet);
        } finally {
            if (isDeleteTempDirectory()) {
                deleteTmpFolders();
            }
        }
        return null;
    }

    protected ResourceSet loadJstFiles() {
        final ResourceSet resourceSet = resourceSetProvider.get();
        encodingProvider.setDefaultEncoding(getFileEncoding());
        final NameBasedFilter nameBasedFilter = new NameBasedFilter();
        nameBasedFilter.setExtension(fileExtensionProvider.getPrimaryFileExtension());
        PathTraverser pathTraverser = new PathTraverser();
        pathTraverser.resolvePathes(getSourcePathDirectories(), new Predicate<URI>() {
            @Override
            public boolean apply(URI input) {
                boolean matches = nameBasedFilter.matches(input);
                if (matches) {
                    if (log.isDebugEnabled()) {
                        log.debug("load jst file '" + input + "'");
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
        fileSystemAccess.setOutputPath(outputDirectory.getAbsolutePath());
        for (Resource resource : resourceSet.getResources()) {
            JstFile jstFile = getJstFile(resource);
            if (jstFile == null) {
                continue;
            }
            StringBuilder classSignatureBuilder = new StringBuilder();
            if (!Strings.isEmpty(jstFile.getPackageName())) {
                classSignatureBuilder.append("package " + jstFile.getPackageName() + ";");
                classSignatureBuilder.append("\n");
            }
            classSignatureBuilder.append("public ");
            classSignatureBuilder.append("class ");
            classSignatureBuilder.append(JstJvmModelInferrer.getClassName(jstFile) + "{}");
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
        if (classPath != null && !isEmpty(classPath)) {
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

        // for annotation processing we need to have the compiler's classpath as a parent.
        URLClassLoader urlClassLoaderForAnnotationProcessing = new URLClassLoader(toArray(classPathUrls, URL.class),
                getClass().getClassLoader());
        //        annotationProcessorFactory.setClassLoader(urlClassLoaderForAnnotationProcessing);
    }

    protected void reportIssues(Iterable<Issue> issues) {
        for (Issue issue : issues) {
            StringBuilder issueBuilder = createIssueMessage(issue);
            if (Severity.ERROR == issue.getSeverity()) {
                log.error(issueBuilder.toString());
            } else if (Severity.WARNING == issue.getSeverity()) {
                log.warn(issueBuilder.toString());
            }
        }
    }

    private StringBuilder createIssueMessage(Issue issue) {
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
        javaIoFileSystemAccess.setOutputPath(outputPath.getAbsolutePath());
        javaIoFileSystemAccess.setWriteTrace(writeTraceFiles);
        //        if (log.isInfoEnabled()) {
        //            int size = Iterables.size(resourceSet.getResources());
        //            if (size == 0) {
        //                log.info("No sources to compile in '" + sourcePath + "'");
        //            } else {
        //                log.info("Compiling " + size + " source " + (size == 1 ? "file" : "files") + " to " + outputPath);
        //            }
        //        }
        for (Resource resource : resourceSet.getResources()) {
            JstFile jstFile = getJstFile(resource);
            if (jstFile == null) {
                continue;
            }
            JvmDeclaredType jvmGenericType = getFirstOrNull(jvmModelAssociator.getJvmElements(jstFile),
                    JvmDeclaredType.class);
            CharSequence generatedType = generator.generateType(jvmGenericType, generatorConfigprovider.get(jstFile));
            if (log.isDebugEnabled()) {
                log.debug("write '" + outputPath + File.separator + getJavaFileName(jstFile) + "'");
            }
            javaIoFileSystemAccess.generateFile(getJavaFileName(jstFile), generatedType);
        }
    }

    protected <T> T getFirstOrNull(Iterable<EObject> elements, Class<T> type) {
        Iterator<T> iterator = filter(elements, type).iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    protected ResourceSetBasedResourceDescriptions getResourceDescriptions(ResourceSet resourceSet) {
        ResourceSetBasedResourceDescriptions resourceDescriptions = resourceSetDescriptionsProvider.get();
        resourceDescriptions.setContext(resourceSet);
        resourceDescriptions.setRegistry(IResourceServiceProvider.Registry.INSTANCE);
        return resourceDescriptions;
    }

    private String getJavaFileName(JstFile jstFile) {
        return jstFile.getPackageName().replaceAll("\\.", "/") + "/" + JstJvmModelInferrer.getClassName(jstFile)
                + ".java";
    }

    protected JstFile getJstFile(Resource resource) {
        if (resource instanceof XtextResource) {
            XtextResource xtextResource = (XtextResource) resource;
            IParseResult parseResult = xtextResource.getParseResult();
            if (parseResult != null) {
                EObject model = parseResult.getRootASTElement();
                if (model instanceof JstFile) {
                    JstFile jstFile = (JstFile) model;
                    return jstFile;
                }
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

    protected List<String> getDirectories(List<File> path) {
        if (path == null || path.isEmpty()) {
            return Lists.newArrayList();
        }
        return transform(path, new Function<File, String>() {
            @Override
            public String apply(File from) {
                try {
                    return from.getCanonicalPath();
                } catch (IOException e) {
                    throw new WrappedException(e);
                }
            }
        });
    }

    protected File createTempDir(String prefix) {
        File tempDir = new File(tempDirectory, prefix + System.nanoTime());
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
    protected static boolean cleanFolder(File parentFolder, FileFilter filter, boolean continueOnError,
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

}