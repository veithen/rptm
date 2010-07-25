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
package org.apache.art.vote;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.art.mailarchive.MailingListArchive;
import org.apache.art.mailarchive.MailingListArchiveException;
import org.apache.art.mailarchive.MimeMessageProcessor;
import org.apache.art.mailarchive.YearMonth;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @goal vote-count
 */
public class CountMojo extends AbstractVoteMojo {
    /**
     * @component
     */
    private MailingListArchive mailingListArchive;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        final VoteThread thread = loadVoteThread();
        try {
            mailingListArchive.retrieveMessages(thread.getMailingList(), new YearMonth(thread.getMonth().getYear(), thread.getMonth().getMonth()),
                    new MimeMessageProcessor() {
                        public void processMessage(MimeMessage msg) throws MessagingException {
                            CountMojo.processMessage(thread, msg);
                        }
                    });
        } catch (MailingListArchiveException ex) {
            throw new MojoExecutionException("Failed to crawl mailing list archive: " + ex.getMessage(), ex);
        }
    }
    
    private static void processMessage(VoteThread thread, MimeMessage msg) throws MessagingException {
        String messageId = "<" + thread.getMessageId() + ">";
        if (messageId.equals(msg.getMessageID())
                || references(msg, messageId, "In-Reply-To")
                || references(msg, messageId, "References")) {
            System.out.println(msg.getSubject());
        }
    }
    
    private static boolean references(MimeMessage msg, String messageId, String header) throws MessagingException {
        String[] values = msg.getHeader(header);
        if (values == null) {
            return false;
        }
        for (String value : values) {
            String[] references = value.split("\\s+");
            for (String reference : references) {
                if (reference.equals(messageId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
