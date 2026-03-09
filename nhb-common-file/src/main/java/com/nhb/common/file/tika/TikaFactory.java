package com.nhb.common.file.tika;

import org.apache.tika.Tika;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 14:35
 * @description: Tika 工厂类接口
 */
public interface TikaFactory {
    Tika getTika();
}
