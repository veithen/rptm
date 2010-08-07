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
package com.google.code.rptm.scm.svn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;

import com.google.code.rptm.scm.FilenameMatcher;

class GlobMatcher implements FilenameMatcher {
    private Set<String> names;
    private List<Pattern> patterns;
    private GlobCompiler compiler;

    void addGlob(String glob) {
        if (glob.indexOf('*') != -1 || glob.indexOf('?') != -1) {
            if (compiler == null) {
                compiler = new GlobCompiler();
            }
            try {
                addPattern(compiler.compile(glob));
            } catch (MalformedPatternException ex) {
                addName(glob);
            }
        } else {
            addName(glob);
        }
    }
    
    private void addPattern(Pattern pattern) {
        if (patterns == null) {
            patterns = new ArrayList<Pattern>();
        }
        patterns.add(pattern);
    }
    
    private void addName(String name) {
        if (names == null) {
            names = new HashSet<String>();
        }
        names.add(name);
    }

    public boolean matches(String filename) {
        if (names != null && names.contains(filename)) {
            return true;
        }
        if (patterns != null) {
            Perl5Matcher matcher = new Perl5Matcher();
            for (Pattern pattern : patterns) {
                if (matcher.matches(filename, pattern)) {
                    return true;
                }
            }
        }
        return false;
    }
}
