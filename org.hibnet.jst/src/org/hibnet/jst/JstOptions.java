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
package org.hibnet.jst;

import org.eclipse.xtext.xbase.XStringLiteral;
import org.hibnet.jst.jst.JstFile;
import org.hibnet.jst.jst.JstOption;

public class JstOptions {

    public static String getStringOption(JstFile jstFile, String name) {
        for (JstOption option : jstFile.getOptions()) {
            if (option.getKey().equals(name) && option.getValue() instanceof XStringLiteral) {
                return ((XStringLiteral) option.getValue()).getValue();
            }
        }
        return null;
    }
}
