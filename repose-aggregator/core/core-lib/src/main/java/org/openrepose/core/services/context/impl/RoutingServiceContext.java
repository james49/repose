package org.openrepose.core.services.context.impl;

import org.openrepose.commons.config.manager.UpdateListener;
import org.openrepose.core.domain.Port;
import org.openrepose.core.domain.ReposeInstanceInfo;
import org.openrepose.core.domain.ServicePorts;
import org.openrepose.core.systemmodel.Node;
import org.openrepose.core.systemmodel.ReposeCluster;
import org.openrepose.core.systemmodel.SystemModel;
import org.openrepose.core.services.ServiceRegistry;
import org.openrepose.core.services.config.ConfigurationService;
import org.openrepose.core.services.context.ServiceContext;
import org.openrepose.core.services.routing.RoutingService;
import org.openrepose.core.servlet.InitParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.net.URL;

@Component("routingServiceContext")
public class RoutingServiceContext implements ServiceContext<RoutingService> {

    public static final String SERVICE_NAME = "powerapi:/services/routing";
    private static final Logger LOG = LoggerFactory.getLogger(RoutingServiceContext.class);
    private final RoutingService service;
    private SystemModel config;
    private ConfigurationService configurationManager;
    private final PowerApiConfigListener configListener;
    private final ServiceRegistry registry;
    private final ServicePorts servicePorts;
    private String clusterId, nodeId;
    private ReposeInstanceInfo instanceInfo;

    @Autowired
    public RoutingServiceContext(
            @Qualifier("routingService") RoutingService service,
            @Qualifier("serviceRegistry") ServiceRegistry registry,
            @Qualifier("configurationManager") ConfigurationService configurationManager,
            @Qualifier("servicePorts") ServicePorts servicePorts,
            @Qualifier("reposeInstanceInfo") ReposeInstanceInfo instanceInfo) {
        this.service = service;
        configListener = new PowerApiConfigListener();
        this.registry = registry;
        this.configurationManager = configurationManager;
        this.servicePorts = servicePorts;
        this.instanceInfo = instanceInfo;
    }

    public void register() {
        if (registry != null) {
            registry.addService(this);
        }
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public RoutingService getService() {
        return service;
    }

    private class PowerApiConfigListener implements UpdateListener<SystemModel> {

        private boolean isInitialized = false;

        private ServicePorts determinePorts(Node reposeNode) {
            ServicePorts ports = new ServicePorts();

            if (reposeNode != null) {
                if (reposeNode.getHttpPort() != 0) {
                    ports.add(new Port("http", reposeNode.getHttpPort()));
                } else {
                    LOG.error("Http service port not specified for Repose Node " + reposeNode.getId());
                }

                if (reposeNode.getHttpsPort() != 0) {
                    ports.add(new Port("https", reposeNode.getHttpsPort()));
                } else {
                    LOG.info("Https service port not specified for Repose Node " + reposeNode.getId());
                }
            }

            return ports;
        }

        private Node determineReposeNode(String clusterId, String nodeId) {

            for (ReposeCluster cluster : config.getReposeCluster()) {

                if (cluster.getId().equals(clusterId)) {
                    for (Node node : cluster.getNodes().getNode()) {
                        if (node.getId().equals(nodeId)) {
                            return node;
                        }
                    }
                }
            }

            return null;
        }

        @Override
        public void configurationUpdated(SystemModel configurationObject) {
            config = configurationObject;
            service.setSystemModel(config);


            if (!isInitialized()) {
                LOG.info("Determining ports for repose under cluster: " + clusterId);
                ServicePorts ports = determinePorts(determineReposeNode(clusterId, nodeId));
                servicePorts.clear();
                servicePorts.addAll(ports);
            }
            isInitialized = true;
        }

        @Override
        public boolean isInitialized() {
            return isInitialized;
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        ServletContext ctx = servletContextEvent.getServletContext();

        final String clusterIdParam = InitParameter.REPOSE_CLUSTER_ID.getParameterName();
        final String nodeIdParam = InitParameter.REPOSE_NODE_ID.getParameterName();

        clusterId = System.getProperty(clusterIdParam, ctx.getInitParameter(clusterIdParam));
        nodeId = System.getProperty(nodeIdParam, ctx.getInitParameter(nodeIdParam));

        if (instanceInfo == null) {
            instanceInfo = new ReposeInstanceInfo(clusterId, nodeId);
        } else {
            instanceInfo.setClusterId(clusterId);
            instanceInfo.setNodeId(nodeId);
        }
        URL xsdURL = getClass().getResource("/META-INF/schema/system-model/system-model.xsd");
        configurationManager.subscribeTo("system-model.cfg.xml", xsdURL, configListener, SystemModel.class);
        register();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (configurationManager != null) {
            configurationManager.unsubscribeFrom("system-model.cfg.xml", configListener);
        }
    }
}
