package org.openrepose.core.services.datastore.distributed.impl.distributed.servlet;

import org.openrepose.core.domain.ReposeInstanceInfo;
import org.openrepose.core.services.context.ContextAdapter;
import org.openrepose.core.services.context.ServletContextHelper;
import org.openrepose.services.datastore.DatastoreService;
import org.openrepose.core.services.datastore.distributed.impl.distributed.cluster.DistributedDatastoreServiceClusterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/*
 * Builds servlet context for the distributed datastore servlet
 */
@Component("distributedDatastoreServletContextManager")
public class DistributedDatastoreServletContextManager implements ServletContextListener, ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(DistributedDatastoreServletContextManager.class);

   private DatastoreService datastoreService;
   private ApplicationContext applicationContext;
   private ReposeInstanceInfo instanceInfo;

   @Override
   public void contextInitialized(ServletContextEvent sce) {

      final ServletContext servletContext = sce.getServletContext();
      ServletContextHelper.configureInstance(
              servletContext,
              applicationContext);
      servletContext.setAttribute("datastoreService", datastoreService);
      configureReposeInfo(applicationContext);
      initializeService(sce);
   }
   
   public void configureReposeInfo(ApplicationContext context) {
      if (instanceInfo == null) {

         String clusterId = System.getProperty("repose-cluster-id");
         String nodeId = System.getProperty("repose-node-id");
         instanceInfo = new ReposeInstanceInfo(clusterId, nodeId);
      }
      if (context == null) {
         return;
      }

      ReposeInstanceInfo reposeInstanceInfo = context.getBean("reposeInstanceInfo", ReposeInstanceInfo.class);
      reposeInstanceInfo.setClusterId(instanceInfo.getClusterId());
      reposeInstanceInfo.setNodeId(instanceInfo.getNodeId());
   }

   @Override
   public void contextDestroyed(ServletContextEvent sce) {
   }

   public void setDatastoreSystemProperties(DatastoreService datastore, ReposeInstanceInfo instanceInfo) {
      this.datastoreService = datastore;
      this.instanceInfo = instanceInfo;
   }

   private void initializeService(ServletContextEvent sce) {

      ServletContextHelper helper = ServletContextHelper.getInstance(sce.getServletContext());
      ContextAdapter ca = helper.getPowerApiContext();

      ca.getContext(DistributedDatastoreServiceClusterContext.class).contextInitialized(sce);
   }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
