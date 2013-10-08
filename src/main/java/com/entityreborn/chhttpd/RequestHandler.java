/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.entityreborn.chhttpd;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

/**
 *
 * @author import
 */
public class RequestHandler implements Container {
    public void handle(Request request, Response response) {
        final Events.HTTPRequest event = new Events.HTTPRequest(request, response);
        
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
}
