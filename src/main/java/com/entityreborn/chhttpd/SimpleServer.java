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
    
    public void listen(int port) throws IOException {
        if (!connectionMap.containsKey(port)) {
            Connection connection;
            
            connection = new SocketConnection(server);
            
            SocketAddress address = new InetSocketAddress(port);
            
            connection.connect(address); // Let it throw an exception if necessary.
            
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
