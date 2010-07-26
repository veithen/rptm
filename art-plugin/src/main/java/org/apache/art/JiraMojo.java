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

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.art.jira.JiraSoapService;
import org.apache.art.jira.JiraSoapServiceServiceLocator;
import org.apache.art.jira.RemoteProject;
import org.apache.axis.AxisFault;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

/**
 * @goal jira
 * @aggregator true
 */
public class JiraMojo extends AbstractMojo {
    /**
     * @parameter expression="${project.issueManagement}"
     * @readonly
     */
    private IssueManagement issueManagement;
    
    /**
     * @component
     */
    private WagonManager wagonManager;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!issueManagement.getSystem().equals("JIRA")) {
            throw new MojoFailureException("Only JIRA is supported at this moment");
        }
        Matcher matcher = Pattern.compile("(.+/)browse/([A-Z0-9]+)").matcher(issueManagement.getUrl());
        if (!matcher.matches()) {
            throw new MojoFailureException("Unrecognized JIRA URL");
        }
        String jiraUrlString = matcher.group(1);
        String projectKey = matcher.group(2);
        URL jiraUrl;
        try {
            jiraUrl = new URL(jiraUrlString);
        } catch (MalformedURLException ex) {
            throw new MojoFailureException("Malformed URL: " + jiraUrlString);
        }
        if (jiraUrl.getProtocol().equals("http")) {
            try {
                jiraUrl = new URL("https", jiraUrl.getHost(), jiraUrl.getFile());
            } catch (MalformedURLException ex) {
                // Really don't know what could go wrong here...
                throw new MojoExecutionException("Unexpected exception", ex);
            }
        }
        String serverId = jiraUrl.getHost();
        AuthenticationInfo authInfo = wagonManager.getAuthenticationInfo(serverId);
        if (authInfo == null) {
            throw new MojoFailureException("No authentication info found. Please add a server section with ID " + serverId + " and your JIRA credentials to your settings.xml file");
        }
        getLog().info("Using " + jiraUrl + " to connect to JIRA");
        JiraSoapService service;
        try {
            service = new JiraSoapServiceServiceLocator().getJirasoapserviceV2(new URL(jiraUrl, "rpc/soap/jirasoapservice-v2"));
        } catch (Exception ex) {
            throw new MojoExecutionException("Unable to create SOAP service proxy", ex);
        }
        try {
            String token = service.login(authInfo.getUserName(), authInfo.getPassword());
            RemoteProject project = service.getProjectByKey(token, projectKey);
            System.out.println("Found project: " + project.getName());
            service.logout(token);
        } catch (AxisFault fault) {
            throw new MojoFailureException(fault.getFaultString());
        } catch (RemoteException ex) {
            throw new MojoExecutionException("Remote exception", ex);
        }
    }
}
