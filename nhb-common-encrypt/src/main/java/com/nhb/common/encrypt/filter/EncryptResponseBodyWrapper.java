package com.nhb.common.encrypt.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nhb.common.core.domain.ResultMessage;
import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.core.utils.StreamUtil;
import com.nhb.common.encrypt.utils.EncryptUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 11:34
 * @description: 返回参数进行加密处理
 */
public class EncryptResponseBodyWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream byteArrayOutputStream;
    private final ServletOutputStream servletOutputStream;
    private final PrintWriter printWriter;

    public EncryptResponseBodyWrapper(HttpServletResponse response) throws IOException {
        super(response);
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.servletOutputStream = this.getOutputStream();
        this.printWriter = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream));
    }

    @Override
    public PrintWriter getWriter() {
        return printWriter;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (servletOutputStream != null) {
            servletOutputStream.flush();
        }
        if (printWriter != null) {
            printWriter.flush();
        }
    }

    @Override
    public void reset() {
        byteArrayOutputStream.reset();
    }

    public byte[] getResponseData() throws IOException {
        flushBuffer();
        return byteArrayOutputStream.toByteArray();
    }

    public String getContent() throws IOException {
        flushBuffer();
        return byteArrayOutputStream.toString();
    }

    /**
     * 获取加密内容
     *
     * @param servletResponse response
     * @param publicKey       RSA公钥 (用于加密 AES 秘钥)
     * @param headerFlag      请求头标志
     * @return 加密内容
     * @throws IOException
     */
    public String getEncryptContent(HttpServletResponse servletResponse, String publicKey, String headerFlag) throws IOException, NoSuchAlgorithmException {
        //随机生成AES秘钥及向量
        Map<String, byte[]> keyMap = EncryptUtil.generateAesKey();
        byte[] aesKey = keyMap.get(EncryptUtil.PASSWORD);
        byte[] aesIv = keyMap.get(EncryptUtil.SALT);
        //将秘钥和向量进行RSA加密 且 加密结果为BASE64编码后的值
        // Rsa 公钥加密 AES秘钥及向量 且值为 加密后并base64编码后 通过 : 拼接的值
        String headerValue = EncryptUtil.aesByRsaBase64Header(aesKey,aesIv,publicKey);
        // 设置响应头
        servletResponse.setHeader(headerFlag, headerValue);
        // 获取原始内容
        String originalBody = this.getContent();
        //仅对有用数据进行加密
        boolean jsonFlag = JacksonUtil.isJson(originalBody);
        if (jsonFlag) {
            ObjectNode jsonNode = (ObjectNode) JacksonUtil.getObjectMapper().readTree(originalBody);
            String fieldName = StreamUtil.getFieldName(ResultMessage<String>::getData);
            JsonNode data = jsonNode.get(fieldName);
            if (!data.isNull()) {
                String text = data.asText();
                //JSON数据的话 仅对data属性值进行加密
                jsonNode.put(fieldName, EncryptUtil.encryptByAesBase64(text, aesKey, aesIv));
                return JacksonUtil.toJsonString(jsonNode);
            }
        }
        // 非JSON数据对整体加密
        return EncryptUtil.encryptByAesBase64(originalBody, aesKey, aesIv);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }

            @Override
            public void write(int b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                byteArrayOutputStream.write(b, off, len);
            }
        };
    }
}
