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

import static com.google.common.collect.Lists.*;
import static org.eclipse.xtext.util.Strings.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.validation.Issue;
import org.hibnet.jst.JstStandaloneSetup;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Injector;

public class Main {

    public static void main(String[] args) {
        if ((args == null) || (args.length == 0)) {
            printUsage();
            return;
        }

        Injector injector = new JstStandaloneSetup().createInjectorAndDoEMFRegistration();
        JstJavaFileGenerator generator = injector.getInstance(JstJavaFileGenerator.class);

        Iterator<String> arguments = Arrays.asList(args).iterator();
        while (arguments.hasNext()) {
            String argument = arguments.next();
            if ("-d".equals(argument.trim())) {
                generator.setOutputPath(new File(arguments.next().trim()));
            } else if ("-classpath".equals(argument.trim()) || "-cp".equals(argument.trim())) {
                generator.setClassPath(asFileList(arguments.next().trim()));
            } else if ("-tempdir".equals(argument.trim()) || "-td".equals(argument.trim())) {
                generator.setTempDirectory(new File(arguments.next().trim()));
            } else if ("-encoding".equals(argument.trim())) {
                generator.setFileEncoding(arguments.next().trim());
            } else {
                generator.setSourcePath(asFileList(argument));
            }
        }
        List<Issue> issues = generator.compile();
        boolean hasError = false;
        if (issues != null) {
            Map<String, List<Issue>> issuesByFile = new TreeMap<String, List<Issue>>();
            for (Issue issue : issues) {
                if (issue.getSeverity() == Severity.ERROR) {
                    hasError = true;
                }
                String file = issue.getUriToProblem().trimFragment().toFileString();
                List<Issue> fileIssues = issuesByFile.get(file);
                if (fileIssues == null) {
                    fileIssues = new ArrayList<Issue>();
                    issuesByFile.put(file, fileIssues);
                }
                fileIssues.add(issue);
            }

            for (Entry<String, List<Issue>> entry : issuesByFile.entrySet()) {
                System.out.println("In file " + entry.getKey());
                for (Issue issue : entry.getValue()) {
                    System.out.println("    line " + issue.getLineNumber() + ": [" + issue.getSeverity() + "] "
                            + issue.getMessage());
                }
            }
        }

        if (hasError) {
            System.exit(1);
        }
    }

    private static List<File> asFileList(String path) {
        if (Strings.isEmpty(path)) {
            return Lists.newArrayList();
        }
        final List<String> split = split(emptyIfNull(path), File.pathSeparator);
        return transform(split, new Function<String, File>() {
            @Override
            public File apply(String from) {
                return new File(from);
            }
        });
    }

    private static void printUsage() {
        System.out.println("Usage: Main <options> <source directories>");
        System.out.println("where possible options include:");
        System.out.println("-d <directory>             Specify where to place generated jst files");
        System.out.println("-tp <path>                 Temp directory to hold generated stubs and classes");
        System.out.println("-cp <path>                 Specify where to find user class files");
        System.out.println("-encoding <encoding>       Specify character encoding used by source files");
    }
}
