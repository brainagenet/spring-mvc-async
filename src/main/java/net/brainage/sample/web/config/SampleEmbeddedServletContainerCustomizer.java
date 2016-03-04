package net.brainage.sample.web.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;

/**
 * Created by ms29.seo on 2016-02-02.
 */
@Slf4j
public class SampleEmbeddedServletContainerCustomizer implements EmbeddedServletContainerCustomizer {

    @Value("${servlet.container.maxThreads}")
    private int maxThreads;

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        if (container instanceof TomcatEmbeddedServletContainerFactory) {
            customizeTomcat((TomcatEmbeddedServletContainerFactory) container);
        }
    }

    private void customizeTomcat(TomcatEmbeddedServletContainerFactory container) {
        container.addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                Object defaultMaxThreads = connector.getAttribute("maxThreads");
                connector.setAttribute("maxThreads", maxThreads);
                log.info("Changed tomcat connector maxThreads from {} to {}.", defaultMaxThreads, maxThreads);
            }
        });
    }

}
