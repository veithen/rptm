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
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.components.io.resources.PlexusIoArchivedResourceCollection;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;

import com.google.code.rptm.scm.ScmException;
import com.google.code.rptm.scm.ScmUtil;
import com.google.code.rptm.scm.ScmUtilManager;

/**
 * @goal check-source-dist
 * @aggregator true
 */
public class CheckSourceDistMojo extends AbstractSourceDistValidationMojo {
    /**
     * @parameter expression="${basedir}"
     * @readonly
     * @required
     */
    private File basedir;
    
    /**
     * @component
     */
    private ScmUtilManager scmUtilManager;
    
    /**
     * @component
     */
    private ArchiverManager archiverManager;

    protected void validate(File file) throws MojoExecutionException, MojoFailureException {
        ScmUtil scmUtil = scmUtilManager.getScmUtil(basedir);
        if (scmUtil == null) {
            throw new MojoFailureException("Not a working copy");
        }
        PlexusIoArchivedResourceCollection rc;
        try {
            rc = (PlexusIoArchivedResourceCollection)archiverManager.getResourceCollection(file);
        } catch (NoSuchArchiverException ex) {
            throw new MojoExecutionException("No archiver found for " + file);
        }
        rc.setFile(file);
        NamedNode root = new NamedNode(null);
        try {
            for (Iterator it = rc.getResources(); it.hasNext(); ) {
                PlexusIoResource resource = (PlexusIoResource)it.next();
                NamedNode node = root;
                for (String component : resource.getName().split("/")) {
                    NamedNode child = node.getChild(component);
                    if (child == null) {
                        child = new NamedNode(component);
                        node.addChild(child);
                    }
                    node = child;
                }
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("Error reading " + file, ex);
        }
        if (root.getChildren().size() == 1) {
            root = root.getChildren().iterator().next();
            getLog().info("Stripping " + root.getName() + " from archive entries");
        }
        check(root, scmUtil, basedir);
    }
    
    private void check(NamedNode node, ScmUtil scmUtil, File dir) throws MojoExecutionException {
        Log log = getLog();
        Set<String> ignore;
        try {
            ignore = scmUtil.getIgnoredEntries(dir);
        } catch (ScmException ex) {
            throw new MojoExecutionException("Unable to get ignore list for " + dir);
        }
        if (log.isDebugEnabled()) {
            log.debug("=================");
            log.debug("Archive entry: " + node.getName());
            log.debug("Directory: " + dir);
            log.debug("Ignore list: " + ignore);
        }
        for (NamedNode child : node.getChildren()) {
            File file = new File(dir, child.getName());
            // TODO: this doesn't take into account globs contained in the ignore list!
            if (ignore.contains(child.getName())) {
                log.error("Source distribution contains entry that is ignored in SCM: " + file);
            } else if (file.isDirectory()) {
                check(child, scmUtil, file);
            }
        }
    }
}
