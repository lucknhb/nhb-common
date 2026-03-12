package com.nhb.common.file.actuator;


import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.chain.GetFileAspectChain;
import com.nhb.common.file.core.FileStorageService;
import com.nhb.common.file.exception.ExceptionCheck;
import com.nhb.common.file.pretreatment.GetFilePretreatment;
import com.nhb.common.file.core.RemoteFileInfo;
import com.nhb.common.file.platform.FileStorage;

import java.util.HashMap;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 获取文件执行器
 */
public class GetFileActuator {
    private final FileStorageService fileStorageService;
    private final GetFilePretreatment pre;

    public GetFileActuator(GetFilePretreatment pre) {
        this.pre = pre;
        this.fileStorageService = pre.getFileStorageService();
    }

    /**
     * 执行获取文件
     */
    public RemoteFileInfo execute() {
        return execute(fileStorageService.getFileStorageVerify(pre.getPlatform()), fileStorageService.getAspectList());
    }

    /**
     * 执行获取文件
     */
    public RemoteFileInfo execute(FileStorage fileStorage, List<FileStorageAspect> aspectList) {
        ExceptionCheck.getFile(pre);
        return new GetFileAspectChain(aspectList, (_pre, _fileStorage) -> {
                    RemoteFileInfo info = _fileStorage.getFile(_pre);
                    if (info != null) {
                        if (info.getMetadata() == null) info.setMetadata(new HashMap<>());
                        if (info.getUserMetadata() == null) info.setUserMetadata(new HashMap<>());
                    }
                    return info;
                })
                .next(pre, fileStorage);
    }
}
