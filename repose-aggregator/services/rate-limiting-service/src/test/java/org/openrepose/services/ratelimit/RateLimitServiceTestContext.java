package org.openrepose.services.ratelimit;

import org.openrepose.services.ratelimit.config.ConfiguredRatelimit;
import org.openrepose.services.ratelimit.config.HttpMethod;
import org.openrepose.services.ratelimit.config.TimeUnit;

import java.util.List;

public class RateLimitServiceTestContext {
    public static final String SIMPLE_URI_REGEX = "/loadbalancer/.*";
    public static final String COMPLEX_URI_REGEX = "/loadbalancer/vips/.*";
    public static final String GROUPS_URI_REGEX = "/loadbalancer/(.*)/1234";
    public static final String SIMPLE_URI = "*loadbalancer*";
    public static final String COMPLEX_URI = "*loadbalancer/vips*";
    public static final String GROUPS_URI = "*loadbalancer/vips/cap1/1234*";
    public static final String SIMPLE_ID = "12345-ABCDE";
    public static final String COMPLEX_ID = "09876-ZYXWV";

    protected ConfiguredRatelimit newLimitConfig(String limitId, String uri, String uriRegex, List<HttpMethod> methods, List<String> queryNames) {
        final ConfiguredRatelimit configuredRateLimit = new ConfiguredRatelimit();

        configuredRateLimit.setId(limitId);
        configuredRateLimit.setUnit(TimeUnit.HOUR);
        configuredRateLimit.setUri(uri);
        configuredRateLimit.setUriRegex(uriRegex);
        configuredRateLimit.setValue(20);
        for (String qn : queryNames) {
            configuredRateLimit.getQueryParamNames().add(qn);
        }
        for (HttpMethod m : methods) {
            configuredRateLimit.getHttpMethods().add(m);
        }

        return configuredRateLimit;
    }
}
