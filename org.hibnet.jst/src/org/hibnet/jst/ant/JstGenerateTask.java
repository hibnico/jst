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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileProvider;
import org.hibnet.jst.JstJavaFileGenerator;
import org.hibnet.jst.JstJavaFileGenerator.Logger;
import org.hibnet.jst.JstStandaloneSetup;

import com.google.inject.Injector;

public class JstGenerateTask extends Task {

    private String encoding;

    private ResourceCollection sources;

    private File output;

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void add(ResourceCollection sources) {
        this.sources = sources;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    private Logger antLogger = new Logger() {

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
            log(message, Project.MSG_DEBUG);
        }

        @Override
        public void info(String message) {
            log(message, Project.MSG_INFO);
        }

        @Override
        public void warn(String message) {
            log(message, Project.MSG_WARN);
        }

        @Override
        public void error(String message) {
            log(message, Project.MSG_ERR);
        }

    };

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

        if (!generator.generate(jstFiles, encoding, output, antLogger)) {
            throw new BuildException("Error compiling jst templates");
        }
    }

}
