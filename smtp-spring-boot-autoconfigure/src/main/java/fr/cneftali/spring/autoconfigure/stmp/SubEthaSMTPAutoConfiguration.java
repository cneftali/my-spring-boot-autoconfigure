package fr.cneftali.spring.autoconfigure.stmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.wiser.Wiser;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
@ConditionalOnClass({SMTPServer.class , Wiser.class})
@AutoConfigureBefore(MailSenderAutoConfiguration.class)
@EnableConfigurationProperties(SubEthaSMTPProperties.class)
public class SubEthaSMTPAutoConfiguration {

    private static final Log LOGGER = LogFactory.getLog(SubEthaSMTPAutoConfiguration.class);

    @PostConstruct
    protected void init() {
        LOGGER.debug("Initialization SMTP Server");
    }

    @Configuration
    @ConditionalOnProperty(prefix = "spring.smtp.server.wiser", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(Wiser.class)
    protected static class WiserConfiguration {

        private final SubEthaSMTPProperties properties;

        private Wiser wiser;

        public WiserConfiguration(final SubEthaSMTPProperties properties) {
            this.properties = properties;
        }

        @Bean
        protected Wiser wiser() {
            wiser = new Wiser();
            wiser.setHostname(properties.getHostName());
            wiser.setPort(properties.getPort());
            wiser.start();
            return wiser;
        }

        @PreDestroy
        public void close() {
            if (this.wiser != null) {
                this.wiser.stop();
            }
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "spring.smtp.server.wiser", name = "enabled", havingValue = "false", matchIfMissing = true)
    protected static class ClassicConfiguration {

        private final SubEthaSMTPProperties properties;

        private SMTPServer smtpServer;

        public ClassicConfiguration(final SubEthaSMTPProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnMissingBean({ SimpleMessageListener.class})
        protected SimpleMessageListener simpleMessageListener() {
            return new MySimpleMessageListener();
        }

        @Bean
        @ConditionalOnMissingBean(MessageHandlerFactory.class)
        protected MessageHandlerFactory messageHandlerFactory(final SimpleMessageListener listener) {
            return new SimpleMessageListenerAdapter(listener);
        }

        @Bean
        @ConditionalOnMissingBean(SMTPServer.class)
        protected SMTPServer smtpServer(final MessageHandlerFactory factory) {
            smtpServer = new SMTPServer(factory);
            smtpServer.setHostName(properties.getHostName());
            smtpServer.setPort(properties.getPort());
            smtpServer.start();
            return smtpServer;
        }

        @PreDestroy
        public void close() {
            if (this.smtpServer != null) {
                this.smtpServer.stop();
            }
        }
    }
}
