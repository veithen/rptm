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
package org.apache.art.mailarchive;

import org.apache.commons.lang.StringUtils;

/**
 * Identifies a year and month.
 */
public class YearMonth {
    private final int year;
    private final int month;
    
    /**
     * Construct a new instance with the given year and month.
     * 
     * @param year the year
     * @param month the month
     */
    public YearMonth(int year, int month) {
        this.year = year;
        this.month = month;
    }

    /**
     * Get the year.
     * 
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * Get the month.
     * 
     * @return a value in the range 1..12
     */
    public int getMonth() {
        return month;
    }
    
    /**
     * Construct a new instance representing the previous month.
     * 
     * @return the previous month
     */
    public YearMonth previous() {
        return month == 1 ? new YearMonth(year-1, 12) : new YearMonth(year, month-1);
    }
    
    /**
     * Construct a new instance representing the next month.
     * 
     * @return the next month
     */
    public YearMonth next() {
        return month == 12 ? new YearMonth(year+1, 1) : new YearMonth(year, month+1);
    }
    
    /**
     * Format the year and month as YYYYMM.
     * 
     * @return the formatted year and month
     */
    public String toSimpleFormat() {
        return year + StringUtils.leftPad(String.valueOf(month), 2, '0');
    }
}
