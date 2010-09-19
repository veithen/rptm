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
package com.google.code.rptm.metadata.pmc;

import java.net.MalformedURLException;
import java.net.URL;

public final class PmcUtil {
    private PmcUtil() {}

    private static String getProjectIdFromDomain(String domain) {
        int idx = domain.indexOf('.');
        if (domain.substring(idx+1).equals("apache.org")) {
            return domain.substring(0, idx);
        } else {
            return null;
        }
    }
    
    public static String getProjectIdFromSiteUrl(String siteUrl) {
        try {
            return getProjectIdFromDomain(new URL(siteUrl).getHost());
        } catch (MalformedURLException ex) {
            return null;
        }
    }
    
    public static String getProjectIdFromMailingList(String mailingListAddress) {
        int idx = mailingListAddress.indexOf('@');
        return idx == -1 ? null : getProjectIdFromDomain(mailingListAddress.substring(idx+1));
    }
}
