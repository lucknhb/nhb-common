package com.nhb.common.dubbo.codec;

import lombok.Getter;
import org.apache.dubbo.common.utils.AllowClassNotifyListener;
import org.apache.dubbo.common.utils.SerializeCheckStatus;
import org.apache.dubbo.common.utils.SerializeSecurityManager;
import org.apache.dubbo.rpc.model.FrameworkModel;
import org.apache.fory.Fory;
import org.apache.fory.exception.InsecureException;
import org.apache.fory.resolver.AllowListChecker;
import org.apache.fory.serializer.Serializer;
import org.apache.fory.serializer.SerializerFactory;

import java.io.Serializable;
import java.util.Set;

@Getter
@SuppressWarnings("rawtypes")
public class ForyCheckerListener implements AllowClassNotifyListener, SerializerFactory {
    private final AllowListChecker checker;
    private volatile boolean checkSerializable;

    public ForyCheckerListener(FrameworkModel frameworkModel) {
        checker = new AllowListChecker();
        SerializeSecurityManager securityManager = frameworkModel.getBeanFactory().getOrRegisterBean(SerializeSecurityManager.class);
        securityManager.registerListener(this);
    }

    @Override
    public void notifyPrefix(Set<String> allowedList, Set<String> disAllowedList) {
        for (String prefix : allowedList) {
            checker.allowClass(prefix + "*");
        }
        for (String prefix : disAllowedList) {
            checker.disallowClass(prefix + "*");
        }
    }

    @Override
    public void notifyCheckStatus(SerializeCheckStatus status) {
        switch (status) {
            case DISABLE:
                checker.setCheckLevel(AllowListChecker.CheckLevel.DISABLE);
                return;
            case WARN:
                checker.setCheckLevel(AllowListChecker.CheckLevel.WARN);
                return;
            case STRICT:
                checker.setCheckLevel(AllowListChecker.CheckLevel.STRICT);
                return;
            default:
                throw new UnsupportedOperationException("Unsupported check level " + status);
        }
    }

    @Override
    public void notifyCheckSerializable(boolean checkSerializable) {
        this.checkSerializable = checkSerializable;
    }

    @Override
    public Serializer createSerializer(Fory fory, Class<?> cls) {
        if (checkSerializable && !Serializable.class.isAssignableFrom(cls)) {
            throw new InsecureException(String.format("%s is not Serializable", cls));
        }
        return null;
    }

}