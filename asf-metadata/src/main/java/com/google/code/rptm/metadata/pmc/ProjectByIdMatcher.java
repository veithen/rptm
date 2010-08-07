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

public class ProjectByIdMatcher implements ProjectMatcher {
    private final String id;
    
    public ProjectByIdMatcher(String id) {
        this.id = id;
    }

    public MatchLevel matches(String name) {
        if (name.replaceAll("\\s", "").equalsIgnoreCase(id)) {
            return MatchLevel.EXACT_MATCH;
        } else {
            String[] parts = name.split("\\s+");
            if (parts.length == id.length()) {
                for (int i=0; i<parts.length; i++) {
                    if (Character.toLowerCase(parts[i].charAt(0)) != id.charAt(i)) {
                        return MatchLevel.NO_MATCH;
                    }
                }
                return MatchLevel.FUZZY_MATCH;
            } else {
                return MatchLevel.NO_MATCH;
            }
        }
    }
}
