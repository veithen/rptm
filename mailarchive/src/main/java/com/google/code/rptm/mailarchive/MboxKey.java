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

class MboxKey {
    private final String mailingList;
    private final YearMonth month;
    
    public MboxKey(String mailingList, YearMonth month) {
        this.mailingList = mailingList;
        this.month = month;
    }

    @Override
    public int hashCode() {
        return mailingList.hashCode()*31 + month.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof MboxKey) {
            MboxKey other = (MboxKey)obj;
            return mailingList.equals(other.mailingList) && month.equals(other.month);
        } else {
            return false;
        }
    }
}
