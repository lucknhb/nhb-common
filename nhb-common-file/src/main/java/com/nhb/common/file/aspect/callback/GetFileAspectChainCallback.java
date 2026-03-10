package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.pretreatment.GetFilePretreatment;
import com.nhb.common.file.core.RemoteFileInfo;
import com.nhb.common.file.platform.FileStorage;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 获取文件切面调用链结束回调
 */
public interface GetFileAspectChainCallback {
    RemoteFileInfo run(GetFilePretreatment pre, FileStorage fileStorage);
}
