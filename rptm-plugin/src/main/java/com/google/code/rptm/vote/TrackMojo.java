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
package com.google.code.rptm.vote;

import java.util.List;

import org.apache.maven.model.MailingList;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.google.code.rptm.mailarchive.MailingListArchive;
import com.google.code.rptm.mailarchive.MailingListArchiveException;
import com.google.code.rptm.mailarchive.YearMonth;

/**
 * @goal vote-track
 */
public class TrackMojo extends AbstractVoteMojo {
    /**
     * @parameter expression="${messageId}"
     * @required
     */
    private String messageId;
    
    /**
     * @parameter expression="${mailingList}"
     */
    private String mailingList;
    
    /**
     * @parameter expression="${project.mailingLists}"
     * @readonly
     */
    private List<MailingList> mailingLists;
    
    /**
     * @component
     */
    private MailingListArchive mailingListArchive;

    public void execute() throws MojoExecutionException, MojoFailureException {
        VoteThread thread = new VoteThread();
        thread.setMessageId(messageId);
        if (mailingList == null) {
            for (MailingList list : mailingLists) {
                String address = list.getPost();
                if (address.contains("dev")) {
                    mailingList = address;
                    break;
                }
            }
        }
        if (mailingList == null) {
            throw new MojoFailureException("Unable to identify mailing list; please specify one");
        }
        thread.setMailingList(mailingList);
        MessageIdMatcher matcher = new MessageIdMatcher(messageId);
        LoggingMailingListArchiveEventListener listener = new LoggingMailingListArchiveEventListener(getLog());
        YearMonth month = new YearMonth();
        try {
            while (true) {
                mailingListArchive.retrieveMessages(mailingList, month, matcher, listener);
                if (matcher.isFound()) {
                    break;
                } else {
                    month = month.previous();
                }
            }
            getLog().info("Message found; creating vote file");
            thread.setMonth(month);
            persistState(thread);
        } catch (MailingListArchiveException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
    }
}
