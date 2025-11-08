package at.fhv.Authors.rest;

import at.fhv.Authors.domain.Author;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class AuthorRestController {

    @GetMapping("/getAuthors")
    public List<Author> getAuthors() {
        List<Author> authors = new ArrayList<Author>();
        authors.add(new Author("John", "Doe"));
        return authors;
    }

    @PostMapping("/createAuthor")
    public Author createAuthor(@RequestBody Author author) {
        // TBD: process author
        System.out.println(author);
        return author;
    }
}
