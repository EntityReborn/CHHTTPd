package me.entityreborn.chhttpd;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

/**
 *
 * @author import
 */
public class SimpleServer extends Server {
    
    private static String VERSION;

    static {
        Package p = SimpleServer.class.getPackage();

        if (p == null) {
            p = Package.getPackage("me.entityreborn.chhttpd");
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
    
    Map<Integer, AbstractConnector> connectorMap = new HashMap<Integer, AbstractConnector>();
    
    public SimpleServer() {
        super();
        
        setHandler(new RequestHandler());
    }
    
    public AbstractConnector listen(int port) {
        if (!connectorMap.containsKey(port)) {
            ServerConnector connector = new ServerConnector(this);
            
            connector.setPort(port);
            addConnector(connector);
            
            connectorMap.put(port, connector);
        }
        
        return connectorMap.get(port);
    }
    
    public boolean unlisten(int port) {
        if (connectorMap.containsKey(port)) {
            AbstractConnector connector = connectorMap.remove(port);
            removeConnector(connector);
            
            return true;
        }
        
        return false;
    }
}
