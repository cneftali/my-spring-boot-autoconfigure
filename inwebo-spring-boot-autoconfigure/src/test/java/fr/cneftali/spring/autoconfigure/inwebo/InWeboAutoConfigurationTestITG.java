package fr.cneftali.spring.autoconfigure.inwebo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.fest.assertions.Assertions.assertThat;

public class InWeboAutoConfigurationTestITG {

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
    public void test_context() throws Exception {
        // Given
        EnvironmentTestUtils.addEnvironment(this.context, "inwebo.api.certificate.path:certificate.p12");
        EnvironmentTestUtils.addEnvironment(this.context, "inwebo.api.certificate.password:meechaew");
        EnvironmentTestUtils.addEnvironment(this.context, "inwebo.api.service.id:1309");
        this.context.register(JacksonAutoConfiguration.class, InWeboAutoConfiguration.class);

        // When
        this.context.refresh();

        // Then
        assertThat(this.context.getBean("inWeboAuthentication")).isNotNull();
        assertThat(this.context.getBean("inWeboConsoleAdmin")).isNotNull();
        assertThat(this.context.getBean("inWeboRestClient")).isNotNull();
        assertThat(this.context.getBean("inWeboSslContext")).isNotNull();
    }
}