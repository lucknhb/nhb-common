package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.pretreatment.GeneratePresignedUrlPretreatment;
import com.nhb.common.file.core.GeneratePresignedUrlResult;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 对文件生成可以签名访问的 URL 切面调用链结束回调
 */
public interface GeneratePresignedUrlAspectChainCallback {
    GeneratePresignedUrlResult run(GeneratePresignedUrlPretreatment pre, FileStorage fileStorage);
}
