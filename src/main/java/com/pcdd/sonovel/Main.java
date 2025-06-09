package com.pcdd.sonovel;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.dialect.console.ConsoleLog;
import cn.hutool.log.level.Level;
import cn.hutool.setting.Setting;
import com.openhtmltopdf.util.XRLog;
import com.pcdd.sonovel.context.HttpClientContext;
import com.pcdd.sonovel.core.OkHttpClientFactory;
import com.pcdd.sonovel.model.AppConfig;
import com.pcdd.sonovel.util.ConfigUtils;
import com.pcdd.sonovel.util.EnvUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.nio.file.Paths;

import static org.fusesource.jansi.AnsiRenderer.render;

@SpringBootApplication
public class Main {

    static {
        if (EnvUtils.isDev()) {
            System.out.println(render("当前为开发环境！", "red"));
        }
        ConsoleLog.setLevel(Level.OFF);
        if (EnvUtils.isProd()) {
            XRLog.listRegisteredLoggers().forEach(logger -> XRLog.setLevel(logger, java.util.logging.Level.OFF));
        }
    }

    private static AppConfig config = ConfigUtils.config();

    public static void main(String[] args) {
        watchConfig();
        HttpClientContext.set(OkHttpClientFactory.create(config, true));
        SpringApplication.run(Main.class, args);
    }

    private static void watchConfig() {
        String path;
        String configFilePath = System.getProperty("config.file");

        if (!FileUtil.exist(configFilePath)) {
            path = System.getProperty("user.dir") + File.separator + ConfigUtils.resolveConfigFileName();
        } else {
            path = Paths.get(configFilePath).toAbsolutePath().toString();
        }

        Setting setting = new Setting(path);
        setting.autoLoad(true, aBoolean -> {
            config = ConfigUtils.config();
            System.out.println("<== 配置文件修改成功！");
        });
    }
}