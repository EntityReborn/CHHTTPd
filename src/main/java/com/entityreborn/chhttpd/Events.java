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
package com.entityreborn.chhttpd;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Part;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class Events {
    protected static class HTTPRequest implements BindableEvent {
        private final Request request;
        private final Response response;

        public HTTPRequest(Request request, Response response) {
            this.request = request;
            this.response = response;
        }
        
        public Object _GetObject() {
            return this;
        }
        
        public String getPath() {
            return request.getPath().getPath();
        }
        
        public String getServerName() {
            return request.getValue("Host");
        }
        
        public String getMethod() {
            return request.getMethod();
        }
        
        public void setCode(int code) {
            response.setCode(code);
        }
        
        public void setHeader(String key, String value) {
            response.setValue(key, value);
        }
        
        public Request getRequest() {
            return request;
        }
        
        public void setBody(String body) throws IOException {
            response.getPrintStream().append(body);
        }
        
        public void setContentType(String type) {
            response.setContentType(type);
        }
        
        public void setCookie(String key, String value) {
            response.setCookie(key, value);
        }

        public void setCookie(Cookie c) {
            response.setCookie(c);
        }
        
        public void commit() {
            try {
                response.commit();
            } catch (IOException ex) {
                Logger.getLogger(Events.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    response.getPrintStream().close();
                } catch (IOException ex1) {
                    Logger.getLogger(Events.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
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
                Target t = Target.UNKNOWN;
                HTTPRequest req = (HTTPRequest)e;
                
                retn.put("host", new CString(req.getServerName(), t));
                retn.put("method", new CString(req.getMethod(), t));
                retn.put("path", new CString(req.getPath(), t));
                
                Request r = req.getRequest();
                
                CArray params = new CArray(t);
                
                for (String key : r.getQuery().keySet()) {
                    CArray items = new CArray(t);
                    
                    for (String item : r.getQuery().getAll(key)) {
                        items.push(new CString(item, t), t);
                    }
                    
                    params.set(key, items, t);
                }
                
                retn.put("parameters", params);
                
                CArray headers = new CArray(t);
                
                for (String key : r.getNames()) {
                    CArray items = new CArray(t);
                    
                    for (String item : r.getValues(key)) {
                        items.push(new CString(item, t), t);
                    }
                    
                    headers.set(key, items, t);
                }
                
                retn.put("headers", headers);
                
                CArray data = new CArray(t);
                
                for (Part part : r.getParts()) {
                    String key = part.getName();
                    String value = "";
                    
                    try {
                        value = part.getContent();
                    } catch (IOException ex) {
                        Logger.getLogger(Events.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    data.set(key, value, t);
                }
                
                retn.put("postdata", data);
                
                CArray cookies = new CArray(t);
                
                for (Cookie cookie : r.getCookies()) {
                    CArray cook = new CArray(t);
                    
                    cook.set("value", cookie.getValue());
                    cook.set("expires", String.valueOf(cookie.getExpiry()));
                    cook.set("path", cookie.getPath());
                    cook.set("domain", cookie.getDomain());
                    cook.set("httponly", 
                            CBoolean.get(cookie.isProtected()), t);
                    
                    cookies.set(cookie.getName(), cook, t);
                }
                
                retn.put("cookies", cookies);
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
                        throw new CREIOException(ex.getMessage(), Target.UNKNOWN);
                    }
                } else if ("code".equalsIgnoreCase(key)) {
                    if (!(value instanceof CInt)) {
                        throw new CREFormatException("You must specify an int!", Target.UNKNOWN);
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

        @Override
        public void postExecution(Environment env, BoundEvent.ActiveEvent activeEvent) {
            if (activeEvent.getUnderlyingEvent() instanceof HTTPRequest) {
                HTTPRequest req = (HTTPRequest)activeEvent.getUnderlyingEvent();
                
                req.commit();
            }
        }
        

        public Version since() {
            return CHVersion.V3_3_1;
        }

        public BindableEvent convert(CArray manualObject, Target t) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
}
