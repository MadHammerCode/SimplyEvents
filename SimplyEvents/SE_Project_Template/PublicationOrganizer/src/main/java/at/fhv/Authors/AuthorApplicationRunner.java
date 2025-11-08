package at.fhv.Authors;

import at.fhv.Authors.domain.model.Author;
import at.fhv.Authors.persistence.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class AuthorApplicationRunner implements ApplicationRunner {

    @Autowired
    private AuthorRepository authorRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // TBD: access Database
    }
}
