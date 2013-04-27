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
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileProvider;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.validation.Issue;
import org.hibnet.jst.JstStandaloneSetup;
import org.hibnet.jst.generator.JstJavaFileGenerator;

import com.google.inject.Injector;

public class JstGenerateTask extends Task {

    private ResourceCollection sources;

    private File output;

    public void add(ResourceCollection sources) {
        this.sources = sources;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    @Override
    public void execute() throws BuildException {
        if (!sources.isFilesystemOnly()) {
            throw new BuildException("Only local files are supported for the sources");
        }

        List<File> jstFiles = new ArrayList<File>();
        @SuppressWarnings("unchecked")
        Iterator<Resource> itResource = sources.iterator();
        while (itResource.hasNext()) {
            FileProvider fp = (FileProvider) itResource.next().as(FileProvider.class);
            jstFiles.add(fp.getFile());
        }

        Injector injector = new JstStandaloneSetup().createInjectorAndDoEMFRegistration();
        JstJavaFileGenerator generator = injector.getInstance(JstJavaFileGenerator.class);

        List<Issue> issues = generator.generate(jstFiles, output);
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
