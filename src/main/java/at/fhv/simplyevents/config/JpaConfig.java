package at.fhv.simplyevents.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {
        "at.fhv.simplyevents.persistence.model",
        "at.fhv.simplyevents.billing.infrastructure.persistence.model"
})
@EnableJpaRepositories(basePackages = {
        "at.fhv.simplyevents.persistence.springdata",
        "at.fhv.simplyevents.billing.infrastructure.persistence.springdata"})

public class JpaConfig {
}
