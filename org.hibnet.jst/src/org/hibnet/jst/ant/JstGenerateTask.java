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
package org.hibnet.jst.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.validation.Issue;
import org.hibnet.jst.JstStandaloneSetup;
import org.hibnet.jst.generator.JstJavaFileGenerator;

import com.google.inject.Injector;

public class JstGenerateTask extends Task {

    private String encoding;

    private Path src;

    private File output;

    private File tmp;

    private Path compileClasspath;

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public void setTmp(File tmp) {
        this.tmp = tmp;
    }

    /**
     * Adds a path for source compilation.
     * 
     * @return a nested src element.
     */
    public Path createSrc() {
        if (src == null) {
            src = new Path(getProject());
        }
        return src.createPath();
    }

    /**
     * Recreate src.
     * 
     * @return a nested src element.
     */
    protected Path recreateSrc() {
        src = null;
        return createSrc();
    }

    /**
     * Set the source directories to find the source Java files.
     * 
     * @param srcDir
     *            the source directories as a path
     */
    public void setSrcdir(Path srcDir) {
        if (src == null) {
            src = srcDir;
        } else {
            src.append(srcDir);
        }
    }

    /**
     * Gets the source dirs to find the source java files.
     * 
     * @return the source directories as a path
     */
    public Path getSrcdir() {
        return src;
    }

    /**
     * Set the classpath to be used for this compilation.
     * 
     * @param classpath
     *            an Ant Path object containing the compilation classpath.
     */
    public void setClasspath(Path classpath) {
        if (compileClasspath == null) {
            compileClasspath = classpath;
        } else {
            compileClasspath.append(classpath);
        }
    }

    /**
     * Gets the classpath to be used for this compilation.
     * 
     * @return the class path
     */
    public Path getClasspath() {
        return compileClasspath;
    }

    /**
     * Adds a path to the classpath.
     * 
     * @return a class path to be configured
     */
    public Path createClasspath() {
        if (compileClasspath == null) {
            compileClasspath = new Path(getProject());
        }
        return compileClasspath.createPath();
    }

    /**
     * Adds a reference to a classpath defined elsewhere.
     * 
     * @param r
     *            a reference to a classpath
     */
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    @Override
    public void execute() throws BuildException {

        Injector injector = new JstStandaloneSetup().createInjectorAndDoEMFRegistration();
        JstJavaFileGenerator generator = injector.getInstance(JstJavaFileGenerator.class);

        if (encoding != null) {
            generator.setFileEncoding(encoding);
        }
        if (tmp != null) {
            generator.setTempDirectory(tmp);
        }

        generator.setSourcePath(pathAsFiles(src));
        generator.setOutputPath(output);
        generator.setClassPath(pathAsFiles(compileClasspath));

        List<Issue> issues = generator.compile();
        boolean hasError = false;
        if (issues != null) {
            Map<String, List<Issue>> issuesByFile = new TreeMap<String, List<Issue>>();
            Map<String, Severity> worstSeverityByFile = new TreeMap<String, Severity>();
            for (Issue issue : issues) {
                if (issue.getSeverity() == Severity.ERROR) {
                    hasError = true;
                }
                String file = issue.getUriToProblem().trimFragment().toFileString();
                List<Issue> fileIssues = issuesByFile.get(file);
                Severity severity = worstSeverityByFile.get(file);
                if (fileIssues == null) {
                    fileIssues = new ArrayList<Issue>();
                    issuesByFile.put(file, fileIssues);
                }
                if (severity == null || issue.getSeverity().ordinal() < severity.ordinal()) {
                    worstSeverityByFile.put(file, issue.getSeverity());
                }
                fileIssues.add(issue);
            }

            for (Entry<String, List<Issue>> entry : issuesByFile.entrySet()) {
                log("In file " + entry.getKey(), getLogLevel(worstSeverityByFile.get(entry.getKey())));
                for (Issue issue : entry.getValue()) {
                    int level = getLogLevel(issue.getSeverity());
                    log("    line " + issue.getLineNumber() + ": [" + issue.getSeverity() + "] " + issue.getMessage(),
                            level);
                }
            }
        }

        if (hasError) {
            throw new BuildException("Error compiling jst templates");
        }
    }

    private List<File> pathAsFiles(Path path) {
        List<File> dirs = new ArrayList<File>();
        if (path != null) {
            @SuppressWarnings("unchecked")
            Iterator<Resource> itResource = path.iterator();
            while (itResource.hasNext()) {
                FileProvider provider = (FileProvider) itResource.next().as(FileProvider.class);
                dirs.add(provider.getFile());

            }
        }
        return dirs;
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

    private int getLogLevel(Severity severity) {
        switch (severity) {
            case ERROR:
                return Project.MSG_ERR;
            case WARNING:
                return Project.MSG_WARN;
            case INFO:
                return Project.MSG_INFO;
            default:
                return Project.MSG_INFO;
        }
    }
}
