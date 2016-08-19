package fr.cneftali.spring.autoconfigure.stmp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.smtp.server")
public class SubEthaSMTPProperties {

    private String hostName = "localhost";

    private Integer port = 25;

    private Map<String, String> properties = new LinkedHashMap<>();
}
