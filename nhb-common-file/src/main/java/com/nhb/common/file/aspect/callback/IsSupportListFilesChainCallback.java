package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.core.ListFilesSupportInfo;
import com.nhb.common.file.platform.FileStorage;

/**
 * 是否支持手动分片上传的切面调用链结束回调
 */
public interface IsSupportListFilesChainCallback {
    ListFilesSupportInfo run(FileStorage fileStorage);
}
