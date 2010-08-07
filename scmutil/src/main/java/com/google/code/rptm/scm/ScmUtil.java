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

import java.io.File;
import java.util.Set;

/**
 * Provides methods to retrieve information from a working copy checked out from a given SCM
 * system.
 */
public interface ScmUtil {
    /**
     * Determine if the given directory is a working copy checked out from SCM.
     * 
     * @param dir
     *            the directory to check
     * @return <code>true</code> if the directory is a working copy, <code>false</code> otherwise.
     *         In the special case where the directory is contained in a working copy, but has not
     *         been added or checked in, the return value will be <code>false</code>. E.g. this will
     *         be the case for the <tt>target</tt> directory of a Maven project.
     */
    boolean isWorkingCopy(File dir);
    
    ScmInfo getInfo(File dir) throws ScmException;
    
    /**
     * Get the ignored entries for a given directory.
     * 
     * @param dir
     * @return
     * @throws ScmException
     */
    FilenameMatcher getIgnoredEntries(File dir) throws ScmException;
}
