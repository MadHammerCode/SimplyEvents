package at.fhv.simplyevents.config;

import at.fhv.simplyevents.service.PendingBookingCache;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public PendingBookingCache pendingBookingCache() {
        return new PendingBookingCache(Duration.ofMinutes(15));
    }
}

