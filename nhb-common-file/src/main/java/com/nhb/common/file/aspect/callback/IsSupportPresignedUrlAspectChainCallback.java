package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.platform.FileStorage;

/**
 * 是否支持对文件生成可以签名访问的 URL 切面调用链结束回调
 */
public interface IsSupportPresignedUrlAspectChainCallback {
    boolean run(FileStorage fileStorage);
}
