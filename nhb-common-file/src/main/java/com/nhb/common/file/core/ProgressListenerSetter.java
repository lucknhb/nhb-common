package com.nhb.common.file.core;

import com.nhb.common.file.utils.ToolUtil;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 16:37
 * @description:
 */
public interface ProgressListenerSetter <T extends ProgressListenerSetter<?>>{

    /**
     * 设置进度监听器
     *
     * @param progressListener 提供一个参数，表示已传输字节数
     */
    default T setProgressListener(boolean flag, Consumer<Long> progressListener) {
        if (flag) {
            setProgressListener(progressListener);
        }
        return ToolUtil.cast(this);
    }

    /**
     * 设置进度监听器
     *
     * @param progressListener 提供一个参数，表示已传输字节数
     */
    default T setProgressListener(Consumer<Long> progressListener) {
        return setProgressListener((progressSize, allSize) -> progressListener.accept(progressSize));
    }

    /**
     * 设置进度监听器
     *
     * @param progressListener 提供两个参数，第一个是 progressSize已传输字节数，第二个是 allSize总字节数
     */
    default T setProgressListener(boolean flag, BiConsumer<Long, Long> progressListener) {
        if (flag) setProgressListener(progressListener);
        return ToolUtil.cast(this);
    }

    /**
     * 设置进度监听器
     *
     * @param progressListener 提供两个参数，第一个是 progressSize已传输字节数，第二个是 allSize总字节数
     */
    default T setProgressListener(BiConsumer<Long, Long> progressListener) {
        return setProgressListener(new ProgressListener() {
            @Override
            public void start() {}

            @Override
            public void progress(long progressSize, Long allSize) {
                progressListener.accept(progressSize, allSize);
            }

            @Override
            public void finish() {}
        });
    }

    /**
     * 设置进度监听器
     */
    default T setProgressListener(boolean flag, ProgressListener progressListener) {
        if (flag) {
            setProgressListener(progressListener);
        }
        return ToolUtil.cast(this);
    }

    /**
     * 设置进度监听器
     */
    T setProgressListener(ProgressListener progressListener);
}
