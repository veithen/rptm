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

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.google.code.rptm.metadata.MetadataException;
import com.google.code.rptm.metadata.MetadataProvider;
import com.google.code.rptm.metadata.pmc.PmcMember;
import com.google.code.rptm.metadata.pmc.PmcUtil;
import com.google.code.rptm.metadata.pmc.ProjectByIdMatcher;

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
    private MetadataProvider metadataProvider;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        String projectId = PmcUtil.getProjectIdFromSiteUrl(projectUrl);
        if (projectId == null) {
            throw new MojoFailureException("Unable to determine ASF project ID from URL " + projectUrl);
        }
        
        List<PmcMember> members;
        try {
            members = metadataProvider.getPmcMembers(new ProjectByIdMatcher(projectId));
        } catch (MetadataException ex) {
            throw new MojoExecutionException("Failed to get PMC info: " + ex.getMessage(), ex);
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
