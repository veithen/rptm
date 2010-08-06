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

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.google.code.rptm.release.config.Distribution;
import com.google.code.rptm.release.config.DistributionType;
import com.google.code.rptm.release.config.ReleaseConfig;

public abstract class AbstractSourceDistValidationMojo extends AbstractMojo {
    /**
     * @component
     */
    private ArtifactFactory factory;
    
    /**
     * @component
     */
    private ArtifactResolver resolver;
    
    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private List remoteRepositories;
    
    /**
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private ArtifactRepository localRepository;
    
    /**
     * @parameter expression="${project.groupId}"
     * @readonly
     * @required
     */
    private String groupId;
    
    /**
     * @parameter expression="${project.version}"
     * @readonly
     * @required
     */
    private String version;
    
    /**
     * @parameter expression="${releaseConfig}" default-value="etc/release-config.xml"
     * @required
     */
    private File releaseConfigFile;

    public void execute() throws MojoExecutionException, MojoFailureException {
        ReleaseConfig releaseConfig = ReleaseConfigUtil.loadReleaseConfig(releaseConfigFile);
        List<Distribution> distributions = releaseConfig.getDistributions();
        if (distributions.isEmpty()) {
            throw new MojoFailureException("No source distributions declared in release-config file");
        }
        for (Distribution dist : distributions) {
            if (dist.getDistributionType().equals(DistributionType.SOURCE)) {
                Artifact artifact = factory.createArtifactWithClassifier(groupId, dist.getArtifactId(), version, dist.getType(), dist.getClassifier());
                try {
                    // TODO: no need to look in remote repositories here
                    // TODO: first attempt to locate the artifact in the relevant "target" folder
                    resolver.resolve(artifact, remoteRepositories, localRepository);
                } catch (AbstractArtifactResolutionException ex) {
                    throw new MojoExecutionException(ex.getMessage(), ex);
                }
                File file = artifact.getFile();
                if (file == null) {
                    throw new MojoFailureException("Artifact " + dist.getArtifactId() + " has no file");
                }
                validate(file);
            }
        }
    }
    
    protected abstract void validate(File file) throws MojoExecutionException, MojoFailureException;
}
