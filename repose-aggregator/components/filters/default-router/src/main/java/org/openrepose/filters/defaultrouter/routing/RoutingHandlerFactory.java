package org.openrepose.filters.defaultrouter.routing;

import com.google.common.base.Optional;
import org.openrepose.commons.config.manager.UpdateListener;
import org.openrepose.core.filter.SystemModelInterrogator;
import org.openrepose.core.filter.logic.AbstractConfiguredFilterHandlerFactory;
import org.openrepose.core.systemmodel.Destination;
import org.openrepose.core.systemmodel.ReposeCluster;
import org.openrepose.core.systemmodel.SystemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

public class RoutingHandlerFactory extends AbstractConfiguredFilterHandlerFactory<RoutingTagger> implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final SystemModelInterrogator modelInterrogator;
    private Destination dst;
    private static final Logger LOG = LoggerFactory.getLogger(RoutingTagger.class);

    public RoutingHandlerFactory(SystemModelInterrogator modelInterrogator) {
        this.modelInterrogator = modelInterrogator;
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) {
        this.applicationContext = ac;
    }

    private class RoutingConfigurationListener implements UpdateListener<SystemModel> {

        private boolean isInitialized = false;

        @Override
        public void configurationUpdated(SystemModel configurationObject) {
            Optional<Destination> destination = modelInterrogator.getDefaultDestination(configurationObject);
            Optional<ReposeCluster> cluster = modelInterrogator.getLocalCluster(configurationObject);

            dst = destination.orNull();
            if (cluster.isPresent() && !destination.isPresent()) {
                LOG.warn("No default destination configured for service domain: " + cluster.get().getId());
            } else if (!cluster.isPresent() && !destination.isPresent()) {
                LOG.warn("Unable to identify the local host in the system model - please check your system-model.cfg.xml");
            }

            isInitialized = true;
        }

        @Override
        public boolean isInitialized() {
            return isInitialized;
        }
    }

    @Override
    protected RoutingTagger buildHandler() {

        if (!this.isInitialized()) {
            return null;
        }
        return applicationContext.getBean("routingTagger", RoutingTagger.class).setDestination(dst);
    }

    @Override
    protected Map<Class, UpdateListener<?>> getListeners() {
        return new HashMap<Class, UpdateListener<?>>() {

            {
                put(SystemModel.class, new RoutingConfigurationListener());
            }
        };
    }
}
