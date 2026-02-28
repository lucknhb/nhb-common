package com.nhb.common.dubbo.codec;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.serialize.ObjectInput;
import org.apache.dubbo.common.serialize.ObjectOutput;
import org.apache.dubbo.common.serialize.Serialization;
import org.apache.dubbo.rpc.model.FrameworkModel;

import org.apache.fory.Fory;
import org.apache.fory.collection.Tuple2;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.util.LoaderBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/28 10:49
 * @description:  基础序列化
 */
public abstract class BaseForySerialization implements Serialization {
    protected abstract Tuple2<LoaderBinding, MemoryBuffer> getFory();

    public ObjectOutput serialize(URL url, OutputStream output) throws IOException {
        Tuple2<LoaderBinding, MemoryBuffer> tuple2 = getFory();
        tuple2.f0.setClassLoader(Thread.currentThread().getContextClassLoader());
        Fory fory = tuple2.f0.get();
        ForyCheckerListener checkerListener = getCheckerListener(url);
        fory.getClassResolver().setTypeChecker(checkerListener.getChecker());
        fory.getClassResolver().setSerializerFactory(checkerListener);
        return new ForyObjectOutput(fory, tuple2.f1, output);
    }

    public ObjectInput deserialize(URL url, InputStream input) throws IOException {
        Tuple2<LoaderBinding, MemoryBuffer> tuple2 = getFory();
        tuple2.f0.setClassLoader(Thread.currentThread().getContextClassLoader());
        Fory fory = tuple2.f0.get();
        ForyCheckerListener checkerListener = getCheckerListener(url);
        fory.getClassResolver().setTypeChecker(checkerListener.getChecker());
        return new ForyObjectInput(fory, tuple2.f1, input);
    }

    private static ForyCheckerListener getCheckerListener(URL url) {
        return Optional.ofNullable(url)
                .map(URL::getOrDefaultFrameworkModel)
                .orElseGet(FrameworkModel::defaultModel)
                .getBeanFactory()
                .getBean(ForyCheckerListener.class);
    }
}