package com.bruhmosuki.dhoomaKedu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    public static File getUploadBaseDirectory() {
        // 1. Try user.dir/uploads
        String userDir = System.getProperty("user.dir");
        File dir = new File(userDir, "uploads");
        try {
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (dir.canWrite()) {
                return dir;
            }
        } catch (Exception ignored) {}

        // 2. Fallback to user.home/.dhoomakedu/uploads
        File homeDir = new File(System.getProperty("user.home"), ".dhoomakedu/uploads");
        try {
            if (!homeDir.exists()) {
                homeDir.mkdirs();
            }
            if (homeDir.canWrite()) {
                return homeDir;
            }
        } catch (Exception ignored) {}

        // 3. Final fallback to java.io.tmpdir
        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "dhoomakedu/uploads");
        tmpDir.mkdirs();
        return tmpDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File uploadFolder = getUploadBaseDirectory();
        String uploadPath = uploadFolder.getAbsolutePath();
        if (!uploadPath.endsWith(File.separator)) {
            uploadPath += File.separator;
        }

        // Map both /message/uploads/** and /uploads/** so reverse proxies routing /message/* always work
        registry.addResourceHandler("/message/uploads/**", "/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
