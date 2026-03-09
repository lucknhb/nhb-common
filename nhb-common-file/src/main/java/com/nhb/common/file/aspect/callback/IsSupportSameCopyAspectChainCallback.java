package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.platform.FileStorage;

/**
 * 是否支持同存储平台复制的切面调用链结束回调
 */
public interface IsSupportSameCopyAspectChainCallback {
    boolean run(FileStorage fileStorage);
}
