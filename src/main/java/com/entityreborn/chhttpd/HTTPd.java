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

import com.entityreborn.chhttpd.Events.HTTPRequest;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.exceptions.CRE.CREBindException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREIOException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.io.IOException;
import org.simpleframework.http.Cookie;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class HTTPd {
    public static HTTPRequest getEvent(String source, Environment env, Target t) {
        BoundEvent.ActiveEvent active = env.getEnv(GlobalEnv.class).GetEvent();

        if (active == null) {
            throw new CREBindException(source + " must be called from within an event handler", t);
        }

        Event e = active.getEventDriver();

        if (active.getBoundEvent().getPriority().equals(BoundEvent.Priority.MONITOR)) {
            throw new CREBindException("Monitor level handlers may not modify an event!", t);
        }

        if (!(active.getUnderlyingEvent() instanceof HTTPRequest)) {
            throw new CREBindException("This function must be called in the http_request event!", t);
        }

        HTTPRequest req = (HTTPRequest)active.getUnderlyingEvent();

        return req;
    }

    // Unashamedly copied from modify_event. Original version (C) LadyCailin
    @api
    public static class httpd_set_header extends AbstractFunction {

        public String getName() {
            return "httpd_set_header";
        }

        public Integer[] numArgs() {
            return new Integer[]{2};
        }

        public String docs() {
            return "void {key, value} Sets a header in the response. Works like modify_event, meaning it should be"
					+ " called from within a bind.";
        }

        public Class<? extends CREThrowable>[] thrown() {
            return new Class[]{CREBindException.class};
        }

        public boolean isRestricted() {
            return true;
        }

        public MSVersion since() {
            return MSVersion.V3_3_1;
        }

        public Boolean runAsync() {
            return false;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
            String key = args[0].val();
            String value = args[1].val();

            HTTPRequest req = getEvent(this.getName(), environment, t);

            req.setHeader(key, value);

            return CVoid.VOID;
        }
    }

    // Unashamedly copied from modify_event. Original version (C) LadyCailin
    @api
    public static class httpd_set_cookie extends AbstractFunction {

        public String getName() {
            return "httpd_set_cookie";
        }

        public Integer[] numArgs() {
            return new Integer[]{1, 2};
        }

        public String docs() {
            return "void {cookiesArray | key, value} Sets one or more cookies. If the input parameter is"
					+ " an associative array, cookies are set with the given key/value pairs. Otherwise,"
					+ " the input should be a key and a value, and a cookie with that name and value will"
					+ " be set. Works like modify_event, meaning it should be called within a bind.";
        }

        public Class<? extends CREThrowable>[] thrown() {
            return new Class[]{CREBindException.class};
        }

        public boolean isRestricted() {
            return true;
        }

        public MSVersion since() {
            return MSVersion.V3_3_1;
        }

        public Boolean runAsync() {
            return false;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
            HTTPRequest req = getEvent(this.getName(), environment, t);

            if (args[0] instanceof CArray) {
                CArray parts = (CArray)args[0];

                if (!parts.inAssociativeMode()) {
                    throw new CREFormatException("Expecting an associative array for httpd_set_cookie!", t);
                }

                if (!parts.containsKey("name") || !parts.containsKey("value")) {
                    throw new CREFormatException("Associative array for httpd_set_cookie must contain 'name' and 'value'!", t);
                }

                Cookie c = new Cookie(parts.get("name", t).val(), parts.get("value", t).val());

                if (parts.containsKey("path")) {
                    c.setPath(parts.get("path", t).val());
                }

                if (parts.containsKey("expires")) {
                    c.setExpiry(Static.getInt32(parts.get("expires", t), t));
                }

                if (parts.containsKey("domain")) {
                    c.setDomain(parts.get("domain", t).val());
                }

                if (parts.containsKey("httponly")) {
                    c.setProtected(Static.getBoolean(parts.get("httponly", t), t));
                }

                req.setCookie(c);
            } else {
                String key = args[0].val();
                String value = args[1].val();

                req.setCookie(key, value);
            }

            return CVoid.VOID;
        }
    }

    @api
    public static class httpd_listen extends AbstractFunction {

        public String getName() {
            return "httpd_listen";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "void {port} Begins listening on the given port. Note that ports less than 1024 usually require"
					+ " running as root/Administrator in order to bind to. Once this is called, the http_request"
					+ " event will begin firing when connections are received.";
        }

        public Class<? extends CREThrowable>[] thrown() {
            return new Class[]{CREFormatException.class, CREIOException.class};
        }

        public boolean isRestricted() {
            return true;
        }

        public MSVersion since() {
            return MSVersion.V3_3_1;
        }

        public Boolean runAsync() {
            return false;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
            int port = Static.getInt32(args[0], t);

            try {
                CHHTTPd.getServer().listen(port);
            } catch (IOException e) {
                throw new CREIOException("Could not listen on port " + port, t);
            }

            return CVoid.VOID;
        }
    }

    @api
    public static class httpd_unlisten extends AbstractFunction {

        public String getName() {
            return "httpd_unlisten";
        }

        public Integer[] numArgs() {
            return new Integer[]{1};
        }

        public String docs() {
            return "void {port} Unbinds from a previously bound port with httpd_listen.";
        }

        public Class<? extends CREThrowable>[] thrown() {
            return new Class[]{CREFormatException.class};
        }

        public boolean isRestricted() {
            return true;
        }

        public MSVersion since() {
            return MSVersion.V3_3_1;
        }

        public Boolean runAsync() {
            return false;
        }

        public Mixed exec(Target t, Environment environment, Mixed... args) throws ConfigRuntimeException {
            int port = Static.getInt32(args[0], t);

            CHHTTPd.getServer().unlisten(port);

            return CVoid.VOID;
        }
    }
}
