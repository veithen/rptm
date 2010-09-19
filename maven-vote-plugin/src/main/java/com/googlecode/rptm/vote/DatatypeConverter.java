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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.code.rptm.mailarchive.YearMonth;

public class DatatypeConverter {
    protected static final DatatypeFactory DATATYPE_FACTORY;
    
    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static YearMonth parseYearMonth(String s) {
        XMLGregorianCalendar cal = DATATYPE_FACTORY.newXMLGregorianCalendar(s);
        return new YearMonth(cal.getYear(), cal.getMonth());
    }
    
    public static String printYearMonth(YearMonth month) {
        XMLGregorianCalendar cal = DATATYPE_FACTORY.newXMLGregorianCalendar();
        cal.setYear(month.getYear());
        cal.setMonth(month.getMonth());
        return cal.toXMLFormat();
    }
    
    public static Date parseDateTime(String s) {
        return javax.xml.bind.DatatypeConverter.parseDateTime(s).getTime();
    }
    
    public static String printDateTime(Date date) {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        return javax.xml.bind.DatatypeConverter.printDateTime(cal);
    }
}
