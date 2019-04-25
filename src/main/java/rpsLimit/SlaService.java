package rpsLimit;

import java.util.concurrent.CompletableFuture;

public interface SlaService {
    int GUEST_RPS = 20;
    SLA GUEST = new SLA("guest", GUEST_RPS);

    CompletableFuture<SLA> getSlaByToken(String token);

    class SLA {
        private final String user;
        private final int rps;

        public SLA(String user, int rps) {
            this.user = user;
            this.rps = rps;
        }

        public String getUser() {
            return user;
        }

        public int getRps() {
            return rps;
        }

        @Override
        public boolean equals(Object obj) {
            return user.equals(((SLA)obj).getUser());
        }
    }
}
