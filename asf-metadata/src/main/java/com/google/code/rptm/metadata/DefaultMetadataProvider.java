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
package com.google.code.rptm.metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;

import com.google.code.rptm.metadata.aliases.MailAliasVisitor;
import com.google.code.rptm.metadata.pmc.CommitteeInfoParser;
import com.google.code.rptm.metadata.pmc.CommitteeInfoVisitor;
import com.google.code.rptm.metadata.pmc.PmcMember;
import com.google.code.rptm.metadata.pmc.ProjectMatcher;

/**
 * @plexus.component
 */
public class DefaultMetadataProvider implements MetadataProvider, LogEnabled, Disposable {
    /**
     * @plexus.requirement
     */
    private WagonManager wagonManager;
    
    private Logger logger;
    
    private final Map<String,File> cache = new HashMap<String,File>();

    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    private File getFile(String name) throws MetadataException {
        File file = cache.get(name);
        if (file != null) {
            return file;
        }
        try {
            Repository repo = new Repository("asf-private", "dav:https://svn.apache.org/repos/private/");
            Wagon wagon = wagonManager.getWagon(repo);
            wagon.connect(repo, wagonManager.getAuthenticationInfo(repo.getId()), wagonManager.getProxy(repo.getProtocol()));
            try {
                try {
                    file = File.createTempFile("asf-metadata", ".txt");
                } catch (IOException ex) {
                    throw new MetadataException("Failed to create temporary file", ex);
                }
                boolean success = false;
                try {
                    logger.info("Fetching " + name);
                    wagon.get(name, file);
                    cache.put(name, file);
                    success = true;
                    return file;
                } finally {
                    if (!success) {
                        file.delete();
                    }
                }
            } finally {
                wagon.disconnect();
            }
        } catch (WagonException ex) {
            throw new MetadataException("Failed to get PMC info from asf-private repository: " + ex.getMessage(), ex);
        }
    }

    public void getCommitteeInfo(CommitteeInfoVisitor visitor) throws MetadataException {
        try {
            InputStream in = new FileInputStream(getFile("committers/board/committee-info.txt"));
            try {
                CommitteeInfoParser.parse(in, visitor);
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            throw new MetadataException("Unexpected I/O exception: " + ex.getMessage(), ex);
        }
    }

    public List<PmcMember> getPmcMembers(ProjectMatcher matcher) throws MetadataException {
        VisitorImpl visitor = new VisitorImpl(matcher);
        getCommitteeInfo(visitor);
        return visitor.getMembers();
    }

    public void getMailAliases(MailAliasVisitor visitor) throws MetadataException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(getFile("committers/MailAlias.txt")), "ASCII"));
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    int idx = line.indexOf(',');
                    visitor.visitMailAlias(line.substring(0, idx), line.substring(idx+1));
                }
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            throw new MetadataException("Unexpected I/O exception: " + ex.getMessage(), ex);
        }
    }

    public void dispose() {
        for (File file : cache.values()) {
            file.delete();
        }
        cache.clear();
    }
}
