package rpsLimit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    TrottlingSevice trottlingSevice;

    @Before
    public void before() {
        trottlingSevice = new TrottlingSeviceImpl(new SlaServiceImpl());
    }

    @Test
    public void test1() {
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
    public void test3() {
        AtomicInteger trueCols = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(50);

        List<CompletableFuture<Void>> futureList = IntStream.range(0, 50).mapToObj(value -> CompletableFuture.runAsync(() -> {
            if (doAskRandom(TOKEN_1, TOKEN_2, TOKEN_3, TOKEN_4, "any")) {
                trueCols.incrementAndGet();
            }
        }, executorService)).collect(Collectors.toList());


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
