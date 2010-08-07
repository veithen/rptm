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
package com.google.code.rptm.scm;

/**
 * Contains information about a given folder in a working copy.
 * <p>
 * NOTE: Currently, the properties of this class are too specific to SVN. This will change when we
 * support CVS.
 */
public class ScmInfo {
    private final String url;
    private final String repoRoot;
    private final String repoUUID;
    
    public ScmInfo(String url, String repoRoot, String repoUUID) {
        this.url = url;
        this.repoRoot = repoRoot;
        this.repoUUID = repoUUID;
    }

    public String getUrl() {
        return url;
    }

    public String getRepoRoot() {
        return repoRoot;
    }

    public String getRepoUUID() {
        return repoUUID;
    }
}
