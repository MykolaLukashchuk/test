package rpsLimit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SlaServiceImpl implements SlaService {
    private final static Map<String, SLA> slaMap = new HashMap<>();

    static {
        final SLA user1 = new SLA("User1", 5);
        final SLA user2 = new SLA("User1", 3);
        slaMap.put("token1", user1);
        slaMap.put("token2", user1);
        slaMap.put("token3", user2);
        slaMap.put("token4", user2);
    }


    @Override
    public CompletableFuture<SLA> getSlaByToken(String token) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SLA sla = slaMap.get(token);
            return sla != null ? sla : GUEST;
        });
    }

}
