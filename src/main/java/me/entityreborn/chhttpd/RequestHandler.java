/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.entityreborn.chhttpd;

import static com.laytonsmith.extensions.chhttpd.Events.fireevent;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 *
 * @author import
 */
public class RequestHandler extends AbstractHandler {
    public RequestHandler() {
    }

    public void handle(String string, Request request, HttpServletRequest srequest, HttpServletResponse response) throws IOException, ServletException {
        fireevent(string, request, response);
    }
}
