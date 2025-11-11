package at.fhv.Authors.ui;

import at.fhv.Authors.domain.Author;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

@Controller
public class AuthorTemplateProvider {

    @GetMapping("/")
    public ModelAndView getAuthorTemplate(Model model) {
        List<Author> authors = Arrays.asList(new Author("Ralph", "Hoch"), new Author("FH", "Vorarlberg"));
        return new ModelAndView("index", "authors", authors);
    }
}
