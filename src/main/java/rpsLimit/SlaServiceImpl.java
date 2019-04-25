package rpsLimit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SlaServiceImpl implements SlaService {
    private final static Map<String, SLA> slaMap = new HashMap<>();

    static {
        final SLA user1 = new SLA("user1", 10);
        final SLA user2 = new SLA("user2", 5);
        slaMap.put("token1", user1);
        slaMap.put("token2", user1);
        slaMap.put("token3", user2);
        slaMap.put("token4", user2);
    }


    @Override
    public CompletableFuture<SLA> getSlaByToken(String token) {
        return CompletableFuture.supplyAsync(() -> {
            SLA sla = null;
            try {
                Thread.sleep(250);
                sla = slaMap.get(token);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return sla != null ? sla : GUEST;
        });
    }

}
