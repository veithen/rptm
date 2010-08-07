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
package com.google.code.rptm;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.util.FileUtils;

/**
 * @goal test-source-dist
 * @aggregator true
 */
public class TestSourceDistMojo extends AbstractSourceDistValidationMojo {
    /**
     * @parameter expression="${project.build.directory}/source-dist"
     * @required
     */
    private File tmpDir;
    
    /**
     * @component
     */
    private ArchiverManager archiverManager;
    
    /**
     * @component
     */
    private Invoker invoker;

    @Override
    protected void validate(File file) throws MojoExecutionException, MojoFailureException {
        UnArchiver unArchiver;
        try {
            unArchiver = archiverManager.getUnArchiver(file);
        } catch (NoSuchArchiverException ex) {
            throw new MojoExecutionException("No (un)archiver found for " + file);
        }
        if (tmpDir.exists()) {
            try {
                FileUtils.cleanDirectory(tmpDir);
            } catch (IOException ex) {
                throw new MojoExecutionException("Unable to clean directory " + tmpDir + ": " + ex.getMessage(), ex);
            }
        }
        tmpDir.mkdirs();
        unArchiver.setDestDirectory(tmpDir);
        unArchiver.setSourceFile(file);
        try {
            unArchiver.extract();
        } catch (ArchiverException ex) {
            throw new MojoExecutionException("Unable to extract " + file + ": " + ex.getMessage(), ex);
        }
        File[] children = tmpDir.listFiles();
        File mvnDir = children.length == 1 && children[0].isDirectory() ? children[0] : tmpDir;
        InvocationRequest invocationRequest = new DefaultInvocationRequest();
        invocationRequest.setBaseDirectory(mvnDir);
        invocationRequest.setGoals(Arrays.asList("clean", "install"));
        try {
            invoker.execute(invocationRequest);
        } catch (MavenInvocationException ex) {
            throw new MojoExecutionException("Failed to invoke Maven: " + ex.getMessage(), ex);
        }
    }
}
