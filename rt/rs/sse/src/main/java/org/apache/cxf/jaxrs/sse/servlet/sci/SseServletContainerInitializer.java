/**
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
package org.apache.cxf.jaxrs.sse.servlet.sci;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;
import javax.ws.rs.Path;

import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.apache.cxf.jaxrs.sse.atmosphere.SseAtmosphereInterceptor;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.handler.ReflectorServletProcessor;

@HandlesTypes({Path.class})
public class SseServletContainerInitializer implements ServletContainerInitializer {
    private static final String SSE_SERVLET_NAME = "cxfSseServlet";
    
    private final CXFNonSpringServlet delegate;
    
    public SseServletContainerInitializer() {
        this(new CXFNonSpringJaxrsServlet());
    }
    
    public SseServletContainerInitializer(final CXFNonSpringServlet delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext ctx) throws ServletException {
        if (delegate != null) {
            final AtmosphereServlet servlet = new AtmosphereServlet(true);
            servlet.framework().addAtmosphereHandler("/*", new ReflectorServletProcessor(delegate));
            servlet.framework().interceptor(new SseAtmosphereInterceptor());
            servlet.framework().addInitParameter(ApplicationConfig.PROPERTY_NATIVE_COMETSUPPORT, "true");
            servlet.framework().addInitParameter(ApplicationConfig.WEBSOCKET_SUPPORT, "true");
            servlet.framework().addInitParameter(ApplicationConfig.DISABLE_ATMOSPHEREINTERCEPTOR, "true");
            servlet.framework().addInitParameter(ApplicationConfig.CLOSE_STREAM_ON_CANCEL, "true");
            
            final ServletRegistration.Dynamic registration = ctx.addServlet(SSE_SERVLET_NAME, servlet);
            registration.setAsyncSupported(true);
            registration.addMapping("/*");
        }
    }
}
