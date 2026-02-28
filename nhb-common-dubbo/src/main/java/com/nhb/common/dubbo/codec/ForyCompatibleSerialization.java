package com.nhb.common.dubbo.codec;

import org.apache.fory.Fory;
import org.apache.fory.collection.Tuple2;
import org.apache.fory.config.CompatibleMode;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.memory.MemoryUtils;
import org.apache.fory.util.LoaderBinding;

public class ForyCompatibleSerialization extends BaseForySerialization {
    public static final byte FORY_SERIALIZATION_ID = 29;
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
                                                        .withCompatibleMode(CompatibleMode.COMPATIBLE)
                                                        .withClassLoader(classLoader)
                                                        .build());
                        MemoryBuffer buffer = MemoryUtils.buffer(32);
                        return Tuple2.of(binding, buffer);
                    });

    public byte getContentTypeId() {
        return FORY_SERIALIZATION_ID;
    }

    public String getContentType() {
        return "fory/compatible";
    }

    @Override
    protected Tuple2<LoaderBinding, MemoryBuffer> getFory() {
        return foryFactory.get();
    }
}