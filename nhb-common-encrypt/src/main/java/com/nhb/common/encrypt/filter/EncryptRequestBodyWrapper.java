package com.nhb.common.encrypt.filter;

import cn.hutool.core.io.IoUtil;
import com.nhb.common.encrypt.utils.EncryptUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 11:31
 * @description: 处理加密请求
 */
public class EncryptRequestBodyWrapper extends HttpServletRequestWrapper {

    private final byte[] body;

    public EncryptRequestBodyWrapper(HttpServletRequest request, String privateKey, String headerFlag) throws IOException {
        super(request);
        // 获取 AES 密码 采用 RSA 加密
        String headerRsa = request.getHeader(headerFlag);
        String decryptAes = EncryptUtil.decryptByRsa(headerRsa, privateKey);
        // 解密 AES 密码
        String aesPassword = EncryptUtil.decryptByBase64(decryptAes);

        request.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        byte[] readBytes = IoUtil.readBytes(request.getInputStream(), false);
        String requestBody = new String(readBytes, StandardCharsets.UTF_8);
        // 解密 body 采用 AES 加密
        String decryptBody = EncryptUtil.decryptByAes(requestBody, aesPassword);
        body = decryptBody.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }


    @Override
    public int getContentLength() {
        return body.length;
    }

    @Override
    public long getContentLengthLong() {
        return body.length;
    }

    @Override
    public String getContentType() {
        return MediaType.APPLICATION_JSON_VALUE;
    }


    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() {
                return bais.read();
            }

            @Override
            public int available() {
                return body.length;
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }
}
