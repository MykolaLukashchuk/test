package rpsLimit;

import java.util.concurrent.atomic.AtomicInteger;

public class Limiter {
    private final AtomicInteger leftRequest = new AtomicInteger();
    private int rps;
    private long lastRefillTimeStamp;

    public Limiter(SlaService.SLA sla) {
        rps = sla.getRps();
        leftRequest.set(rps);
    }

    public boolean isRequestAllowed() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastRefillTimeStamp > 1000) {
            lastRefillTimeStamp = currentTime;
            resetLeftRequest();
        }
        return leftRequest.getAndDecrement() > 0;
    }

    private void resetLeftRequest() {
        leftRequest.set(rps);
    }
}