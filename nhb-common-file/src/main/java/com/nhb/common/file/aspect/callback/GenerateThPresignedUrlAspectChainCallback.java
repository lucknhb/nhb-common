package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;

import java.util.Date;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 对缩略图文件生成可以签名访问的 URL 切面调用链结束回调
 */
public interface GenerateThPresignedUrlAspectChainCallback {
    String run(FileInfo fileInfo, Date expiration, FileStorage fileStorage);
}
