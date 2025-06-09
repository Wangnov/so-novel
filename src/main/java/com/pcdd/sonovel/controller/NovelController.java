package com.pcdd.sonovel.controller;

import com.pcdd.sonovel.model.Chapter;
import com.pcdd.sonovel.model.SearchResult;
import com.pcdd.sonovel.model.SourceInfo;
import com.pcdd.sonovel.service.NovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NovelController {

    private final NovelService novelService;

    @GetMapping("/sources")
    public List<SourceInfo> listSources() {
        return novelService.listSources();
    }

    @GetMapping("/search/aggregated")
    public List<SearchResult> aggregatedSearch(@RequestParam String keyword) {
        return novelService.aggregatedSearch(keyword);
    }

    @GetMapping("/search/single")
    public List<SearchResult> singleSearch(@RequestParam int sourceId, @RequestParam String keyword) {
        return novelService.singleSearch(sourceId, keyword);
    }

    @GetMapping("/toc")
    public List<Chapter> getToc(@RequestParam int sourceId, @RequestParam String bookUrl) {
        return novelService.getToc(sourceId, bookUrl);
    }

    @PostMapping("/download")
    public Map<String, String> download(@RequestBody Map<String, Object> payload) {
        novelService.download(payload);
        return Map.of("message", "下载任务已创建，请关注服务器后台日志");
    }
}