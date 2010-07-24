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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.maven.model.MailingList;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

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
     * @parameter expression="${month}"
     */
    private String month;
    
    /**
     * @parameter expression="${project.mailingLists}"
     * @readonly
     */
    private List<MailingList> mailingLists;

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
        XMLGregorianCalendar monthValue = DATATYPE_FACTORY.newXMLGregorianCalendar();
        if (month == null) {
            GregorianCalendar cal = new GregorianCalendar();
            monthValue.setYear(cal.get(Calendar.YEAR));
            monthValue.setMonth(cal.get(Calendar.MONTH) + 1);
        } else {
            if (month.length() == 6) {
                monthValue.setYear(Integer.parseInt(month.substring(0, 4)));
                monthValue.setMonth(Integer.parseInt(month.substring(4, 6)));
            } else {
                throw new MojoFailureException("Invalid format for parameter 'month'. Use YYYYMM.");
            }
        }
        thread.setMonth(monthValue);
        persistVoteThread(thread);
    }
}
