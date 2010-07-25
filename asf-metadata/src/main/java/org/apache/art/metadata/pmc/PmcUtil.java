/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.art.metadata.pmc;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class PmcUtil {
    private static class Visitor implements CommitteeInfoVisitor {
        private final ProjectMatcher matcher;
        private final List<PmcMember> members = new ArrayList<PmcMember>();
        private boolean matches;
        private MatchLevel currentMatchLevel = MatchLevel.NO_MATCH;
        private boolean ambiguous;

        public Visitor(ProjectMatcher matcher) {
            this.matcher = matcher;
        }

        public void startProject(String name) {
            MatchLevel matchLevel = matcher.matches(name);
            if (matchLevel != MatchLevel.NO_MATCH) {
                int c = currentMatchLevel.compareTo(matchLevel);
                if (c < 0) {
                    currentMatchLevel = matchLevel;
                    members.clear();
                    ambiguous = false;
                    matches = true;
                } else if (c == 0) {
                    members.clear();
                    ambiguous = true;
                }
            }
        }
        
        public void endProject() {
            matches = false;
        }

        public void member(String name, String address) {
            if (matches) {
                members.add(new PmcMember(name, address));
            }
        }
        
        public List<PmcMember> getMembers() {
            return ambiguous || members.isEmpty() ? null : members;
        }
    }
    
    private PmcUtil() {}
    
    public static String getProjectIdFromSiteUrl(String siteUrl) {
        String host;
        try {
            host = new URL(siteUrl).getHost();
        } catch (MalformedURLException ex) {
            return null;
        }
        int idx = host.indexOf('.');
        if (host.substring(idx+1).equals("apache.org")) {
            return host.substring(0, idx);
        } else {
            return null;
        }
    }
    
    public static List<PmcMember> getPmcMembers(InputStream in, ProjectMatcher matcher) throws IOException {
        Visitor visitor = new Visitor(matcher);
        CommitteeInfoParser.parse(in, visitor);
        return visitor.getMembers();
    }
}
