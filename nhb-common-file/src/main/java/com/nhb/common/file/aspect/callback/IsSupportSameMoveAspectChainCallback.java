package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.platform.FileStorage;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description:  是否支持同存储平台移动的切面调用链结束回调
 */
public interface IsSupportSameMoveAspectChainCallback {
    boolean run(FileStorage fileStorage);
}
