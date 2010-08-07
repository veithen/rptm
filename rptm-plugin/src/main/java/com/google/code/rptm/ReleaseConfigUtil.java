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
package com.google.code.rptm;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.maven.plugin.MojoExecutionException;

import com.google.code.rptm.release.config.ReleaseConfig;

public final class ReleaseConfigUtil {
    private ReleaseConfigUtil() {}
    
    public static ReleaseConfig loadReleaseConfig(File file) throws MojoExecutionException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ReleaseConfig.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (ReleaseConfig)unmarshaller.unmarshal(file);
        } catch (JAXBException ex) {
            throw new MojoExecutionException("JAXB error: " + ex.getMessage(), ex);
        }
    }
}
