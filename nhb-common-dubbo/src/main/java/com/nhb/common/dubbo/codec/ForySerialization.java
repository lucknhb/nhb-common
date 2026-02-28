package com.nhb.common.dubbo.codec;

import org.apache.fory.Fory;
import org.apache.fory.collection.Tuple2;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.memory.MemoryUtils;
import org.apache.fory.util.LoaderBinding;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/28 10:49
 * @description:  fory序列化方式
 */
public class ForySerialization extends BaseForySerialization {
    public static final byte FORY_SERIALIZATION_ID = 28;
    private static final ThreadLocal<Tuple2<LoaderBinding, MemoryBuffer>> foryFactory =
            ThreadLocal.withInitial(
                    () -> {
                        LoaderBinding binding =
                                new LoaderBinding(
                                        classLoader ->
                                                Fory.builder()
                                                        .withRefTracking(true)
                                                        .withStringCompressed(true)
                                                        .requireClassRegistration(false)
                                                        .withClassLoader(classLoader)
                                                        .build());
                        MemoryBuffer buffer = MemoryUtils.buffer(32);
                        return Tuple2.of(binding, buffer);
                    });

    public byte getContentTypeId() {
        return FORY_SERIALIZATION_ID;
    }

    public String getContentType() {
        return "fory/consistent";
    }

    @Override
    protected Tuple2<LoaderBinding, MemoryBuffer> getFory() {
        return foryFactory.get();
    }
}