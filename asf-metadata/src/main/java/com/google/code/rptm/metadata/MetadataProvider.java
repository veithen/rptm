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

import java.util.List;

import com.google.code.rptm.metadata.aliases.MailAliasVisitor;
import com.google.code.rptm.metadata.pmc.CommitteeInfoVisitor;
import com.google.code.rptm.metadata.pmc.PmcMember;
import com.google.code.rptm.metadata.pmc.ProjectMatcher;

public interface MetadataProvider {
    void getCommitteeInfo(CommitteeInfoVisitor visitor) throws MetadataException;
    List<PmcMember> getPmcMembers(ProjectMatcher matcher) throws MetadataException;
    void getMailAliases(MailAliasVisitor visitor) throws MetadataException;
}
