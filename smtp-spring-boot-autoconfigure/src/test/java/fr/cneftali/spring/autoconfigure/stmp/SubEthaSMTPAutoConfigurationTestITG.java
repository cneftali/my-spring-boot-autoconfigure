package fr.cneftali.spring.autoconfigure.stmp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SocketUtils;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.wiser.Wiser;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static javax.mail.Message.RecipientType.TO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;


public class SubEthaSMTPAutoConfigurationTestITG {

    private AnnotationConfigApplicationContext context;

    @Before
    public void setUp() {
        this.context = new AnnotationConfigApplicationContext();
    }

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void test_getSmtpServerBean() throws Exception {
        // Given
        int port = SocketUtils.findAvailableTcpPort();
        EnvironmentTestUtils.addEnvironment(this.context, "spring.smtp.server.hostName:localhost");
        EnvironmentTestUtils.addEnvironment(this.context, "spring.smtp.server.port:" + port);
        this.context.register(SubEthaSMTPAutoConfiguration.class);

        // When
        this.context.refresh();

        // Then
        assertThat(this.context.getBean(MySimpleMessageListener.class)).isNotNull();
        assertThat(this.context.getBean(MessageHandlerFactory.class)).isNotNull();
        assertThat(this.context.getBean(SMTPServer.class)).isNotNull();
    }

    @Test
    public void test_getSmtpServerBean_send_email() throws Exception {
        // Given
        int port = SocketUtils.findAvailableTcpPort();
        EnvironmentTestUtils.addEnvironment(this.context, "spring.smtp.server.hostName:localhost");
        EnvironmentTestUtils.addEnvironment(this.context, "spring.smtp.server.port:" + port);
        this.context.register(SubEthaSMTPAutoConfiguration.class);
        this.context.refresh();

        // When
        sendMail(port, "toto@example.com", "titi@example.com");

        // Then
        assertThat(this.context.getBean(MySimpleMessageListener.class)).isNotNull();
        final MySimpleMessageListener bean = this.context.getBean(MySimpleMessageListener.class);
        assertThat(bean.getMessages()).isNotEmpty();
        assertThat(bean.getMessages()).hasSize(1);
        assertThat(bean.getMessages().get(0).getEnvelopeReceiver()).isEqualTo("titi@example.com");
        assertThat(bean.getMessages().get(0).getEnvelopeSender()).isEqualTo("toto@example.com");
        assertThat(bean.getMessages().get(0).getMessageData()).isNotEmpty();
    }

    private void sendMail(int port, String from, String to) throws Exception {
        final Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", "localhost");
        properties.setProperty("mail.smtp.port", String.valueOf(port));
        Session session = Session.getDefaultInstance(properties);
        final MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(TO, new InternetAddress(to));
        message.setSubject("This is the Subject Line!");
        message.setText("This is actual message");
        Transport.send(message);
    }

    @Test
    public void test_getSmtpServerBeanWithSpecificsMessageListenerAndMessageHandler() throws Exception {
        // Given
        int port = SocketUtils.findAvailableTcpPort();
        EnvironmentTestUtils.addEnvironment(this.context, "spring.smtp.server.hostName:localhost");
        EnvironmentTestUtils.addEnvironment(this.context, "spring.smtp.server.port:" + port);
        this.context.register(SimpleMessageListenerAndMessageHandlerFactoryConfiguration.class, SubEthaSMTPAutoConfiguration.class);

        // When
        this.context.refresh();

        // Then
        assertThat(this.context.getBean(TestSimpleMessageListener.class)).isNotNull();
        assertThat(this.context.getBean(MessageHandlerFactory.class)).isNotNull();
        assertThat(this.context.getBean(MessageHandlerFactory.class)).isInstanceOf(TestMessageHandlerFactory.class);
        assertThat(this.context.getBean(SMTPServer.class)).isNotNull();
    }



    @Test
    public void test_getWiserBean() throws Exception {
        // Given
        int port = SocketUtils.findAvailableTcpPort();
        EnvironmentTestUtils.addEnvironment(this.context, "spring.smtp.server.hostName:localhost");
        EnvironmentTestUtils.addEnvironment(this.context, "spring.smtp.server.port:" + port);
        EnvironmentTestUtils.addEnvironment(this.context, "spring.smtp.server.wiser.enabled:true");
        this.context.register(SubEthaSMTPAutoConfiguration.class);

        // When
        this.context.refresh();

        // Then
        assertThat(this.context.getBean(Wiser.class)).isNotNull();
        assertMissingBean(SMTPServer.class);
    }


    @Configuration
    protected static class SimpleMessageListenerAndMessageHandlerFactoryConfiguration {


        @Bean
        protected TestSimpleMessageListener simpleMessageListener() {
            return new TestSimpleMessageListener();
        }

        @Bean
        protected MessageHandlerFactory messageHandlerFactory(final TestSimpleMessageListener listener) {
            return new TestMessageHandlerFactory(listener);
        }
    }

    protected static class TestSimpleMessageListener implements SimpleMessageListener {


        @Override
        public boolean accept(String from, String recipient) {
            return false;
        }

        @Override
        public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException {

        }
    }

    protected static class TestMessageHandlerFactory implements MessageHandlerFactory {


        public TestMessageHandlerFactory(final SimpleMessageListener listener) {
        }

        @Override
        public MessageHandler create(MessageContext ctx) {
            return null;
        }
    }

    protected void assertMissingBean(final Class<?> beanClass) {
        try {
            assertThat(this.context.getBean(beanClass)).isNotNull();
            fail("Unexpected bean in context of type " + beanClass.getName());
        } catch (final NoSuchBeanDefinitionException ex) {
            // Expected
        }
    }
}