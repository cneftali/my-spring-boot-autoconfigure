package fr.cneftali.spring.autoconfigure.inwebo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ConfigurationProperties(prefix = "inwebo.api")
public class InWeboProperties {

    @Valid
    private Certificate certificate;

    @Valid
    private Service service;

    @NotNull
    private String restBaseUrl = "https://api.myinwebo.com";

    public static class Service {

        private int id;

        @Valid
        private Push push;

        @Valid
        private Helium helium;

        @Valid
        private Form form;
    }

    public static class Certificate {

        @NotNull
        private String path;

        @NotNull
        private String password;

    }

    public static class Push {

        private boolean authenticate = false;

        private int timeout = 30;

        private int nThread = 10;

    }

    public static class Helium {

        private boolean authenticate = false;

        @NotNull
        private String alias;

    }

    public static class Form {

        private boolean authenticate = false;

        @NotNull
        private String id;

        @NotNull
        private String login;

        @NotNull
        private String password;
    }
}
