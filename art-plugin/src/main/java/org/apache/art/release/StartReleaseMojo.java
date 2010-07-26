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
package org.apache.art.release;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal start-release
 * @aggregator true
 */
public class StartReleaseMojo extends AbstractReleaseMojo {
    /**
     * @parameter expression="${project.version}"
     */
    private String version;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        // TODO: we need to interactively ask for the release version!
        if (version.equals("SNAPSHOT")) {
            throw new MojoFailureException("Unexpected version " + version + ": please branch first!");
        } else if (version.endsWith("-SNAPSHOT")) {
            ReleaseInfo releaseInfo = new ReleaseInfo();
            releaseInfo.setVersion(version.substring(0, version.length()-9));
            persistState(releaseInfo);
        } else {
            throw new MojoFailureException("Unexpected version " + version + ": already released?!?");
        }
    }
}
