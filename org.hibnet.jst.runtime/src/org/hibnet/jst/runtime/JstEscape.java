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
package org.hibnet.jst.runtime;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.StringEscapeUtils;

public class JstEscape {

    public static void unescape(final Writer out, final Object object, final boolean elvis) throws IOException {
      if (object != null) {
        out.append(object.toString());
      } else if (!elvis) {
        out.append("null");
      }
    }
    
    public static void escapeXml(final Writer out, final Object object, final boolean elvis) throws IOException {
      if (object != null) {
        out.append(StringEscapeUtils.escapeXml(object.toString()));
      } else if (!elvis) {
        out.append("null");
      }
    }
    
    public static void escapeHtml(final Writer out, final Object object, final boolean elvis) throws IOException {
      if (object != null) {
        out.append(StringEscapeUtils.escapeHtml(object.toString()));
      } else if (!elvis) {
        out.append("null");
      }
    }
    
    public static void escpaeJs(final Writer out, final Object object, final boolean elvis) throws IOException {
      if (object != null) {
        out.append(StringEscapeUtils.escapeJavaScript(object.toString()));
      } else if (!elvis) {
        out.append("null");
      }
    }
    
    public static void escapeJava(final Writer out, final Object object, final boolean elvis) throws IOException {
      if (object != null) {
        out.append(StringEscapeUtils.escapeJava(object.toString()));
      } else if (!elvis) {
        out.append("null");
      }
    }
    
    public static void escapeCsv(final Writer out, final Object object, final boolean elvis) throws IOException {
      if (object != null) {
        out.append(StringEscapeUtils.escapeCsv(object.toString()));
      } else if (!elvis) {
        out.append("null");
      }
    }
    
    public static void escapeSql(final Writer out, final Object object, final boolean elvis) throws IOException {
      if (object != null) {
        out.append(StringEscapeUtils.escapeSql(object.toString()));
      } else if (!elvis) {
        out.append("null");
      }
    }

}
