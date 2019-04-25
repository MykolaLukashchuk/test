package rpsLimit;

import com.google.inject.Inject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static rpsLimit.SlaService.SLA;

public class TrottlingSeviceImpl implements TrottlingSevice {
    private final Map<String, Limiter> limiters;
    private final Map<String, SLA> cash;
    private SlaService slaService;

    @Inject
    public TrottlingSeviceImpl(SlaService slaService) {
        this.slaService = slaService;
        limiters = new ConcurrentHashMap<>();
        cash = new ConcurrentHashMap<>();
        limiters.put("guest", new Limiter(slaService.GUEST));
    }

    @Override
    public boolean isRequestAllowed(Optional<String> token) {
        SLA curSla = cash.computeIfAbsent(token.get(), t -> {
            slaService.getSlaByToken(t).thenAccept(sla -> cash.put(t, sla));
            return slaService.GUEST;
        });

        return limiters.computeIfAbsent(curSla.getUser(), u -> new Limiter(curSla)).isRequestAllowed();
    }
}
