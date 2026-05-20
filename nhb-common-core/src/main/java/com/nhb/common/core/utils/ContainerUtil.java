package com.nhb.common.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/19 9:05
 * @description: 容器相关工具
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContainerUtil {

    /**
     * 判断当前运行环境是否在容器中
     * @return 结果值 true/false
     */
    public static boolean isRunningInsideContainer() {
        // 1. Docker 专属标记
        if (new File("/.dockerenv").exists()) {
            return true;
        }
        // 2. Podman 专属标记
        if (new File("/run/.containerenv").exists()) {
            return true;
        }
        // 3. 检查 cgroup 信息（兼容 Docker cgroup v1）
        try {
            String cgroup = new String(Files.readAllBytes(Paths.get("/proc/1/cgroup")));
            if (cgroup.contains("/docker/") || cgroup.contains("/lxc/")) {
                return true;
            }
        } catch (IOException e) {
            log.warn("Failed to read container cgroup:{}",e.getMessage());
        }
        // 4. 检查环境变量（Podman 特有）
        try {
            String environ = new String(Files.readAllBytes(Paths.get("/proc/1/environ")));
            if (environ.contains("container=podman")) {
                return true;
            }
        } catch (IOException e) {
            log.warn("Failed to read container environ:{}",e.getMessage());
        }
        return false;
    }
}
