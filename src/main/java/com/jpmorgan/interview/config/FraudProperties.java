package com.jpmorgan.interview.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "fraud")
public class FraudProperties {

    private int maxAttempts = 3;
    private int retryWindowMinutes = 10;
    private boolean autoBlock = true;

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getRetryWindowMinutes() {
        return retryWindowMinutes;
    }

    public void setRetryWindowMinutes(int retryWindowMinutes) {
        this.retryWindowMinutes = retryWindowMinutes;
    }

    public boolean isAutoBlock() {
        return autoBlock;
    }

    public void setAutoBlock(boolean autoBlock) {
        this.autoBlock = autoBlock;
    }
}
