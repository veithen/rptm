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
package com.google.code.rptm.scm.svn;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

public class PropFileReaderTest {
    @Test
    public void test() throws Exception {
        InputStream in = PropFileReaderTest.class.getResourceAsStream("dir-prop-base");
        try {
            PropFileReader pfr = new PropFileReader(in);
            Assert.assertTrue(pfr.next());
            Assert.assertEquals("svn:ignore", pfr.getKey());
            Assert.assertEquals(".settings\ntarget\n.classpath\n.project\n", pfr.getValue());
            Assert.assertFalse(pfr.next());
        } finally {
            in.close();
        }
    }
}
