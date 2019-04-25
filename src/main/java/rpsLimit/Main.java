package rpsLimit;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.io.IOException;
import java.util.Optional;

import static akka.http.javadsl.marshallers.jackson.Jackson.jsonAs;
import static akka.http.javadsl.model.HttpResponse.create;
import static akka.http.javadsl.server.RequestVals.entityAs;
import static akka.http.scaladsl.model.StatusCodes.InternalServerError;

public class Main extends HttpApp {
    private final TrottlingSevice trottlingSevice;

    @Inject
    public Main(TrottlingSevice trottlingSevice) {
        this.trottlingSevice = trottlingSevice;
    }

    public static void main(String[] args) throws IOException {
        ActorSystem akkaSystem = ActorSystem.create("akka-http");
        Injector injector = Guice.createInjector(new AppModule());
        injector.getInstance(Main.class).bindRoute("0.0.0.0", 8080, akkaSystem);

        System.out.println("<ENTER> to exit!");
        System.in.read();
        akkaSystem.shutdown();
    }

    @Override
    public Route createRoute() {

        return handleExceptions(e -> {
                    e.printStackTrace();
                    return complete(create().withStatus(InternalServerError()));
                },
                post(pathEndOrSingleSlash().route(handleWith(entityAs(jsonAs(Request.class)),
                        (ctx, request) -> {
                            trottlingSevice.isRequestAllowed(Optional.of(request.token));
                            return ctx.completeWithStatus(200);
                        }
                ))));
    }
}
