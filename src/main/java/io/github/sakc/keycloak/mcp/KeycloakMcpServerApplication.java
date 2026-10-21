package io.github.sakc.keycloak.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KeycloakMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KeycloakMcpServerApplication.class, args);
    }
}
