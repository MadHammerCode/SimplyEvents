package at.fhv.simplyevents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {
        "at.fhv.simplyevents.persistence.model",
        "at.fhv.simplyevents.billing.infrastructure.persistence.model"
})
@EnableJpaRepositories(basePackages = {
        "at.fhv.simplyevents.persistence.springdata",
        "at.fhv.simplyevents.billing.infrastructure.persistence.springdata"
})
public class EventApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventApplication.class, args);
    }

}
