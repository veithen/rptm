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
package com.google.code.rptm;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

public abstract class AbstractStatefulMojo<T> extends AbstractMojo {
    /**
     * @parameter expression="${basedir}"
     * @readonly
     */
    private File basedir;
    
    private final Class<T> stateClass;

    public AbstractStatefulMojo(Class<T> stateClass) {
        this.stateClass = stateClass;
    }
    
    protected abstract String getFileName();
    
    private File getFile() {
        return new File(basedir, getFileName());
    }
    
    protected final void persistState(T state) throws MojoExecutionException {
        try {
            File file = getFile();
            file.getParentFile().mkdirs();
            JAXBContext jaxbContext = JAXBContext.newInstance(stateClass);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(state, file);
        } catch (JAXBException ex) {
            throw new MojoExecutionException("JAXB error: " + ex.getMessage(), ex);
        }
    }
    
    protected final T loadState() throws MojoExecutionException {
        try {
            File file = getFile();
            JAXBContext jaxbContext = JAXBContext.newInstance(stateClass);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return stateClass.cast(unmarshaller.unmarshal(file));
        } catch (JAXBException ex) {
            throw new MojoExecutionException("JAXB error: " + ex.getMessage(), ex);
        }
    }
}
