package cz.mendelu.ipa.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final PrincipalFactory principalFactory;

    private final Bucket freeBucket = createBucket(PricingPlan.FREE);
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();

    public RateLimitingService(PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }

    public Bucket resolveBucket(Jwt jwt) {
        if (jwt == null) {
            return freeBucket;
        }

        Principal principal = principalFactory.of(jwt);

        return userBuckets.computeIfAbsent(
                principal.getUsername(),
                p -> createBucket(principal.getPricingPlan())
        );
    }

    private Bucket createBucket(PricingPlan pricingPlan) {
        var limit = Bandwidth.classic(
                pricingPlan.getRateLimit(),
                Refill.intervally(pricingPlan.getRateLimit(), Duration.ofSeconds(60))
        );
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

}