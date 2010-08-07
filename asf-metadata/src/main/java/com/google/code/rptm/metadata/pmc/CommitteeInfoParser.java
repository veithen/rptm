/*
 * Copyright 2010 Andreas Veithen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.rptm.metadata.pmc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class CommitteeInfoParser {
    private static final Pattern PROJECT_HEADER = Pattern.compile("\\* (\\S.*[^\\s\\)])(\\s*\\([^\\)]+\\))*\\s*");
    private static final Pattern MEMBER_ENTRY = Pattern.compile("\\s+(\\S.*[^\\s\\)])(\\s*\\(.*\\))?\\s+<(.*)>.*");
    private static final Pattern END_MARKER = Pattern.compile("=====*");
    
    private static final int STATE_PREAMBLE = 0;
    private static final int STATE_PROJECT = 1;
    private static final int STATE_PROJECT_COMPLETE = 2;
    private static final int STATE_COMPLETE = 3;
    
    public static void parse(InputStream in, CommitteeInfoVisitor visitor) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        int state = STATE_PREAMBLE;
        int i = 0;
        while (true) {
            String line = reader.readLine();
            i++;
            if (line == null) {
                if (state != STATE_COMPLETE) {
                    throw new IOException("Unexpected end of file (state = " + state);
                } else {
                    break;
                }
            } else if (state != STATE_PREAMBLE && END_MARKER.matcher(line).matches()) {
                if (state == STATE_PROJECT) {
                    visitor.endProject();
                }
                state = STATE_COMPLETE;
                continue;
            } else if ((state == STATE_PROJECT || state == STATE_PROJECT_COMPLETE) && StringUtils.isBlank(line)) {
                if (state == STATE_PROJECT) {
                    visitor.endProject();
                    state = STATE_PROJECT_COMPLETE;
                }
                continue;
            } else if (state == STATE_PROJECT) {
                Matcher m = MEMBER_ENTRY.matcher(line);
                if (m.matches()) {
                    visitor.member(m.group(1), m.group(3));
                    continue;
                }
            } else if (state == STATE_PREAMBLE || state == STATE_PROJECT_COMPLETE) {
                Matcher m = PROJECT_HEADER.matcher(line);
                if (m.matches()) {
                    visitor.startProject(m.group(1));
                    state = STATE_PROJECT;
                    continue;
                } else if (state == STATE_PREAMBLE) {
                    continue;
                }
            } else if (state == STATE_COMPLETE && StringUtils.isBlank(line)) {
                continue;
            }
            throw new IOException("Unexpected content at line " + i + " (state = " + state + "): " + line);
        }
    }
}
