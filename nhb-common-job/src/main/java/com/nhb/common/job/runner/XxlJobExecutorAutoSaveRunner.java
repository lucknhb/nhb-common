package com.nhb.common.job.runner;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.nhb.common.core.exception.ServiceException;
import com.nhb.common.job.properties.XxlJobAdminConfigProperties;
import com.nhb.common.job.properties.XxlJobConfigProperties;
import com.nhb.common.job.utils.XxlJobUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/11 10:01
 * @description:
 */
@Slf4j
public class XxlJobExecutorAutoSaveRunner implements ApplicationRunner {
    private final XxlJobConfigProperties xxlJobConfigProperties;
    private final ObjectMapper objectMapper;

    public XxlJobExecutorAutoSaveRunner(XxlJobConfigProperties xxlJobConfigProperties, ObjectMapper objectMapper) {
        this.xxlJobConfigProperties = xxlJobConfigProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        XxlJobAdminConfigProperties xxlJobAdminProperties = xxlJobConfigProperties.getAdmin();
        if (StrUtil.isBlank(xxlJobAdminProperties.getLoginUri()) && StrUtil.isBlank(xxlJobAdminProperties.getSaveUri())) {
            log.error(">>>>>>>>> XXL_JOB SETTING MISSING loginUri OR saveUri !!! <<<<<<<<<<");
            return;
        }
        String userName = xxlJobAdminProperties.getUserName();
        String password = xxlJobAdminProperties.getPassword();
        if (StrUtil.isBlank(userName) && StrUtil.isBlank(password)) {
            log.error(">>>>>>>>> XXL_JOB SETTING MISSING userName OR password !!! <<<<<<<<<<");
            return;
        }
        //登录XXL_JOB 调度中心
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userName", userName);
        paramMap.put("password", password);
        try (HttpResponse httpResponse = HttpRequest.post(xxlJobAdminProperties.getAddress() + xxlJobConfigProperties.getAdmin().getLoginUri())
                .form(paramMap)
                .timeout(15000)
                .execute()) {
            int status = httpResponse.getStatus();
            Assert.isTrue(200 == status, "XXL_JOB 登录失败,请检查用户名密码是否正确");
            String body = httpResponse.body();
            //{"code":200,"data":null,"msg":"Success","success":true}
            JSONObject result = new JSONObject(body);
            Assert.isTrue(200 == result.getInt("code"), "XXL_JOB 登录失败,请检查用户名密码是否正确");
            List<HttpCookie> cookies = httpResponse.getCookies();
            Integer groupId = findGroupByAppName(xxlJobConfigProperties.getExecutor().getAppName(), cookies);
            //为空的情况下 进行保存
            if (Objects.isNull(groupId)) {
                saveExecutor(cookies);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 保存执行器
     */
    public boolean saveExecutor(List<HttpCookie> cookies) throws JsonProcessingException {
        HttpResponse response = this.getRequest(cookies)
                .method(Method.POST)
                .setUrl(xxlJobConfigProperties.getAdmin().getAddress() + xxlJobConfigProperties.getAdmin().getSaveUri())
                .form(new HashMap<String, Object>() {{
                    this.put("addressType", "0");
                    this.put("title", StrUtil.isBlank(xxlJobConfigProperties.getExecutor().getAppTitle()) ? StrUtil.subPre(xxlJobConfigProperties.getExecutor().getAppName(), 12) : xxlJobConfigProperties.getExecutor().getAppTitle());
                    this.put("appname", xxlJobConfigProperties.getExecutor().getAppName());
                }})
                .execute();
        String body = response.body();
        JsonNode result = objectMapper.readTree(body);
        boolean code = "200".equals(result.get("code").asText());
        if (!code) {
            log.error(">>>>> ERROR: SAVE EXECUTOR GROUP NOT SUCCESS <<<<<");
        }
        return code;
    }

    /**
     * 判断是否已存在执行器
     *
     * @return
     */
    public Integer findGroupByAppName(String appName, List<HttpCookie> cookies) {
        HttpResponse response = this.getRequest(cookies)
                .method(Method.POST)
                .setUrl(xxlJobConfigProperties.getAdmin().getAddress() + xxlJobConfigProperties.getAdmin().getGroupUri())
                .form(new HashMap<String, Object>() {{
                    this.put("appname", appName);
                }})
                .execute();
        String body = response.body();
        String version = XxlJobUtil.getVersion();
        if (version.startsWith("2")) {
            try {
                JsonNode result = objectMapper.readTree(body);
                JsonNode data = result.get("data");
                if (data.isArray()) {
                    ArrayNode arrayNode = (ArrayNode) data;
                    for (int i = 0; i < arrayNode.size(); i++) {
                        JsonNode jsonNode = arrayNode.get(i);
                        if (appName.equals(jsonNode.get("appname").asText())) {
                            return jsonNode.get("id").asInt();
                        }
                    }
                }
            } catch (Exception e) {
                throw new ServiceException("[XXL-JOB] Find Group By AppName Error for VERSION[{}]", version, e);
            }
        } else if (XxlJobUtil.getVersion().startsWith("3")) {
            try {
                JsonNode result = objectMapper.readTree(body);
                JsonNode data = result.get("data");
                ArrayNode dataArrays = (ArrayNode)data.get("data");
                for (int i = 0; i < dataArrays.size(); i++) {
                    JsonNode dataNode = dataArrays.get(i);
                    if (appName.equals(dataNode.get("appname").asText())) {
                        return dataNode.get("id").asInt();
                    }
                }
            } catch (Exception e) {
                throw new ServiceException("[XXL-JOB] Find Group By AppName Error for VERSION[{}]", version, e);
            }
        }
        return null;
    }

    /**
     * 初始化请求
     *
     * @return
     */
    private HttpRequest getRequest(List<HttpCookie> cookies) {
        HttpRequest request = HttpRequest.of(xxlJobConfigProperties.getAdmin().getAddress());
        request.cookie(cookies);
        request.header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        return request;
    }
}
