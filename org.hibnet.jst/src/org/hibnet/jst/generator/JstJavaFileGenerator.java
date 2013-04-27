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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import com.google.inject.Provider;

public class JstJavaFileGenerator {

    @Inject
    private Provider<ResourceSet> resourceSetProvider;
    @Inject
    private IResourceValidator validator;
    @Inject
    private IGenerator generator;
    @Inject
    private JavaIoFileSystemAccess fileAccess;

    public List<Issue> generate(List<File> sources, File output) {
        ResourceSet set = resourceSetProvider.get();

        List<Resource> resources = new ArrayList<Resource>();
        List<Issue> issues = new ArrayList<Issue>();

        for (File source : sources) {
            Resource resource = set.getResource(URI.createURI(source.toURI().toString()), true);
            resources.add(resource);
            issues.addAll(validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl));
        }

        if (!issues.isEmpty()) {
            return issues;
        }

        fileAccess.setOutputPath(output.getAbsolutePath());

        for (Resource resource : resources) {
            generator.doGenerate(resource, fileAccess);
        }

        return null;
    }

}