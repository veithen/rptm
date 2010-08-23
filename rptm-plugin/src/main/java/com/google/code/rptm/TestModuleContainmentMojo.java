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
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

/**
 * @goal test-module-containment
 * @aggregator true
 */
public class TestModuleContainmentMojo extends AbstractMojo {
    /**
     * @parameter expression="${project.collectedProjects}"
     * @required
     * @readonly
     */
    private List<MavenProject> collectedProjects;
    
    /**
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File basedir;
    
    /**
     * @component
     */
    private Invoker invoker;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            InvocationRequest request = new DefaultInvocationRequest();
            request.setBaseDirectory(basedir);
            request.setGoals(Arrays.asList("clean"));
            invoker.execute(request);
            for (MavenProject module : collectedProjects) {
                request = new DefaultInvocationRequest();
                request.setBaseDirectory(module.getBasedir());
                request.setGoals(Arrays.asList("install", "clean"));
                InvocationResult result = invoker.execute(request);
                if (result.getExitCode() != 0) {
                    throw new MojoFailureException("Module " + module.getArtifact() + " is not self-contained!");
                }
            }
        } catch (MavenInvocationException ex) {
            throw new MojoExecutionException("Failed to invoke Maven: " + ex.getMessage(), ex);
        }
    }
}
