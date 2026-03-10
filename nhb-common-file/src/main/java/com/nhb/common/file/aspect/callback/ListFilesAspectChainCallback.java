package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.pretreatment.ListFilesPretreatment;
import com.nhb.common.file.core.ListFilesResult;
import com.nhb.common.file.platform.FileStorage;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description:  列举文件切面调用链结束回调
 */
public interface ListFilesAspectChainCallback {
    ListFilesResult run(ListFilesPretreatment pre, FileStorage fileStorage);
}
