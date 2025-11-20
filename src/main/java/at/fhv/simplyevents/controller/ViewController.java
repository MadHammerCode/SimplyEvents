package at.fhv.simplyevents.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/dashboard")
    public String authorsView() {
        return "Dashboard"; // Name der HTML-Datei ohne .html
    }

    @GetMapping("/events")
    public String eventList() {
        return "eventlist";     // lädt templates/eventlist.html
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // login.html in /templates
    }
}
