package su.kazuyuki07.frontend.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @Value("${app.gateway.url}")
    private String gatewayUrl;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("gatewayUrl", gatewayUrl);
        return "index";
    }

    @GetMapping("/clients")
    public String clients(Model model) {
        model.addAttribute("gatewayUrl", gatewayUrl);
        return "clients";
    }

    @GetMapping("/visits")
    public String visits(Model model) {
        model.addAttribute("gatewayUrl", gatewayUrl);
        return "visits";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("gatewayUrl", gatewayUrl);
        return "reports";
    }
}
