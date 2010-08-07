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
package com.google.code.rptm.metadata;

import java.util.ArrayList;
import java.util.List;

import com.google.code.rptm.metadata.pmc.CommitteeInfoVisitor;
import com.google.code.rptm.metadata.pmc.MatchLevel;
import com.google.code.rptm.metadata.pmc.PmcMember;
import com.google.code.rptm.metadata.pmc.ProjectMatcher;

class VisitorImpl implements CommitteeInfoVisitor {
    private final ProjectMatcher matcher;
    private final List<PmcMember> members = new ArrayList<PmcMember>();
    private boolean matches;
    private MatchLevel currentMatchLevel = MatchLevel.NO_MATCH;
    private boolean ambiguous;

    public VisitorImpl(ProjectMatcher matcher) {
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