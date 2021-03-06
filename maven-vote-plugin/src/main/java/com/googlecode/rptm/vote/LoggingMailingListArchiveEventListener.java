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

import org.apache.maven.plugin.logging.Log;

import com.google.code.rptm.mailarchive.MailingListArchiveEventListener;
import com.google.code.rptm.mailarchive.YearMonth;

public class LoggingMailingListArchiveEventListener implements MailingListArchiveEventListener {
    private final Log log;
    
    public LoggingMailingListArchiveEventListener(Log log) {
        this.log = log;
    }

    public void mboxLoaded(String mailingList, YearMonth month) {
        log.info("Loaded mbox for mailing list " + mailingList + ", month " + month.toSimpleFormat());
    }
}
