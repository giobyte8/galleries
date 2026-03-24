package me.giobyte8.galleries.admin.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class DashboardController {

    @GetMapping
    public String home() {
        return "redirect:/admin/galleries";
    }

    @GetMapping("/galleries")
    public String galleries(Pageable pageable, Model model) {
        model.addAttribute("currentPage", "galleries");
        model.addAttribute("pageable", pageable);
        return "admin/galleries";
    }

    @GetMapping("/scans")
    public String scans(
            @RequestParam(required = false) String path,
            Pageable pageable,
            Model model
    ) {
        model.addAttribute("currentPage", "scans");
        model.addAttribute("filterPath", path);
        model.addAttribute("pageable", pageable);
        return "admin/scans";
    }
}
