package com.nhb.common.fory.factory;

import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.CompatibleMode;
import org.apache.fory.config.ForyBuilder;
import org.apache.fory.config.Language;

import java.nio.charset.StandardCharsets;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/28 9:39
 * @description: fory工厂类
 */
public class ForyFactory {
    public static final ForyFactory INSTANCE = new ForyFactory();

    private final ThreadSafeFory fory = new ForyBuilder()
            .withLanguage(Language.JAVA)
            // 启用引用跟踪以支持共享/循环引用。
            // 如果没有重复引用，禁用它会有更好的性能。
            .withRefTracking(false)
            // 压缩 int 以获得更小的大小
            .withIntCompressed(true)
            // 压缩 long 以获得更小的大小
            .withLongCompressed(true)
            // 启用类型前向/后向兼容性
            // 禁用它以获得更小的大小和更好的性能
            .withCompatibleMode(CompatibleMode.COMPATIBLE)
            // 启用异步多线程编译
            .withAsyncCompilation(true)
            //禁用可能允许未知类被反序列化，可能导致安全风险
            .requireClassRegistration(true)
            .buildThreadSafeFory();

    /**
     * 注册类
     * @param clazz  需要注册的类
     * @param <T>
     */
    public <T> void register(Class<T> clazz) {
        fory.register(clazz);
    }

    /**
     * 序列化对象
     * @param object  需要序列化的对象
     * @return        序列化后的字节
     */
    public byte[] serialize(Object object) {
        if (object == null) {
            return new byte[0];
        }
        if (object instanceof String str) {
            return str.getBytes(StandardCharsets.UTF_8);
        }
        return fory.serialize(object);
    }

    /**
     * 反序列化对象
     * @param bytes  字节
     * @return       序列化后的对象
     */
    public Object deserialize(byte[] bytes) {
        return fory.deserialize(bytes);
    }

    /**
     * 反序列化对象
     * @param bytes       字节数组
     * @param targetType  目标类型
     * @return            实际对象
     * @param <T>         泛型类型
     */
    public <T> T deserialize(byte[] bytes, Class<T> targetType) {
        return fory.deserialize(bytes,targetType);
    }
}
