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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.code.rptm.metadata.aliases.MailAliasVisitor;

public class Aliases implements MailAliasVisitor {
    private final Map<String,String> primaryAddressMap = new HashMap<String,String>();
    private final Map<String,Set<String>> aliasMap = new HashMap<String,Set<String>>();

    public void visitMailAlias(String primaryAddress, String alias) {
        primaryAddress = primaryAddress.toLowerCase();
        alias = alias.toLowerCase();
        primaryAddressMap.put(alias, primaryAddress);
        Set<String> aliases = aliasMap.get(primaryAddress);
        if (aliases == null) {
            aliases = new HashSet<String>();
            aliasMap.put(primaryAddress, aliases);
        }
        aliases.add(alias);
    }

    public String getPrimaryAddress(String address) {
        address = address.toLowerCase();
        String primaryAddress = primaryAddressMap.get(address);
        if (primaryAddress != null) {
            return primaryAddress;
        } else if (aliasMap.containsKey(address)) {
            return address;
        } else {
            return null;
        }
    }
    
    public Set<String> getAliases(String primaryAddress) {
        return aliasMap.get(primaryAddress.toLowerCase());
    }
}
