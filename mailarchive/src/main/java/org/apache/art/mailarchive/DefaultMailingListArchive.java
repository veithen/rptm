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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;

/**
 * @plexus.component
 */
public class DefaultMailingListArchive implements MailingListArchive, Disposable {
    private static final byte[] MBOX_MAGIC = { 'F', 'r', 'o', 'm', ' ' };
    
    /**
     * @plexus.requirement
     */
    private WagonManager wagonManager;
    
    private final Map<MboxKey,File> cache = new HashMap<MboxKey,File>();

    private static String getMailArchiveForList(String address) {
        int idx = address.indexOf('@');
        return "http://" + address.substring(idx+1) + "/mail/" + address.substring(0, idx) + "/";
    }

    private static boolean isMboxFile(File file) throws IOException {
        byte[] firstBytes = new byte[5];
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        try {
            in.readFully(firstBytes);
        } finally {
            in.close();
        }
        return Arrays.equals(firstBytes, MBOX_MAGIC);
    }
    
    private File getMboxFile(String mailingList, YearMonth month, MailingListArchiveEventListener eventListener) throws MailingListArchiveException {
        MboxKey mboxKey = new MboxKey(mailingList, month);
        File mbox = cache.get(mboxKey);
        if (mbox != null) {
            return mbox;
        }
        Repository repo = new Repository(null, getMailArchiveForList(mailingList));
        try {
            Set<File> tempFiles = new HashSet<File>();
            Wagon wagon = wagonManager.getWagon(repo);
            wagon.connect(repo, wagonManager.getProxy(repo.getProtocol()));
            try {
                mbox = File.createTempFile(mailingList, ".mbox");
                tempFiles.add(mbox);
                wagon.get(month.toSimpleFormat(), mbox);
                eventListener.mboxLoaded(mailingList, month);
                if (!isMboxFile(mbox)) {
                    File compressedMbox = mbox;
                    mbox = File.createTempFile(mailingList, ".mbox");
                    tempFiles.add(mbox);
                    InputStream in = new FileInputStream(compressedMbox);
                    try {
                        OutputStream out = new FileOutputStream(mbox);
                        try {
                            IOUtils.copy(new GZIPInputStream(in), out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        in.close();
                    }
                }
                cache.put(mboxKey, mbox);
                tempFiles.remove(mbox);
                return mbox;
            } finally {
                wagon.disconnect();
                for (File tempFile : tempFiles) {
                    tempFile.delete();
                }
            }
        } catch (WagonException ex) {
            throw new MailingListArchiveException("Wagon exception: " + ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new MailingListArchiveException("Unexpected I/O exception: " + ex.getMessage(), ex);
        }
    }

    public void retrieveMessages(String mailingList, YearMonth month, MimeMessageProcessor processor, MailingListArchiveEventListener eventListener) throws MailingListArchiveException {
        Session session = Session.getDefaultInstance(new Properties());
        try {
            Store store = session.getStore(new URLName("mstor:" + getMboxFile(mailingList, month, eventListener)));
            store.connect();
            try {
                Folder folder = store.getDefaultFolder();
                folder.open(Folder.READ_ONLY);
                for (Message msg : folder.getMessages()) {
                    if (!processor.processMessage((MimeMessage)msg)) {
                        break;
                    }
                }
            } finally {
                store.close();
            }
        } catch (MessagingException ex) {
            throw new MailingListArchiveException("JavaMail exception: " + ex.getMessage(), ex);
        }
    }
    
    public void dispose() {
        for (File file : cache.values()) {
            file.delete();
        }
        cache.clear();
    }
}
