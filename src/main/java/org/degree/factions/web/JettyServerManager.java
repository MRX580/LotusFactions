package org.degree.factions.web;

import org.degree.factions.web.api.FactionsServlet;
import org.degree.factions.web.api.StatusServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.nio.file.Path;

public class JettyServerManager {
    private final Server server;

    public JettyServerManager(int port) {
        this.server = new Server(port);
    }

    public void startServer(Path webDir) throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase(webDir.toAbsolutePath().toString());
        context.setWelcomeFiles(new String[]{"index.html"});
        context.addServlet(DefaultServlet.class, "/");

        context.addServlet(new ServletHolder(new StatusServlet()), "/api/status");
        context.addServlet(new ServletHolder(new FactionsServlet()), "/api/factions");

        server.setHandler(context);
        server.start();
    }

    public void stopServer() throws Exception {
        if (server != null && server.isStarted()) {
            server.stop();
        }
    }
}
