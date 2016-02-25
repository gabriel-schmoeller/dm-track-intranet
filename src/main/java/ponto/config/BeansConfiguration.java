package ponto.config;

import org.springframework.boot.context.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ponto.Main;

/**
 * @author gabriel.schmoeller
 */
@Configuration
@ComponentScan(basePackageClasses = Main.class)
public class BeansConfiguration {

    @Bean
    public ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());
        //Wite/Read dates as ISO Strings
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        //indent the json output so it is easier to read
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        return objectMapper;
    }

    @Bean
    public UndertowEmbeddedServletContainerFactory embeddedServletContainerFactory() {
        UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();
        factory.addBuilderCustomizers(new UndertowBuilderCustomizer() {
            @Override
            public void customize(io.undertow.Undertow.Builder builder) {
                builder.addHttpListener(7374, "0.0.0.0");
            }
        });

        return factory;
    }
}
