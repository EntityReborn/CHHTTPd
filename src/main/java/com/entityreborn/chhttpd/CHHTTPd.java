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

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.extensions.AbstractExtension;
import com.laytonsmith.core.extensions.MSExtension;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
@MSExtension("CHHTTPd")
public class CHHTTPd extends AbstractExtension {
    private static SimpleServer server;
    
    public static SimpleServer getServer() {
        return server;
    }
    
    @Override
    public void onStartup() {
        System.out.println("CHHTTPd v" + getVersion() + " starting...");
        try {
            server = new SimpleServer(new RequestContainer());
        } catch (IOException ex) {
            Logger.getLogger(CHHTTPd.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        System.out.println("CHHTTPd v" + getVersion() + " started.");
    }
    
    @Override
    public void onShutdown() {
        System.out.println("CHHTTPd v" + getVersion() + " stopping...");
        
        try {
            if (server != null) {
                server.stop();
            }
        } catch (Exception ex) {
            Logger.getLogger(CHHTTPd.class.getName()).log(Level.SEVERE, 
                    "Could not stop HTTP server!", ex);
        }
        
        System.out.println("CHHTTPd v" + getVersion() + " stopped");
    }

    public Version getVersion() {
        return new SimpleVersion(0,0,2);
    }
}
