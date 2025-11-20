package at.fhv.simplyevents;

import at.fhv.simplyevents.persistence.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class EventApplicationRunner implements ApplicationRunner {

    @Autowired
    private AuthorRepository authorRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // TBD: access Database
    }
}
