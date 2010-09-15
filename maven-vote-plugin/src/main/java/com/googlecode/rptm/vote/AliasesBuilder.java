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
import java.util.Map;

import com.google.code.rptm.metadata.aliases.MailAliasVisitor;

public class AliasesBuilder implements MailAliasVisitor {
    private final Map<String,String> aliases = new HashMap<String,String>();

    public void visitMailAlias(String primaryAddress, String alias) {
        aliases.put(alias, primaryAddress);
    }

    public Aliases getAliases() {
        return new Aliases(aliases);
    }
}
