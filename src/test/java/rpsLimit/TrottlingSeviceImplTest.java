package rpsLimit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TrottlingSeviceImplTest {
    public static final String TOKEN_1 = "token1";
    public static final String TOKEN_2 = "token2";
    public static final String TOKEN_3 = "token3";
    public static final String TOKEN_4 = "token4";
    Answer<Object> user1 = invocationOnMock -> CompletableFuture.supplyAsync(() -> {
        sleep(300);
        return new SlaService.SLA("user1", 10);
    });
    Answer<Object> user2 = invocationOnMock -> CompletableFuture.supplyAsync(() -> {
        sleep(300);
        return new SlaService.SLA("user2", 5);
    });
    Answer<Object> guest = invocationOnMock -> CompletableFuture.supplyAsync(() -> new SlaService.SLA("guest", 20));
    Answer<Object> guest2 = invocationOnMock -> CompletableFuture.supplyAsync(() -> {
        sleep(300);
        return new SlaService.SLA("guest", 20);
    });


    private SlaServiceImpl slaService = mock(SlaServiceImpl.class);
    TrottlingSevice trottlingSevice;

    @Before
    public void before() {
        trottlingSevice = new TrottlingSeviceImpl(slaService);
    }

    @Test
    public void test1() {
        when(slaService.getSlaByToken(TOKEN_1)).thenAnswer(user1);
        when(slaService.getSlaByToken(TOKEN_2)).thenAnswer(user1);

        Stream.of(TOKEN_1, TOKEN_2).forEach(t -> trottlingSevice.isRequestAllowed(Optional.of(t)));

        sleep(1000);

        AtomicInteger trueCols = new AtomicInteger();

        List<CompletableFuture<Void>> futureList = IntStream.range(0, 30).mapToObj(value -> CompletableFuture.runAsync(() -> {
            if (doAskRandom(TOKEN_1, TOKEN_2)) {
                trueCols.incrementAndGet();
            }

        })).collect(Collectors.toList());


        while (futureList.stream().anyMatch(f -> !f.isDone())) {
            sleep(1);
        }

        assertEquals(10, trueCols.get());
    }

    private boolean doAskRandom(String t1, String t2) {
        return trottlingSevice.isRequestAllowed(Optional.of((System.currentTimeMillis() % 2 == 0) ? t1 : t2));
    }

    @Test
    public void test2() {
        when(slaService.getSlaByToken(TOKEN_1)).thenAnswer(user1);
        when(slaService.getSlaByToken(TOKEN_2)).thenAnswer(user1);
        when(slaService.getSlaByToken(TOKEN_3)).thenAnswer(user2);
        when(slaService.getSlaByToken(TOKEN_4)).thenAnswer(user2);
        when(slaService.getSlaByToken("any")).thenAnswer(guest);

        Stream.of(TOKEN_1, TOKEN_2).forEach(t -> trottlingSevice.isRequestAllowed(Optional.of(t)));

        sleep(1000);

        AtomicInteger trueCols = new AtomicInteger();

        List<CompletableFuture<Void>> futureList = IntStream.range(0, 20).mapToObj(value -> CompletableFuture.runAsync(() -> {
            doAskRandom(TOKEN_3, TOKEN_4, "any");
            if (doAskRandom(TOKEN_1, TOKEN_2)) {
                trueCols.incrementAndGet();
            }

        })).collect(Collectors.toList());


        while (futureList.stream().anyMatch(f -> !f.isDone())) {
            sleep(1);
        }

        assertEquals(10, trueCols.get());
    }

    @Test
    @Ignore
    public void test3() {
        when(slaService.getSlaByToken(TOKEN_1)).thenAnswer(user1);
        when(slaService.getSlaByToken(TOKEN_2)).thenAnswer(user1);
        when(slaService.getSlaByToken(TOKEN_3)).thenAnswer(user2);
        when(slaService.getSlaByToken(TOKEN_4)).thenAnswer(user2);
        when(slaService.getSlaByToken("any")).thenAnswer(guest2);

        AtomicInteger trueCols = new AtomicInteger();
        List<CompletableFuture<Void>> futureList = IntStream.range(0, 50).mapToObj(value -> CompletableFuture.runAsync(() -> {
            if (doAskRandom(TOKEN_1, TOKEN_2, TOKEN_3, TOKEN_4, "any")) {
                trueCols.incrementAndGet();
            }

        })).collect(Collectors.toList());


        while (futureList.stream().anyMatch(f -> !f.isDone())) {
            sleep(1);
        }

        assertEquals(20, trueCols.get());
    }

    private boolean doAskRandom(String... tokens) {
        int rnd = new Random().nextInt(tokens.length);
        return trottlingSevice.isRequestAllowed(Optional.of(tokens[rnd]));
    }

    private void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
