package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.platform.FileStorage;

/**
 * 是否支持 Metadata 切面调用链结束回调
 */
public interface IsSupportMetadataAspectChainCallback {
    boolean run(FileStorage fileStorage);
}
