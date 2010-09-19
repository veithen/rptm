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
package com.google.code.rptm.mailarchive;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public interface MimeMessageProcessor {
    /**
     * Process a MIME message.
     * 
     * @param msg
     *            the MIME message to process
     * @return <code>true</code> if the caller should continue to submit messages,
     *         <code>false</code> if the processor has finished its work and doesn't wish to receive
     *         further messages
     * @throws MessagingException
     *             if one of the methods in {@link MimeMessage} throws a {@link MessagingException}
     * @throws IOException 
     */
    boolean processMessage(MimeMessage msg) throws MessagingException, IOException;
}
