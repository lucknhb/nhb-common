package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.core.FilePartInfoList;
import com.nhb.common.file.pretreatment.ListPartsPretreatment;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description:  手动分片上传-列举已上传的分片切面调用链结束回调
 */
public interface ListPartsAspectChainCallback {
    FilePartInfoList run(ListPartsPretreatment pre, FileStorage fileStorage);
}
