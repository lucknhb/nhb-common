package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.core.ListFilesSupportInfo;
import com.nhb.common.file.platform.FileStorage;


/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 是否支持手动分片上传的切面调用链结束回调
 */
public interface IsSupportListFilesChainCallback {
    ListFilesSupportInfo run(FileStorage fileStorage);
}
