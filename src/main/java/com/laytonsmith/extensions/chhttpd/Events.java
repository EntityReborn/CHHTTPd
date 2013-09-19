/*
 * The MIT License
 *
 * Copyright 2013 Jason Unger <entityreborn@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.laytonsmith.extensions.chhttpd;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class Events {
    public static void fireevent(String path, Request request, HttpServletResponse response) {
        final HTTPRequest event = new HTTPRequest(path, request, response);
        try {
            StaticLayer.GetConvertor().runOnMainThreadAndWait(new Callable<Object>() {
                public Object call() {
                    EventUtils.TriggerListener(Driver.EXTENSION, "http_request", event);
                    return null;
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(Events.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static class HTTPRequest implements BindableEvent {
        private final String path;
        private final Request request;
        private final HttpServletResponse response;

        public HTTPRequest(String path, Request request, HttpServletResponse response) {
            this.path = path;
            this.request = request;
            this.response = response;
        }
        
        public Object _GetObject() {
            return this;
        }
        
        public String getPath() {
            return path;
        }
        
        public String getServerName() {
            return request.getServerName();
        }
        
        public int getPort() {
            return request.getServerPort();
        }
        
        public Map<String, String[]> getParmeters() {
            return request.getParameterMap();
        }
        
        public String getMethod() {
            return request.getMethod();
        }
        
        public void setCode(int code) {
            request.setHandled(true);
            response.setStatus(code);
        }
        
        public void setBody(String body) throws IOException {
            request.setHandled(true);
            response.getWriter().append(body);
        }
        
        public void setContentType(String type) {
            request.setHandled(true);
            response.setContentType(type);
        }
    }
    
    @api
    public static class http_request extends AbstractEvent {

        public String getName() {
            return "http_request";
        }

        public String docs() {
            return ""; //TBA
        }

        public boolean matches(Map<String, Construct> prefilter, BindableEvent e) throws PrefilterNonMatchException {
            return true;
        }

        public BindableEvent convert(CArray manualObject) {
            return null;
        }

        public Map<String, Construct> evaluate(BindableEvent e) throws EventException {
            Map<String, Construct> retn = new HashMap<String, Construct>();
            
            if (e instanceof HTTPRequest) {
                HTTPRequest req = (HTTPRequest)e;
                retn.put("method", new CString(req.getMethod(), Target.UNKNOWN));
                retn.put("path", new CString(req.getPath(), Target.UNKNOWN));
                retn.put("port", new CInt(req.getPort(), Target.UNKNOWN));
                
                CArray params = new CArray(Target.UNKNOWN);
                for (String name: req.getParmeters().keySet()) {
                    CArray arr = new CArray(Target.UNKNOWN);
                    
                    String[] values = req.getParmeters().get(name);
                    for (String value : values) {
                        arr.push(new CString(value, Target.UNKNOWN));
                    }
                    
                    params.set(name, arr, Target.UNKNOWN);
                }
                
                retn.put("parameters", params);
                
            }
            
            return retn;
        }

        public Driver driver() {
            return Driver.EXTENSION;
        }

        public boolean modifyEvent(String key, Construct value, BindableEvent event) throws ConfigRuntimeException {
            if (event instanceof HTTPRequest) {
                HTTPRequest req = (HTTPRequest)event; 
                
                if ("body".equalsIgnoreCase(key)) {
                    try {
                        req.setBody(value.val());
                        return true;
                    } catch (IOException ex) {
                        throw new ConfigRuntimeException(ex.getMessage(), ExceptionType.IOException, Target.UNKNOWN);
                    }
                } else if ("code".equalsIgnoreCase(key)) {
                    if (!(value instanceof CInt)) {
                        throw new ConfigRuntimeException("You must specify an int!", ExceptionType.FormatException, Target.UNKNOWN);
                    }
                    
                    req.setCode(Static.getInt16(value, Target.UNKNOWN));
                    return true;
                } else if ("contenttype".equalsIgnoreCase(key)) {
                    req.setContentType(value.val());
                    return true;
                }
            }
            
            return false;
        }

        public Version since() {
            return CHVersion.V3_3_1;
        }
    }
    
}
