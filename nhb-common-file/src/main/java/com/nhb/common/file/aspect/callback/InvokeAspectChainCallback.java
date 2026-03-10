package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.platform.FileStorage;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 通过反射调用指定存储平台的方法的切面调用链结束回调
 */
public interface InvokeAspectChainCallback {
    <T> T run(FileStorage fileStorage, String method, Object[] args);
}
