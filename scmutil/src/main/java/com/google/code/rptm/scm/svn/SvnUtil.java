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
package com.google.code.rptm.scm.svn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.code.rptm.scm.ScmException;
import com.google.code.rptm.scm.ScmInfo;
import com.google.code.rptm.scm.ScmUtil;

/**
 * {@link ScmUtil} implementation for Subversion.
 * <p>
 * Note: that we are not using SVNKit here because the license of this framework is incompatible
 * with Apache.
 * 
 * @plexus.component role="com.google.code.rptm.scm.ScmUtil" role-hint="svn"
 */
public class SvnUtil implements ScmUtil {
    private File getSvnDir(File dir) {
        return new File(dir, ".svn");
    }
    
    public boolean isWorkingCopy(File dir) {
        return getSvnDir(dir).isDirectory();
    }

    public ScmInfo getInfo(File dir) throws ScmException {
        String url = null;
        String repoRoot = null;
        String repoUUID = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(getSvnDir(dir), "entries")), "UTF-8"));
            try {
                int i = 0;
                loop: while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        throw new ScmException("Unexpected end of file while reading SVN metadata");
                    }
                    switch (i++) {
                        case 4: url = line; break;
                        case 5: repoRoot = line; break;
                        case 26: repoUUID = line; break loop;
                    }
                }
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            throw new ScmException("Failed to read SVN metadata", ex);
        }
        return new ScmInfo(url, repoRoot, repoUUID);
    }
}
