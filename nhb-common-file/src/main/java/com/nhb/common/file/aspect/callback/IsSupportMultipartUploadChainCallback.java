package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.core.MultipartUploadSupportInfo;

/**
 * 是否支持手动分片上传的切面调用链结束回调
 */
public interface IsSupportMultipartUploadChainCallback {
    MultipartUploadSupportInfo run(FileStorage fileStorage);
}
