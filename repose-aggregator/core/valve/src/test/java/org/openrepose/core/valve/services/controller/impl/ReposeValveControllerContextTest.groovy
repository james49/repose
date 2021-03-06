package org.openrepose.core.valve.services.controller.impl

import org.openrepose.core.valve.services.controller.ControllerService
import org.openrepose.core.container.config.ContainerConfiguration
import org.openrepose.core.systemmodel.DestinationEndpoint
import org.openrepose.core.systemmodel.DestinationList
import org.openrepose.core.systemmodel.ReposeCluster
import org.openrepose.core.systemmodel.SystemModel
import org.openrepose.core.services.ServiceRegistry
import org.openrepose.core.services.config.ConfigurationService
import spock.lang.Specification

import javax.servlet.ServletContextEvent

import static org.mockito.Matchers.any
import static org.mockito.Mockito.*

class ReposeValveControllerContextTest extends Specification {
    ServletContextEvent sce
    ServiceRegistry registry
    ControllerService controllerService
    ConfigurationService configurationService

    def setup(){
        sce = mock(ServletContextEvent)
        registry = mock(ServiceRegistry)
        controllerService = mock(ControllerService)
        configurationService = mock(ConfigurationService)

    }

    def "Context Destroyed - happy path"() {
        given:
        def reposeValveControllerContext = new ReposeValveControllerContext(
                controllerService, registry, configurationService)

        when:
        reposeValveControllerContext.contextDestroyed(sce)

        then:
        verify(controllerService, times(1)).updateManagedInstances(any(Map), any(Set))
    }

    def "Container configuration updated - no system model"(){
        when:
        def reposeValveControllerContext = new ReposeValveControllerContext(
                controllerService, registry, configurationService)

        then:
        !reposeValveControllerContext.containerConfigurationListener.initialized

        when:
        reposeValveControllerContext.containerConfigurationListener.configurationUpdated(new ContainerConfiguration())

        then:
        reposeValveControllerContext.containerConfigurationListener.initialized
        !reposeValveControllerContext.systemModelConfigurationListener.initialized
    }

    def "Container configuration updated - with system model"(){
        when:
        def reposeValveControllerContext = new ReposeValveControllerContext(
                controllerService, registry, configurationService)
        reposeValveControllerContext.systemModel = getValidSystemModel()

        then:
        !reposeValveControllerContext.containerConfigurationListener.initialized
        !reposeValveControllerContext.systemModelConfigurationListener.initialized

        when:
        reposeValveControllerContext.containerConfigurationListener.configurationUpdated(new ContainerConfiguration())

        then:
        reposeValveControllerContext.containerConfigurationListener.initialized
        reposeValveControllerContext.systemModelConfigurationListener.initialized
    }


    /**
     * @return a valid system model
     */
    private SystemModel getValidSystemModel() {
        ReposeCluster cluster = new ReposeCluster()
        SystemModel sysModel = new SystemModel()

        cluster.setId("cluster1")
        cluster.setNodes(new org.openrepose.core.systemmodel.NodeList())
        cluster.getNodes().getNode() <<
                new org.openrepose.core.systemmodel.Node(id: "node1", hostname: "localhost", httpPort: 8080, httpsPort: 8181)
        cluster.setDestinations(new DestinationList())
        cluster.getDestinations().getEndpoint() << new DestinationEndpoint(
                hostname: "localhost", port: 9090, default: true, id: "dest1", protocol: "http")

        sysModel.getReposeCluster().add(cluster)

        return sysModel
    }
}
