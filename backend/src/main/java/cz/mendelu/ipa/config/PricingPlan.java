package cz.mendelu.ipa.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
enum PricingPlan {
    FREE(10),
    BASIC(1000),
    PRO(10000);

    private final int rateLimit;

    static PricingPlan forRoles(List<String> roles) {
        if (roles.contains("ADMIN")) {
            return PRO;
        } else if (roles.contains("USER")) {
            return BASIC;
        } else {
            return FREE;
        }
    }

}