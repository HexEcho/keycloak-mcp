package io.github.sakc.keycloak.mcp.domain.dto.realm;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RealmRepresentation(
    String id,
    String realm,
    Boolean enabled,
    Boolean registrationAllowed,
    Boolean resetPasswordAllowed,
    Boolean rememberMe,
    Boolean verifyEmail,
    Boolean bruteForceProtected,
    String passwordPolicy,
    String loginTheme,
    String accountTheme,
    String adminTheme,
    String emailTheme,
    Map<String, String> smtpServer,
    Map<String, String> attributes
) {
}
