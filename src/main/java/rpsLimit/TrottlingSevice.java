package rpsLimit;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface TrottlingSevice {

    boolean isRequestAllowed(Optional<String> token);
}
