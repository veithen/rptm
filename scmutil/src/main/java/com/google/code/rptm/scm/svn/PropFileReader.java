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
package com.google.code.rptm.scm.svn;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

class PropFileReader {
    private final DataInputStream in;
    private String key;
    private String value;
    
    public PropFileReader(InputStream in) {
        this.in = new DataInputStream(in);
    }
    
    public boolean next() throws IOException {
        key = next((byte)'K');
        if (key == null) {
            return false;
        } else {
            value = next((byte)'V');
            return true;
        }
    }
    
    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    private String next(byte recordType) throws IOException {
        byte startByte = in.readByte();
        if (startByte == recordType) {
            expect((byte)' ');
            int len = 0;
            byte b;
            while ((b = in.readByte()) != '\n') {
                len = 10*len + b - '0';
            }
            byte[] content = new byte[len];
            in.readFully(content);
            expect((byte)'\n');
            return new String(content, "UTF-8");
        } else if (startByte == 'E') {
            expect((byte)'N');
            expect((byte)'D');
            expect((byte)'\n');
            return null;
        } else {
            throw new IOException("Unexpected record type");
        }
    }
    
    private void expect(byte expected) throws IOException {
        byte actual = in.readByte();
        if (actual != expected) {
            throw new IOException("Unexpected content; expected 0x" + Integer.toHexString(expected & 0xFF)
                    + ", got " + Integer.toHexString(actual & 0xFF));
        }
    }
}
