package rpsLimit;

import com.google.inject.AbstractModule;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Main.class);
        bind(TrottlingSevice.class).to(TrottlingSeviceImpl.class);
        bind(SlaService.class).to(SlaServiceImpl.class);
    }
}
