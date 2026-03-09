package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.pretreatment.GetFilePretreatment;
import com.nhb.common.file.core.RemoteFileInfo;
import com.nhb.common.file.platform.FileStorage;

/**
 * 获取文件切面调用链结束回调
 */
public interface GetFileAspectChainCallback {
    RemoteFileInfo run(GetFilePretreatment pre, FileStorage fileStorage);
}
