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
package org.apache.art.mailarchive;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.repository.Repository;

/**
 * @plexus.component
 */
public class DefaultMailingListArchive implements MailingListArchive {
    /**
     * @plexus.requirement
     */
    private WagonManager wagonManager;

    private static String getMailArchiveForList(String address) {
        int idx = address.indexOf('@');
        return "http://" + address.substring(idx+1) + "/mail/" + address.substring(0, idx) + "/";
    }
    
    public void retrieveMessages(String mailingList, YearMonth month, MimeMessageProcessor processor) throws MailingListArchiveException {
        Repository repo = new Repository(null, getMailArchiveForList(mailingList));
        Session session = Session.getDefaultInstance(new Properties());
        try {
            Wagon wagon = wagonManager.getWagon(repo);
            wagon.connect(repo, wagonManager.getProxy(repo.getProtocol()));
            try {
                File mbox = File.createTempFile(mailingList, ".mbox");
                try {
                    wagon.get(month.toSimpleFormat(), mbox);
                    Store store = session.getStore(new URLName("mstor:" + mbox));
                    store.connect();
                    try {
                        Folder folder = store.getDefaultFolder();
                        folder.open(Folder.READ_ONLY);
                        for (Message msg : folder.getMessages()) {
                            processor.processMessage((MimeMessage)msg);
                        }
                    } finally {
                        store.close();
                    }
                } finally {
                    mbox.delete();
                }
            } finally {
                wagon.disconnect();
            }
        } catch (WagonException ex) {
            throw new MailingListArchiveException("Wagon exception: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new MailingListArchiveException("Unexpected I/O exception: " + ex.getMessage(), ex);
        } catch (MessagingException ex) {
            throw new MailingListArchiveException("JavaMail exception: " + ex.getMessage(), ex);
        }
    }
}
