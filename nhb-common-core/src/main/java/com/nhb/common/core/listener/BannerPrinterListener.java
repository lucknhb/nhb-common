package com.nhb.common.core.listener;

import cn.hutool.core.util.StrUtil;
import cn.hutool.system.oshi.CpuInfo;
import cn.hutool.system.oshi.OshiUtil;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import oshi.hardware.GlobalMemory;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/29 10:49
 * @description: 打印系统信息
 */
public class BannerPrinterListener implements ApplicationListener<ApplicationReadyEvent> {
    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        //获取系统信息
        OperatingSystem operatingSystem = OshiUtil.getOs();
        //PID
        int pid = operatingSystem.getProcessId();
        //当前进程名称
        String processName = operatingSystem.getCurrentProcess().getName();
        //获取系统名称及版本
        String osName = operatingSystem.getFamily();
        String versionName = operatingSystem.getVersionInfo().toString();
        //位数
        int bitness = operatingSystem.getBitness();
        //CPU信息
        CpuInfo cpuInfo = OshiUtil.getCpuInfo();
        //内存
        GlobalMemory memory = OshiUtil.getMemory();
        //网络
        List<NetworkIF> networkIFs = OshiUtil.getNetworkIFs();
        //磁盘信息
        FileSystem fileSystem = operatingSystem.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();
        System.out.println("========================= Start Print Application Info ========================");
        //先打印系统信息 操作系统 CPU CPU正常运行时间 进程和线程 内存 核数 IP地址
        System.out.println(StrUtil.format("========= OS[{}]  BitNess[{}]  PID[{}]  ProcessName[{}] =========",
                osName + " " + versionName, bitness, pid, processName));
        System.out.println(StrUtil.format("=========   CPUTotal[{}]      CPUModel[{}]   =========",
                cpuInfo.getCpuNum(), cpuInfo.getCpuModel()));
        System.out.println(StrUtil.format("=========   MemoryTotal[{}]     MemoryAvailable[{}]   =========",
                FormatUtil.formatBytes(memory.getTotal()), FormatUtil.formatBytes(memory.getAvailable())));
        //网络信息
        System.out.println("=============== Start Print NetWork Info ==============");
        for (NetworkIF networkIF : networkIFs) {
            if (0 == networkIF.getIPv4addr().length){
                continue;
            }
            System.out.println(StrUtil.format("=========   NetWorkDisPlayName[{}]  IPV4[{}]  IPV6[{}] ==============",
                    networkIF.getDisplayName(), String.join(";", networkIF.getIPv4addr()), String.join(";", networkIF.getIPv6addr())));
        }
        System.out.println("=============== End  Print  NetWork  Info ==============");
        //磁盘信息
        System.out.println("=============== Start Print FileSystem Info ==============");
        for (OSFileStore fileStore : fileStores) {
            System.out.println(StrUtil.format("========= Name[{}]  TotalSpace[{}]  UsedSpace[{}]  FreeSpace[{}] =========",
                    fileStore.getName(), FormatUtil.formatBytes(fileStore.getTotalSpace()), FormatUtil.formatBytes(fileStore.getUsableSpace()), FormatUtil.formatBytes(fileStore.getFreeSpace())));
        }
        System.out.println("=============== End  Print FileSystem Info ==============");
        //项目信息 端口 Java环境 时区 项目环境 项目名称
        ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
        //实际项目端口
        String port = environment.getProperty("local.server.port");
        String active = environment.getProperty("spring.profiles.active");
        String applicationName = environment.getProperty("spring.application.name");
        String jvmName = System.getProperty("java.vm.name");
        String javaVendorVersion = System.getProperty("java.vendor.version");
        String javaRuntimeVersion = System.getProperty("java.runtime.version");
        String jvmInfo = System.getProperty("java.vm.info");
        String javaClassVersion = System.getProperty("java.class.version");
        String javaHome = System.getProperty("java.home");
        String projectHome = System.getProperty("user.dir");
        String tempDir = System.getProperty("java.io.tmpdir");
        System.out.println("=============== Start Print Project Info ==============");
        System.out.println(StrUtil.format("========= JDK Home:{}  Project Home:{} Temp Dir:{} =========", javaHome, projectHome, tempDir));
        System.out.println(StrUtil.format("========= JVM Message : {} {} (build {},{}) Java Class Version[{}] =========",
                jvmName, javaVendorVersion, javaRuntimeVersion, jvmInfo, javaClassVersion));
        System.out.println(StrUtil.format("========= Application:{}  Active:{}  Port:{}", applicationName, active, port));
        System.out.println("=============== End  Print  Project Info ==============");
        System.out.println("========================= End Print PrintApplication Info ========================");
    }
}
