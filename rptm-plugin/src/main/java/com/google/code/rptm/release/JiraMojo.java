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
package com.google.code.rptm.release;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.axis.AxisFault;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;

import com.google.code.rptm.jira.JiraSoapService;
import com.google.code.rptm.jira.JiraSoapServiceServiceLocator;
import com.google.code.rptm.jira.RemoteProject;
import com.google.code.rptm.jira.RemoteVersion;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * @goal jira
 * @aggregator true
 */
public class JiraMojo extends AbstractReleaseMojo {
    private static final Pattern NAME_VERSION = Pattern.compile("(.+) (\\d+(\\.\\d+)*)");
    
    /**
     * @parameter expression="${project.name}"
     * @readonly
     */
    private String projectName;
    
    /**
     * @parameter expression="${project.issueManagement}"
     * @readonly
     */
    private IssueManagement issueManagement;
    
    /**
     * @component
     */
    private WagonManager wagonManager;

    // TODO: implement proxy handling!
    public void execute() throws MojoExecutionException, MojoFailureException {
        ReleaseInfo releaseInfo = loadState();
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
        RemoteProject project;
        RemoteVersion version;
        try {
            String token = service.login(authInfo.getUserName(), authInfo.getPassword());
            project = service.getProjectByKey(token, projectKey);
            version = selectVersion(service.getVersions(token, projectKey), releaseInfo.getVersion());
            if (version == null) {
                throw new MojoExecutionException("No matching version found in JIRA");
            }
            getLog().info("Found project '" + project.getName() + "' and version '" + version.getName() + "'");
            service.logout(token);
        } catch (AxisFault fault) {
            throw new MojoFailureException(fault.getFaultString());
        } catch (RemoteException ex) {
            throw new MojoExecutionException("Remote exception", ex);
        }
        URL searchUrl;
        try {
            searchUrl = new URL(jiraUrl, "sr/jira.issueviews:searchrequest-rss/temp/SearchRequest.xml?pid="
                    + project.getId() + "&fixfor=" + version.getId()
                    + "&status=5&status=6&sorter/field=issuekey&sorter/order=ASC&os_username="
                    + authInfo.getUserName() + "&os_password=" + authInfo.getPassword());
        } catch (MalformedURLException ex) {
            throw new MojoExecutionException("Unexpected exception", ex);
        }
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed;
        try {
            feed = input.build(new XmlReader(searchUrl));
        } catch (Exception ex) {
            throw new MojoExecutionException("Failed to load issue list", ex);
        }
        List<Issue> issues = releaseInfo.getIssues();
        issues.clear();
        for (SyndEntry entry : (List<SyndEntry>)feed.getEntries()) {
            String title = entry.getTitle();
            int idx = title.indexOf(']');
            Issue issue = new Issue();
            issue.setKey(title.substring(1, idx));
            issue.setSummary(title.substring(idx+2));
            issues.add(issue);
        }
        persistState(releaseInfo);
    }
    
    private RemoteVersion selectVersion(RemoteVersion[] versions, String projectVersion) {
        for (RemoteVersion version : versions) {
            String name = version.getName();
            if (name.equals(projectVersion)) {
                return version;
            }
            Matcher m = NAME_VERSION.matcher(name);
            if (m.matches()) {
                String prefix = m.group(1);
                if (m.group(2).equals(projectVersion) &&
                        (projectName.toUpperCase().contains(prefix.toUpperCase()) || prefix.toUpperCase().contains(projectName.toUpperCase()))) {
                    return version;
                }
            }
        }
        return null;
    }
}
