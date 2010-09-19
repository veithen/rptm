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

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.google.code.rptm.mailarchive.MimeMessageProcessor;

public class ConversationFilter implements MimeMessageProcessor {
    private final MimeMessageProcessor parent;
    private final String rootMessageId;
    
    public ConversationFilter(MimeMessageProcessor parent, String rootMessageId) {
        this.parent = parent;
        this.rootMessageId = rootMessageId;
    }

    public boolean processMessage(MimeMessage msg) throws MessagingException, IOException {
        if (msg.getMessageID().equals("<" + rootMessageId + ">")
                || references(msg, "In-Reply-To")
                || references(msg, "References")) {
            return parent.processMessage(msg);
        } else {
            return true;
        }
    }
    
    private boolean references(MimeMessage msg, String header) throws MessagingException {
        String[] values = msg.getHeader(header);
        if (values == null) {
            return false;
        }
        for (String value : values) {
            int messageIdStart = -1;
            for (int i=0; i<value.length(); i++) {
                char c = value.charAt(i);
                if (messageIdStart == -1 && c == '<') {
                    messageIdStart = i+1;
                } else if (messageIdStart != -1 && c == '>') {
                    String messageId = value.substring(messageIdStart, i);
                    if (messageId.equals(rootMessageId)) {
                        return true;
                    }
                    messageIdStart = -1;
                } else if (messageIdStart == -1 && c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                    return false;
                }
            }
        }
        return false;
    }
}
