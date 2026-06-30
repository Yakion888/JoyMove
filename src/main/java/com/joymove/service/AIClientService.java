package com.joymove.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * DeepSeek API 客户端
 */
@Slf4j
@Service
public class AIClientService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${deepseek.api.key:sk-your-key-here}")
    private String apiKey;

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    /**
     * 调用 DeepSeek 对话接口
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @return AI 响应文本，失败返回 null
     */
    public String chat(String systemPrompt, String userMessage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "deepseek-chat");
            body.put("temperature", 0.4);
            body.put("max_tokens", 1024);
            Map<String, String> format = new HashMap<>();
            format.put("type", "json_object");
            body.put("response_format", format);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system"); sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);
            Map<String, String> usrMsg = new HashMap<>();
            usrMsg.put("role", "user"); usrMsg.put("content", userMessage);
            messages.add(usrMsg);
            body.put("messages", messages);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            log.warn("DeepSeek API 调用失败: {}", e.getMessage());
        }
        return null;
    }
}
