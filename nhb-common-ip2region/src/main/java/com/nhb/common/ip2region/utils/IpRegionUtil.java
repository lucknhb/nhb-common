package com.nhb.common.ip2region.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.nhb.common.core.utils.IpUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.service.Ip2Region;
import org.lionsoul.ip2region.xdb.Util;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/25 10:14
 * @description: IP地址转换为区域名称<BR/>
 *  IP地址行政区域工具类 参考地址：<a href="https://gitee.com/lionsoul/ip2region/tree/master/binding/java">ip2region xdb java 查询客户端实现</a><BR/>
 *  xdb数据库文件下载：<a href="https://gitee.com/lionsoul/ip2region/tree/master/data">ip2region data</a>
 */
@Slf4j
public class IpRegionUtil {
    private static final Ip2Region IP_2_REGION = SpringContextUtil.getBean(Ip2Region.class);
    //未知地址
    public static final String UNKNOWN_ADDRESS = "未知";
    // 未知IP
    public static final String UNKNOWN_IP = "未知IP";
    // 内网地址
    public static final String LOCAL_ADDRESS = "内网IP";

    /**
     * 根据IP地址离线获取城市
     *
     * @param ip ip地址字符串
     */
    public static String getRegion(String ip) {
        try {
            // 处理空串并过滤HTML标签
            ip = HtmlUtil.cleanHtmlTag(StrUtil.blankToDefault(ip, ""));
            // 判断是否为IPv4
            boolean isIPv4 = IpUtil.isIPv4(ip);
            // 判断是否为IPv6
            boolean isIPv6 = IpUtil.isIPv6(ip);
            // 如果不是IPv4或IPv6，则返回未知IP
            if (!isIPv4 && !isIPv6) {
                return UNKNOWN_IP;
            }
            // 内网不查询
            if ((isIPv4 && IpUtil.isInnerIP(ip)) || (isIPv6 && IpUtil.isInnerIPv6(ip))) {
                return LOCAL_ADDRESS;
            }
            String region = IP_2_REGION.search(ip);
            if (StringUtils.isBlank(region)) {
                region = UNKNOWN_ADDRESS;
            }
            return region;
        } catch (Exception e) {
            log.error("Ip region find happen exception {}", ip,e);
            return UNKNOWN_ADDRESS;
        }

    }

    /**
     * 根据IP地址离线获取城市
     *
     * @param ipBytes ip地址字节数组
     */
    public static String getRegion(byte[] ipBytes) {
        try {
           return getRegion(Util.ipToString(ipBytes));
        } catch (Exception e) {
            log.error("IP地址离线获取城市异常 {}", Util.ipToString(ipBytes));
            return UNKNOWN_ADDRESS;
        }
    }


}
