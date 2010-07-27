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
package com.google.code.rptm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal validate-pom
 */
public class ValidatePomMojo extends AbstractMojo {
    /**
     * @parameter expression="${project.scm.connection}"
     * @readonly
     */
    private String scmConnection;

    /**
     * @parameter expression="${project.scm.developerConnection}"
     * @readonly
     */
    private String scmDeveloperConnection;
    
    /**
     * @parameter expression="${project.scm.url}"
     * @readonly
     */
    private String scmUrl;
    
    /**
     * @parameter expression="${basedir}"
     * @readonly
     */
    private File basedir;
    
    /**
     * @parameter expression="${scmConnectionRoot}" default-value="scm:svn:http://svn.apache.org/repos/asf/"
     */
    private String scmConnectionRoot;
    
    /**
     * @parameter expression="${scmDeveloperConnectionRoot}" default-value="scm:svn:https://svn.apache.org/repos/asf/"
     */
    private String scmDeveloperConnectionRoot;
    
    /**
     * @parameter expression="${scmUrlRoot}" default-value="http://svn.apache.org/viewvc/"
     */
    private String scmUrlRoot;
    
    /**
     * @parameter expression="${repositoryUUID}" default-value="13f79535-47bb-0310-9956-ffa450edef68"
     */
    private String repositoryUUID;
    
    private static String mergePaths(String root, String path) {
        StringBuffer buffer = new StringBuffer();
        if (root.charAt(root.length()-1) == '/') {
            buffer.append(root.substring(0, root.length()-1));
        } else {
            buffer.append(root);
        }
        if (path.charAt(0) != '/') {
            buffer.append('/');
        }
        buffer.append(path);
        return buffer.toString();
    }
    
    private static void validate(String expectedRoot, String path, String actual, String property) throws MojoFailureException{
        String expected = mergePaths(expectedRoot, path);
        if (!actual.equals(expected)) {
            throw new MojoFailureException("Invalid POM metadata for " + property + ": found \"" + actual + "\", but expected \"" + expected + "\"");
        }
    }
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        File svndir = new File(basedir, ".svn");
        if (!svndir.exists()) {
            getLog().info("Skipping: not an SVN working copy");
            return;
        }
        String wcUrl = null;
        String wcRepoRoot = null;
        String wcRepoUUID = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(svndir, "entries")), "UTF-8"));
            try {
                int i = 0;
                loop: while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        throw new MojoExecutionException("Unexpected end of file while reading SVN metadata");
                    }
                    switch (i++) {
                        case 4: wcUrl = line; break;
                        case 5: wcRepoRoot = line; break;
                        case 26: wcRepoUUID = line; break loop;
                    }
                }
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Failed to read SVN metadata", ex);
        }
        if (!wcUrl.startsWith(wcRepoRoot)) {
            throw new MojoExecutionException("Unexpected error: mismatch between wcUrl and wcRepoRoot; please report this to the dev team");
        }
        String path = wcUrl.substring(wcRepoRoot.length());
        if (!wcRepoUUID.equals(repositoryUUID)) {
            getLog().info("Skipping: working copy is not a checkout from repository " + repositoryUUID);
            return;
        }
        validate(scmConnectionRoot, path, scmConnection, "scm.connection");
        validate(scmDeveloperConnectionRoot, path, scmDeveloperConnection, "scm.developerConnection");
        validate(scmUrlRoot, path, scmUrl, "scm.url");
    }
}
