package org.openrepose.core.servlet.boot.service.config;

import org.openrepose.core.services.config.impl.PowerApiConfigurationManager;
import org.openrepose.core.services.context.ContextAdapter;
import org.openrepose.core.services.context.ServletContextHelper;
import org.openrepose.core.services.context.impl.ConfigurationServiceContext;
import org.openrepose.core.services.event.common.EventService;
import org.openrepose.core.services.threading.ThreadingService;
import org.openrepose.core.servlet.InitParameter;
import org.openrepose.core.servlet.PowerApiContextException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@Ignore("remove this tag after a testing strategy for jndi contexts has been fleshed out")
@RunWith(Enclosed.class)
public class PowerApiConfigurationManagerTest {

    public static class WhenInitalizing {

        private ServletContext context;
        private EventService eventManager;
        private ContextAdapter powerApiServletContext;
        private ThreadingService threadManager;
        private Thread mockedConfigurationUpdateThread;

        @Before
        public void standUp() {
            powerApiServletContext = mock(ContextAdapter.class);
            context = mock(ServletContext.class);
        }

        public void mockAll() {
            mockedConfigurationUpdateThread = mock(Thread.class);
            when(mockedConfigurationUpdateThread.getState()).thenReturn(Thread.State.TERMINATED);
            
            eventManager = mock(EventService.class);
            when(powerApiServletContext.eventService()).thenReturn(eventManager);

            threadManager = mock(ThreadingService.class);
            when(threadManager.newThread(any(Runnable.class), anyString())).thenReturn(mockedConfigurationUpdateThread);
            when(powerApiServletContext.threadingService()).thenReturn(threadManager);

            // Setup our context with the mocks
            when(context.getAttribute(ServletContextHelper.SERVLET_CONTEXT_ATTRIBUTE_NAME)).thenReturn(powerApiServletContext);
            when(context.getInitParameter(InitParameter.POWER_API_CONFIG_DIR.getParameterName())).thenReturn("/etc/powerapi");
        }

        @Test(expected = PowerApiContextException.class)
        public void shouldFailOnMissingConfigurationDirectoryInitParam() {
            final ConfigurationServiceContext configurationManager = new ConfigurationServiceContext(new PowerApiConfigurationManager("n/a"), null, null);

            final ServletContextEvent event = new ServletContextEvent(context);
            configurationManager.contextInitialized(event);
        }

        @Test
        public void shouldInitializeCorrectly() {
            mockAll();

            final ConfigurationServiceContext configurationManager = new ConfigurationServiceContext(new PowerApiConfigurationManager("n/a"), null, null);

            final ServletContextEvent event = new ServletContextEvent(context);
            configurationManager.contextInitialized(event);

            verify(context, times(1)).getInitParameter(eq(InitParameter.POWER_API_CONFIG_DIR.getParameterName()));
            verify(context, times(2)).getAttribute(eq(ServletContextHelper.SERVLET_CONTEXT_ATTRIBUTE_NAME));
//            verify(eventService, times(1)).listen(eq(ConfigurationEvent.class), any(EventListener.class));
            verify(mockedConfigurationUpdateThread, times(1)).start();

            assertNotNull(configurationManager);
        }

        @Test
        public void shouldTearDownCleanly() {
            mockAll();

            final ConfigurationServiceContext configurationManager = new ConfigurationServiceContext(new PowerApiConfigurationManager("n/a"), null, null);

            final ServletContextEvent event = new ServletContextEvent(context);
            configurationManager.contextInitialized(event);
            configurationManager.contextDestroyed(event);
            
            verify(mockedConfigurationUpdateThread, times(1)).interrupt();

            assertNotNull(configurationManager);
        }
    }
}
