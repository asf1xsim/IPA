package cz.mendelu.ipa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PrincipalFactory {

    @Value("${keycloak.client-id}")
    private String clientId;

    public Principal of(Jwt jwt) {
        if (jwt == null) {
            return null;
        }

        List<String> roles = List.of();

        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

        if (resourceAccess != null) {
            Object clientDataObj = resourceAccess.get(clientId);

            if (clientDataObj instanceof Map) {
                Map<?, ?> clientData = (Map<?, ?>) clientDataObj;

                Object rolesObj = clientData.get("roles");
                if (rolesObj instanceof List) {
                    roles = ((List<?>) rolesObj).stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .toList();
                }
            }
        }

        return new Principal(
                jwt.getClaim("preferred_username"),
                jwt.getClaim("name"),
                jwt.getClaim("email"),
                roles,
                PricingPlan.forRoles(roles)
        );
    }
}