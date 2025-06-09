package com.pcdd.sonovel.core;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.model.Rule;
import com.pcdd.sonovel.util.ConfigUtils;

public class Source {

    public final Rule rule;
    public final AppConfig config;

    public Source(AppConfig config) {
        this(config.getSourceId(), config);
    }

    public Source(int id) {
        this(id, null);
    }

    public Source(int id, AppConfig config) {
        String jsonStr = null;

        try {
            jsonStr = ResourceUtil.readUtf8Str("rule/rule-" + id + ".json");
        } catch (Exception e) {
            Console.error("书源规则初始化失败，请检查配置项 source-id 是否正确。{}", e.getMessage());
            System.exit(1);
        }

        this.rule = applyDefaultRule(JSONUtil.toBean(jsonStr, Rule.class));
        this.config = Opt.ofNullable(config).orElse(ConfigUtils.config());
    }

    private Rule applyDefaultRule(Rule rule) {
        Rule.Search ruleSearch = rule.getSearch();
        Rule.Book ruleBook = rule.getBook();
        Rule.Toc ruleToc = rule.getToc();
        Rule.Chapter ruleChapter = rule.getChapter();

        if (ruleSearch != null && StrUtil.isEmpty(ruleSearch.getBaseUri())) {
            ruleSearch.setBaseUri(rule.getUrl());
        }
        if (ruleBook != null && StrUtil.isEmpty(ruleBook.getBaseUri())) {
            ruleBook.setBaseUri(rule.getUrl());
        }
        if (ruleToc != null && StrUtil.isEmpty(ruleToc.getBaseUri())) {
            ruleToc.setBaseUri(rule.getUrl());
        }
        if (ruleChapter != null && StrUtil.isEmpty(ruleChapter.getBaseUri())) {
            ruleChapter.setBaseUri(rule.getUrl());
        }

        if (ruleSearch != null && ruleSearch.getTimeout() == null) {
            ruleSearch.setTimeout(15);
        }
        if (ruleBook != null && ruleBook.getTimeout() == null) {
            ruleBook.setTimeout(15);
        }
        if (ruleToc != null && ruleToc.getTimeout() == null) {
            ruleToc.setTimeout(30);
        }
        if (ruleChapter != null && ruleChapter.getTimeout() == null) {
            ruleChapter.setTimeout(10);
        }

        return rule;
    }
}