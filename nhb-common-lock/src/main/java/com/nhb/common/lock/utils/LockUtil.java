package com.nhb.common.lock.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.UUID;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 15:55
 * @description: 分布式锁工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LockUtil {
    public static String getLockId() {
        return simpleUUID();
    }

    /**
     * 获取本机网卡地址
     *
     * @return macAddress
     */
    public static String getLocalMAC() {
        String localMac;
        try {
            InetAddress ia = InetAddress.getLocalHost();
            // 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
            // 下面代码是把mac地址拼装成String
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                // mac[i] & 0xFF 是为了把byte转化为正整数
                String s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }
            // 把字符串所有小写字母改为大写成为正规的mac地址并返回
            localMac = sb.toString();
        } catch (Exception e) {
            localMac = UUID.randomUUID().toString();
        }
        return localMac.toUpperCase().replace("-", "");
    }

    /**
     * 获取jvmPId
     *
     * @return jvmPid
     */
    public static String getJvmPid() {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        int indexOf = pid.indexOf('@');
        if (indexOf > 0) {
            pid = pid.substring(0, indexOf);
            return pid;
        }
        throw new IllegalStateException("ManagementFactory error");
    }

    /**
     * 去除-的uuid
     *
     * @return simpleUUID
     */
    public static String simpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
