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
package org.apache.art.metadata.pmc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.repository.Repository;

/**
 * @plexus.component
 */
public class DefaultMetadataProvider implements MetadataProvider {
    /**
     * @plexus.requirement
     */
    private WagonManager wagonManager;

    public List<PmcMember> getPmcMembers(ProjectMatcher matcher) throws MetadataException {
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
                        return PmcUtil.getPmcMembers(in, matcher);
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
            throw new MetadataException("Failed to get PMC info from asf-private repository: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new MetadataException("Unexpected I/O exception: " + ex.getMessage(), ex);
        }
    }

}
