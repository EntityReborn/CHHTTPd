package com.entityreborn.chhttpd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 *
 * @author import
 */
public class SimpleServer {
    private final ContainerServer server;
    private static String VERSION;

    static {
        Package p = SimpleServer.class.getPackage();

        if (p == null) {
            p = Package.getPackage("com.entityreborn.chhttpd");
        }

        VERSION = "(unknown)";

        if (p != null) {
            String v = p.getImplementationVersion();

            if (v != null) {
                VERSION = v;
            }
        }
    }

    public static String getVersion() {
        return VERSION;
    }
    
    Map<Integer, Connection> connectionMap = new HashMap<Integer,Connection>();

    public SimpleServer(Container container) throws IOException {
        server = new ContainerServer(container);
    }

    public void stop() throws IOException {
        server.stop();
        
        for (Connection conn : connectionMap.values()) {
            try {
                conn.close();
            } catch (IOException ex) {
                Logger.getLogger(SimpleServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        connectionMap.clear();
    }
    
    public void listen(int port) {
        if (!connectionMap.containsKey(port)) {
            Connection connection;
            
            try {
                connection = new SocketConnection(server);
            } catch (IOException ex) {
                Logger.getLogger(SimpleServer.class.getName()).log(Level.SEVERE, 
                        "Could not create a socketconnection!", ex);
                return;
            }
            
            SocketAddress address = new InetSocketAddress(port);
            
            try {
                connection.connect(address);
            } catch (IOException ex) {
                Logger.getLogger(SimpleServer.class.getName()).log(Level.SEVERE, 
                        "Could not listen on port " + port, ex);
                return;
            }
            
            connectionMap.put(port, connection);
        }
    }
    
    public boolean unlisten(int port) {
        if (connectionMap.containsKey(port)) {
            Connection conn = connectionMap.remove(port);
            
            try {
                conn.close();
            } catch (IOException ex) {
                Logger.getLogger(SimpleServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return true;
        }
        
        return false;
    }
}
