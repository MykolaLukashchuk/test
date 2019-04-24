package rpsLimit;

import lombok.Data;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

public class TrottlingSeviceImpl implements TrottlingSevice {
    public final int guestRPS;
    private final Map<String, Limiter> limiters;
    private final SlaService.SLA guest;
    private final Map<String, CashItem> cash;
    private SlaService slaService;

    public TrottlingSeviceImpl(SlaService slaService, int guestRPS) {
        this.slaService = slaService;
        this.guestRPS = guestRPS;
        guest = new SlaService.SLA("Guest", guestRPS);
        limiters = new ConcurrentHashMap<>();
        limiters.put("Guest", new Limiter(guest));
        cash = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isRequestAllowed(Optional<String> token) {
        SlaService.SLA sla = null;
        if (token.get().equals("")) {
            sla = guest;
        } else {
            try {
                CashItem cashItem = cash.get(token.get());
                if (cashItem == null || System.currentTimeMillis() - cashItem.getTimeCreate() > 400) {
                    CompletableFuture<SlaService.SLA> futureSLA = slaService.getSlaByToken(token.get());
                    futureSLA.thenApply(sla1 -> cash.put(token.get(), new CashItem(sla1)));
                    sla = futureSLA.get(1, TimeUnit.MILLISECONDS);

                } else {
                    sla = cashItem.getSla();
                }
                limiters.putIfAbsent(sla.getUser(), new Limiter(sla));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                sla = guest;
            }
        }
        return limiters.get(sla.getUser()).isRequestAllowed();
    }

    @Data
    static class CashItem {
        Long timeCreate;
        SlaService.SLA sla;

        public CashItem(SlaService.SLA sla) {
            this.sla = sla;
            timeCreate = System.currentTimeMillis();
        }
    }
}
