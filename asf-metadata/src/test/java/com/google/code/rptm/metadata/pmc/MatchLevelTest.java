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
package com.google.code.rptm.metadata.pmc;

import junit.framework.Assert;

import org.junit.Test;

import com.google.code.rptm.metadata.pmc.MatchLevel;

public class MatchLevelTest {
    @Test
    public void testCompare1() {
        Assert.assertTrue(MatchLevel.NO_MATCH.compareTo(MatchLevel.FUZZY_MATCH) < 0);
    }

    @Test
    public void testCompare2() {
        Assert.assertTrue(MatchLevel.FUZZY_MATCH.compareTo(MatchLevel.EXACT_MATCH) < 0);
    }
}
