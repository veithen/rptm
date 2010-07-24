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
package org.apache.art.metadata.pmc;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class CommitteeInfoParserTest {
    private static class TestVisitor implements CommitteeInfoVisitor {
        private int startProjectEvents;
        private int endProjectEvents;
        private int memberEvents;
        
        public void startProject(String name) {
            Assert.assertFalse(StringUtils.isBlank(name));
            Assert.assertEquals(name.trim(), name);
            startProjectEvents++;
            memberEvents = 0;
        }

        public void endProject() {
            endProjectEvents++;
            Assert.assertTrue(memberEvents >= 3);
        }

        public void member(String name, String address) {
            memberEvents++;
            if (address.equals("hboutemy@apache.org")) {
                Assert.assertEquals("Herv\u00E9 Boutemy", name);
            }
        }
        
        public void validate() {
            Assert.assertEquals(89, startProjectEvents);
            Assert.assertEquals(89, endProjectEvents);
        }
    }
    
    @Test
    public void test() throws Exception {
        TestVisitor v = new TestVisitor();
        InputStream in = CommitteeInfoParserTest.class.getResourceAsStream("committee-info.txt");
        try {
            CommitteeInfoParser.parse(in, v);
        } finally {
            in.close();
        }
        v.validate();
    }
}
