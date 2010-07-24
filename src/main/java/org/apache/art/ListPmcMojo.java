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
package org.apache.art;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.art.metadata.pmc.PmcMember;
import org.apache.art.metadata.pmc.PmcUtil;
import org.apache.art.metadata.pmc.ProjectByIdMatcher;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.repository.Repository;

/**
 * @goal list-pmc
 */
public class ListPmcMojo extends AbstractMojo {
    /**
     * @parameter expression="${project.url}"
     * @readonly
     */
    private String projectUrl;
    
    /**
     * @component
     */
    private WagonManager wagonManager;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        String projectId = PmcUtil.getProjectIdFromSiteUrl(projectUrl);
        if (projectId == null) {
            throw new MojoFailureException("Unable to determine ASF project ID from URL " + projectUrl);
        }
        
        List<PmcMember> members;
        try {
            Repository repo = new Repository("asf-private", "dav:https://svn.apache.org/repos/private/");
            Wagon wagon = wagonManager.getWagon(repo);
            wagon.connect(repo, wagonManager.getAuthenticationInfo(repo.getId()), wagonManager.getProxy(repo.getProtocol()));
            try {
                File committeeInfo = File.createTempFile("committee-info", ".txt");
                try {
                    wagon.get("committers/board/committee-info.txt", committeeInfo);
                    InputStream in = new FileInputStream(committeeInfo);
                    try {
                        members = PmcUtil.getPmcMembers(in, new ProjectByIdMatcher(projectId));
                    } finally {
                        in.close();
                    }
                } finally {
                    committeeInfo.delete();
                }
            } finally {
                wagon.disconnect();
            }
        } catch (WagonException ex) {
            throw new MojoExecutionException("Failed to get PMC info from asf-private repository: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new MojoExecutionException("Unexpected I/O exception: " + ex.getMessage(), ex);
        }
        
        if (members == null) {
            throw new MojoFailureException("Unable to find project in committee-info.txt");
        } else {
            Log log = getLog();
            log.info("List of PMC members:");
            for (PmcMember member : members) {
                log.info("  " + member.getName() + " <" + member.getAddress() + ">");
            }
        }
    }
}
