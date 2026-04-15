package me.giobyte8.galleries.admin.controllers;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.services.DirectoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/directories")
public class DirectoryAutocompleteController {

    private final DirectoryService directoryService;

    @GetMapping("/autocomplete")
    public String autocomplete(
            @RequestParam(required = false) String q,
            Model model
    ) {
        var directories = directoryService.searchByPath(q);
        model.addAttribute("directories", directories);
        return "admin/fragments/directory-autocomplete-options";
    }
}

