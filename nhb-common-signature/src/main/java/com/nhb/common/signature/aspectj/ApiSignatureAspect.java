package com.nhb.common.signature.aspectj;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.core.utils.MapUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.signature.annotation.ApiSign;
import com.nhb.common.signature.core.ApiSignRepository;
import com.nhb.common.signature.utils.RequestBodyHolder;
import com.nhb.common.signature.utils.SignatureUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 16:37
 * @description 签名的核心是基于「哈希摘要算法」+「非对称加密算法」的组合<BR/>
 * 完整的流程分为签名生成与签名校验两个环节：
 * 签名生成（客户端）：<BR/>
 * 1：将所有请求参数按照约定规则整理，生成规范的参数字符串<BR/>
 * 2：通过哈希算法对参数字符串生成固定长度的摘要，摘要具有唯一性：只要参数有任何一点改动，生成的摘要都会完全不同<BR/>
 * 3：客户端用自己的私钥对摘要进行加密，生成最终的签名sign
 * 步骤4：将sign放入请求头或请求参数中，随请求一起发送给服务端签名校验
 * （服务端）：<BR/>
 * 1：接收到请求后，提取请求中的sign字段，以及所有业务参数<BR/>
 * 2：按照和客户端完全一致的规则，整理业务参数，生成参数字符串<BR/>
 * 3：用同样的哈希算法生成参数字符串的摘要<BR/>
 * 4：用客户端的公钥对收到的sign进行解密，得到客户端生成的摘要<BR/>
 * 5：对比两个摘要是否完全一致，一致则签名校验通过，否则拒绝请求
 */
@Slf4j
@Aspect
public class ApiSignatureAspect {


    /**
     * 获取请求参数
     *
     * @param request request
     * @return 参数键值对
     * @throws IOException 抛出异常
     */
    private static Map<String, Object> getParameterMap(HttpServletRequest request) throws IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (CollUtil.isNotEmpty(parameterMap)) {
            return MapUtil.getParameterMap(parameterMap).toSingleValueMap();
        }
        String requestBody = RequestBodyHolder.getBody();
        return StringUtil.isBlank(requestBody) ? Collections.emptyMap()
                : JacksonUtil.parseObject(requestBody,new TypeReference<Map<String, Object>>() {});
    }

    @Around("@annotation(apiSign)")
    public Object doAround(ProceedingJoinPoint point, ApiSign apiSign) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        Assert.notNull(requestAttributes, "RequestAttributes not be null");
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        String sign = request.getHeader(SignatureUtil.SIGN);
        String timeStamp = request.getHeader(SignatureUtil.TIMESTAMP);
        String nonce = request.getHeader(SignatureUtil.NONCE);
        String clientId = request.getHeader(SignatureUtil.CLIENT_ID);
        Assert.isTrue(StringUtil.isNotBlank(sign) && StringUtil.isNotBlank(timeStamp)
                        && StringUtil.isNotBlank(nonce) && StringUtil.isNotBlank(clientId),
                "接口签名缺失必要数据,请核实");
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - Long.parseLong(timeStamp) > apiSign.timeRange()) {
            throw new SecurityException("签名时间超时");
        }
        //获取参数
        Map<String, Object> parameterMap = getParameterMap(request);
        //手动添加请求头中的三个数据
        parameterMap.put(SignatureUtil.CLIENT_ID, clientId);
        parameterMap.put(SignatureUtil.NONCE, nonce);
        parameterMap.put(SignatureUtil.TIMESTAMP, timeStamp);
        String content = SignatureUtil.buildSignContent(parameterMap);
        ApiSignRepository apiSignRepository = SpringContextUtil.getBean(ApiSignRepository.class);
        Assert.notNull(apiSignRepository, "ApiSignRepository not null");
        String publicKey = apiSignRepository.findPublicKeyByClientId(Long.valueOf(clientId));
        boolean signFlag = SignatureUtil.verifySignature(content, sign, publicKey);
        Assert.isTrue(signFlag,"签名校验未通过");
        return point.proceed();
    }

}
