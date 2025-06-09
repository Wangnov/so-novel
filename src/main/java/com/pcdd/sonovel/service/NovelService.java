package com.pcdd.sonovel.service;

import cn.hutool.core.collection.CollUtil;
import com.pcdd.sonovel.core.Crawler;
import com.pcdd.sonovel.core.Source;
import com.pcdd.sonovel.handle.SearchResultsHandler;
import com.pcdd.sonovel.model.*;
import com.pcdd.sonovel.parse.SearchParser;
import com.pcdd.sonovel.parse.SearchParser6;
import com.pcdd.sonovel.parse.TocParser;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.util.SourceUtils;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class NovelService {

    public List<SourceInfo> listSources() {
        return SourceUtils.ALL_IDS.stream()
                .map(id -> {
                    Rule rule = new Source(id).rule;
                    return SourceInfo.builder()
                            .id(rule.getId())
                            .name(rule.getName())
                            .url(rule.getUrl())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public List<SearchResult> aggregatedSearch(String keyword) {
        List<SearchResult> results = Collections.synchronizedList(new ArrayList<>());
        List<Source> searchableSources = SourceUtils.getSearchableSources();
        ExecutorService threadPool = Executors.newFixedThreadPool(searchableSources.size());
        CountDownLatch latch = new CountDownLatch(searchableSources.size());

        for (Source source : searchableSources) {
            threadPool.execute(() -> {
                try {
                    List<SearchResult> res = new SearchParser(source.config).parse(keyword);
                    if (CollUtil.isNotEmpty(res)) {
                        Rule rule = source.rule;
                        System.out.printf("<== 书源 %d (%s)\t搜索到 %d 条记录%n", rule.getId(), rule.getName(), res.size());
                        results.addAll(res);
                    }
                } catch (Exception e) {
                    System.err.printf("搜索源 %s 异常：%s%n", source.rule.getName(), e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        threadPool.shutdown();
        return SearchResultsHandler.aggregateSort(results, keyword);
    }

    public List<SearchResult> singleSearch(int sourceId, String keyword) {
        AppConfig config = ConfigUtils.config();
        config.setSourceId(sourceId);
        if (sourceId == 6) {
            return new SearchParser6(config).parse(keyword);
        }
        return new SearchParser(config).parse(keyword, true);
    }

    public List<Chapter> getToc(int sourceId, String bookUrl) {
        AppConfig config = ConfigUtils.config();
        config.setSourceId(sourceId);
        TocParser tocParser = new TocParser(config);
        return tocParser.parse(bookUrl);
    }

    @Async("taskExecutor")
    @SneakyThrows
    public CompletableFuture<Void> download(Map<String, Object> payload) {
        int sourceId = (int) payload.get("sourceId");
        String bookUrl = (String) payload.get("bookUrl");

        AppConfig config = ConfigUtils.config();
        config.setSourceId(sourceId);

        TocParser tocParser = new TocParser(config);
        List<Chapter> toc = tocParser.parse(bookUrl);

        if (CollUtil.isNotEmpty(toc)) {
            Crawler crawler = new Crawler(config);
            crawler.crawl(bookUrl, toc);
        } else {
            System.err.printf("无法获取书籍目录，下载失败。URL: %s%n", bookUrl);
        }

        return CompletableFuture.completedFuture(null);
    }
}