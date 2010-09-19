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
package com.googlecode.rptm.vote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.components.interactivity.PrompterException;

import com.google.code.rptm.mailarchive.MailingListArchive;
import com.google.code.rptm.mailarchive.MailingListArchiveException;
import com.google.code.rptm.mailarchive.MimeMessageProcessor;
import com.google.code.rptm.metadata.MetadataException;
import com.google.code.rptm.metadata.MetadataProvider;
import com.google.code.rptm.metadata.pmc.PmcMember;
import com.google.code.rptm.metadata.pmc.PmcUtil;
import com.google.code.rptm.metadata.pmc.ProjectByIdMatcher;

/**
 * @goal vote-count
 * @aggregator true
 */
public class CountMojo extends AbstractVoteMojo {
    private static final Pattern footerStartPattern = Pattern.compile("(-{4,}.*|[-=]{2,}\\s*)");
    private static final List<String> countOptions = Arrays.asList("+1", "0", "-1", "i", "s");
    
    /**
     * @component
     */
    private MailingListArchive mailingListArchive;
    
    /**
     * @component
     */
    private MetadataProvider metadataProvider;
    
    /**
     * @component
     */
    private Prompter prompter;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        final VoteThread thread = loadState();
        String projectId = PmcUtil.getProjectIdFromMailingList(thread.getMailingList());
        if (projectId == null) {
            throw new MojoFailureException("Unable to determine ASF project ID from mailing list address " + thread.getMailingList());
        }
        final List<PmcMember> members;
        try {
            members = metadataProvider.getPmcMembers(new ProjectByIdMatcher(projectId));
        } catch (MetadataException ex) {
            throw new MojoExecutionException("Failed to get PMC info: " + ex.getMessage(), ex);
        }
        final Aliases aliases = new Aliases();
        try {
            metadataProvider.getMailAliases(aliases);
        } catch (MetadataException ex) {
            throw new MojoExecutionException("Failed to load mail aliases: " + ex.getMessage(), ex);
        }
        try {
            mailingListArchive.retrieveMessages(thread.getMailingList(), thread.getMonth(),
                    new MimeMessageProcessor() {
                        public boolean processMessage(MimeMessage msg) throws MessagingException, IOException {
                            return CountMojo.this.processMessage(thread, members, aliases, msg);
                        }
                    }, new LoggingMailingListArchiveEventListener(getLog()));
        } catch (MailingListArchiveException ex) {
            throw new MojoExecutionException("Failed to crawl mailing list archive: " + ex.getMessage(), ex);
        }
        persistState(thread);
    }
    
    private boolean processMessage(VoteThread thread, List<PmcMember> members, Aliases aliases, MimeMessage msg) throws MessagingException, IOException {
        try {
            String messageId = msg.getMessageID();
            if (messageId.startsWith("<") && messageId.endsWith(">")) {
                messageId = messageId.substring(1, messageId.length()-1);
            }
            if (messageBelongsToThread(msg, thread) && !isCounted(thread, messageId)) {
                InternetAddress from = (InternetAddress)msg.getFrom()[0];
                String fromAddress = from.getAddress().toLowerCase();
                displayMessageContent(msg, new PrintWriter(new OutputStreamWriter(System.out), true));
                Voter voter = getVoter(thread, fromAddress);
                boolean newVoter;
                if (voter != null) {
                    newVoter = false;
                } else {
                    voter = new Voter();
                    String primaryAddress = aliases.getPrimaryAddress(fromAddress);
                    List<String> addresses = voter.getAddresses();
                    if (primaryAddress != null) {
                        addresses.add(primaryAddress);
                        addresses.addAll(aliases.getAliases(primaryAddress));
                    } else {
                        addresses.add(fromAddress);
                    }
                    PmcMember member = null;
                    for (PmcMember candidate : members) {
                        if (addresses.contains(candidate.getAddress().toLowerCase())) {
                            member = candidate;
                            break;
                        }
                    }
                    if (member != null) {
                        voter.setName(member.getName());
                        voter.setPmcMember(true);
                    } else {
                        voter.setName(from.getPersonal());
                        voter.setPmcMember(false);
                    }
                    newVoter = true;
                }
                System.out.println("Voter: " + voter.getName() + " " + voter.getAddresses() + ", PMC member: " + voter.isPmcMember());
                String choice = prompter.prompt("Count this vote as", countOptions);
                if (choice.equals("i")) {
                    thread.getIgnoredMessageIds().add(messageId);
                } else if (choice.equals("s")) {
                    return false;
                } else {
                    if (newVoter) {
                        thread.getVoters().add(voter);
                    }
                    Vote vote = new Vote();
                    vote.setMessageId(messageId);
                    // getReceivedDate would be more accurate here, but there is a bug in mstor
                    // that causes invocations of that method to fail
                    vote.setReceived(msg.getSentDate());
                    vote.setOpinion(choice);
                    voter.getVotes().add(vote);
                }
            }
            return true;
        } catch (PrompterException ex) {
            return false;
        }
    }

    private static boolean messageBelongsToThread(MimeMessage msg, VoteThread thread) throws MessagingException {
        String messageId = "<" + thread.getMessageId() + ">";
        return messageId.equals(msg.getMessageID())
                || references(msg, messageId, "In-Reply-To")
                || references(msg, messageId, "References");
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

    private static boolean isCounted(VoteThread thread, String messageId) {
        for (Voter voter : thread.getVoters()) {
            for (Vote vote : voter.getVotes()) {
                if (vote.getMessageId().equals(messageId)) {
                    return true;
                }
            }
        }
        for (String ignored : thread.getIgnoredMessageIds()) {
            if (ignored.equals(messageId)) {
                return true;
            }
        }
        return false;
    }
    
    private static void printSeparator(PrintWriter out) {
        for (int i=0; i<80; i++) {
            out.print('#');
        }
        out.println();
    }
    
    private static boolean displayMessageContent(Part msg, PrintWriter out) throws IOException, MessagingException {
        Object content = msg.getContent();
        if (content instanceof String) {
            printSeparator(out);
            BufferedReader reader = new BufferedReader(new StringReader((String)content));
            String line;
            boolean inQuotedText = false;
            boolean outputEmptyLine = false;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(">")) {
                    if (!inQuotedText) {
                        out.println("[** Quoted text not shown **]");
                        inQuotedText = true;
                    }
                } else if (footerStartPattern.matcher(line).matches()) {
                    break;
                } else if (line.length() == 0) {
                    outputEmptyLine = true;
                } else {
                    if (outputEmptyLine) {
                        out.println();
                        outputEmptyLine = false;
                    }
                    inQuotedText = false;
                    out.println(line);
                }
            }
            printSeparator(out);
            return true;
        } else if (content instanceof MimeMultipart) {
            MimeMultipart mp = (MimeMultipart)content;
            for (int i=0; i<mp.getCount(); i++) {
                if (displayMessageContent(mp.getBodyPart(i), out)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }
    
    private static Voter getVoter(VoteThread thread, String address) {
        for (Voter voter : thread.getVoters()) {
            if (voter.getAddresses().contains(address)) {
                return voter;
            }
        }
        return null;
    }
}
