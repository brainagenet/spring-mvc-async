package net.brainage.sample;

import net.brainage.sample.web.config.SampleEmbeddedServletContainerCustomizer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@SpringBootApplication
// @PropertySource(value = {"classpath:/application.properties"}, ignoreResourceNotFound = true)
public class SpringMvcAsyncApplication {

    /*
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        pspc.setIgnoreUnresolvablePlaceholders(true);
        pspc.setIgnoreResourceNotFound(true);
        return pspc;
    }
    */

    @Bean
    public EmbeddedServletContainerCustomizer embeddedServletContainerCustomizer() {
        return new SampleEmbeddedServletContainerCustomizer();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringMvcAsyncApplication.class, args);
    }
}
