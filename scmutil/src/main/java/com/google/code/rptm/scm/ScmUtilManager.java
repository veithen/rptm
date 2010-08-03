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
package com.google.code.rptm.scm;

import java.io.File;

/**
 * Manages {@link ScmUtil} implementations.
 */
public interface ScmUtilManager {
    /**
     * Get the {@link ScmUtil} implementation for a given working copy. This method attempts to
     * detect the SCM system from which the working copy has been checked out and returns the
     * appropriate implementation.
     * 
     * @param workingCopy
     *            The working copy. This must be a directory, but not necessarily the root directory
     *            of the checkout.
     * @return the {@link ScmUtil} implementation selected based on the detected SCM system, or
     *         <code>null</code> if the SCM system could not be detected or if the given directory
     *         is not a working copy
     */
    ScmUtil getScmUtil(File workingCopy);
}
