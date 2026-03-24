package me.giobyte8.galleries.admin.controllers;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.admin.dto.ScanStatsView;
import me.giobyte8.galleries.admin.services.ScanRequestsService;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import me.giobyte8.galleries.persistence.repositories.ScanStatsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/fragments")
public class FragmentsController {

    private final DirectoryRepository dirRepository;
    private final ScanStatsRepository scanStatsRepository;
    private final ScanRequestsService scanRequestsService;

    @GetMapping("/galleries-table")
    public String galleriesTable(Pageable pageable, Model model) {
        var page = dirRepository.findRoots(pageable);
        model.addAttribute("directories", page);
        return "admin/fragments/galleries-table";
    }

    @GetMapping("/scans-table")
    public String scansTable(
            @RequestParam(required = false) String path,
            Pageable pageable,
            Model model
    ) {
        var sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("startedAt").descending()
        );

        var page = (path != null && !path.isBlank())
                ? scanStatsRepository.findByPath(path, sortedPageable)
                : scanStatsRepository.findAll(sortedPageable);

        model.addAttribute("scans", page.map(ScanStatsView::from));
        model.addAttribute("filterPath", path);
        return "admin/fragments/scans-table";
    }

    @PostMapping("/directories/{id}/trigger-scan")
    public String triggerScan(@PathVariable UUID id, Model model) {
        var result = scanRequestsService.triggerScan(id);
        model.addAttribute("result", result);
        return "admin/fragments/trigger-scan-feedback";
    }
}
