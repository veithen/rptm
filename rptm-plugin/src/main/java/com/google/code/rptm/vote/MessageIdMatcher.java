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
package com.google.code.rptm.vote;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.google.code.rptm.mailarchive.MimeMessageProcessor;

public class MessageIdMatcher implements MimeMessageProcessor {
    private final String messageId;
    private boolean found;

    public MessageIdMatcher(String messageId) {
        this.messageId = "<" + messageId + ">";
    }

    public boolean processMessage(MimeMessage msg) throws MessagingException {
        if (messageId.equals(msg.getMessageID())) {
            found = true;
            return false;
        } else {
            return true;
        }
    }

    public boolean isFound() {
        return found;
    }
}
