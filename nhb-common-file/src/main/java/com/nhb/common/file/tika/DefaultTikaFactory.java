package com.nhb.common.file.tika;

import org.apache.tika.Tika;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 14:55
 * @description: 默认的 Tika 工厂类
 */
public class DefaultTikaFactory implements TikaFactory {
    private volatile Tika tika;

    @Override
    public Tika getTika() {
        if (tika == null) {
            synchronized (this) {
                if (tika == null) {
                    tika = new Tika();
                }
            }
        }
        return tika;
    }
}
