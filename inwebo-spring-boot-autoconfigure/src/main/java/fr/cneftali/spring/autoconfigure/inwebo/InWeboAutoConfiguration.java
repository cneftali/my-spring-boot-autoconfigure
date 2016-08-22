package fr.cneftali.spring.autoconfigure.inwebo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inwebo.console.ConsoleAdmin;
import com.inwebo.console.ConsoleAdminService;
import com.inwebo.service.Authentication;
import com.inwebo.service.AuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.ext.ContextResolver;
import javax.xml.ws.BindingProvider;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.SecureRandom;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@Configuration
@ConditionalOnClass({ConsoleAdmin.class, Authentication.class})
@EnableConfigurationProperties(InWeboProperties.class)
public class InWeboAutoConfiguration {

    private static final Log LOGGER = LogFactory.getLog(InWeboAutoConfiguration.class);

    @Autowired
    private InWeboProperties properties;

    @PostConstruct
    public void postConstruct() {
        LOGGER.info("InWeboAutoConfiguration initializing");
    }


    @Bean(name = "inWeboSslContext")
    @Order(HIGHEST_PRECEDENCE)
    @ConditionalOnMissingBean(name = "inWeboSslContext")
    protected SSLContext sslContext() throws Exception {
        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        final KeyStore keyStore = KeyStore.getInstance("PKCS12");
        InputStream keyInput = null;
        try {
            final char[] certPassword = properties.getCertificate().getPassword().toCharArray();
            keyInput = getCertificateFile();
            keyStore.load(keyInput, certPassword);
            keyManagerFactory.init(keyStore, certPassword);
            final SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
            return context;
        } finally {
            if (keyInput != null) {
                try {
                    keyInput.close();
                } catch (final Exception e) {

                }
            }
        }
    }

    private InputStream getCertificateFile() throws Exception {
        final String certificatePath = properties.getCertificate().getPath();
        if (Files.exists(Paths.get(certificatePath))) {
            return Files.newInputStream(Paths.get(certificatePath));
        } else {
            return new ClassPathResource(certificatePath).getInputStream();
        }
    }

    @Bean(name = "inWeboConsoleAdmin")
    @ConditionalOnMissingBean(name = "inWeboConsoleAdmin")
    protected ConsoleAdmin inWeboConsoleAdmin() {
        final ConsoleAdminService service = new ConsoleAdminService();
        final ConsoleAdmin consoleAdmin = service.getConsoleAdmin();
        ((BindingProvider) consoleAdmin).getRequestContext().put("set-jaxb-validation-event-handler", "false");
        return consoleAdmin;
    }

    @Bean(name = "inWeboAuthentication")
    @ConditionalOnMissingBean(name = "inWeboAuthentication")
    protected Authentication inWeboAuthentication() {
        final AuthenticationService as = new AuthenticationService();
        final Authentication authentication = as.getAuthentication();
        ((BindingProvider) authentication).getRequestContext().put("set-jaxb-validation-event-handler", "false");
        return authentication;
    }

    @Configuration
    @ConditionalOnClass({WebTarget.class, ObjectMapper.class})
    @ConditionalOnMissingBean(name = "inWeboRestClient")
    @AutoConfigureAfter(JacksonAutoConfiguration.class)
    protected static class RestClient {

        @Autowired
        private Jackson2ObjectMapperBuilder jacksonBuilder;

        @Autowired
        private InWeboProperties properties;

        @Bean(name = "inWeboRestClient")
        protected WebTarget inWeboRestClient(@Qualifier("inWeboSslContext") final SSLContext sslContext) throws Exception {
            final Client client = ClientBuilder.newBuilder()
                                               .register(JacksonFeature.class)
                                               .register(new ObjectMapperResolver(), ContextResolver.class)
                                               .register(new LoggingFeature())
                                               .sslContext(sslContext)
                                               .build();
            return client.target(properties.getRestBaseUrl());
        }

        private final class ObjectMapperResolver implements ContextResolver<ObjectMapper> {

            private final ObjectMapper objectMapper;

            public ObjectMapperResolver() {
                objectMapper = jacksonBuilder.build();
            }

            @Override
            public ObjectMapper getContext(Class<?> type) {
                return objectMapper;
            }
        }
    }
}
