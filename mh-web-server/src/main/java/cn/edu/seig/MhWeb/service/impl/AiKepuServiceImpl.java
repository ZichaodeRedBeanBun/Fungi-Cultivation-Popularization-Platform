package cn.edu.seig.MhWeb.service.impl;

import cn.edu.seig.MhWeb.service.IAiKepuService;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionResult;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 完全对齐官方示例的火山方舟实现类（API Key模式）
 */
@Slf4j
@Service
public class AiKepuServiceImpl implements IAiKepuService {

    @Value("${volc.ark.api-key}")
    private String apiKey;

    @Value("${volc.ark.base-url:https://ark.cn-beijing.volces.com/api/v3}")
    private String baseUrl;

    @Value("${volc.ark.model:ep-20260301181711-5gptp}")
    private String modelId;

    private ArkService arkService;

    @PostConstruct
    public void init() {
        try {
            ConnectionPool connectionPool = new ConnectionPool(10, 30, TimeUnit.SECONDS);
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(20);
            dispatcher.setMaxRequestsPerHost(10);

            Duration connectTimeout = Duration.ofSeconds(10);

            arkService = ArkService.builder()
                    .dispatcher(dispatcher)
                    .connectionPool(connectionPool)
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .connectTimeout(connectTimeout)
                    .build();
            log.info("火山方舟客户端初始化成功");
        } catch (Exception e) {
            log.error("火山方舟客户端初始化失败：", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String generateReply(String userInput, String context) {
        // 构建消息列表
        String systemPrompt = "仅回答蘑菇核心信息，总字数严格≤400字，包含：1.毒菇名字和简介 2.识别特征（3条内）3.毒性 4.安全建议（1条）。禁止分段、禁止分隔符、语言极简。";
        StringBuilder userContent = new StringBuilder();
        if (context != null && !context.trim().isEmpty()) {
            userContent.append("上下文：").append(context).append("\n");
        }
        userContent.append(userInput);

        List<ChatMessage> messages = List.of(
                ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(systemPrompt).build(),
                ChatMessage.builder().role(ChatMessageRole.USER).content(userContent.toString()).build()
        );

        return callArkModel(messages);
    }

    @Override
    public String chat(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "请输入有效的问题或上传蘑菇图片。";
        }
        return callArkModel(messages);
    }
    /**
     * 调用大模型
     */
    private String callArkModel(List<ChatMessage> messages) {
        try {
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(modelId)
                    .messages(messages)
                    .maxTokens(400)
                    .temperature(0.0)
                    .topP(0.0)
                    .stop(List.of("###", "---"))
                    .build();

            ChatCompletionResult response = arkService.createChatCompletion(request);
            if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                String reply = (String) response.getChoices().get(0).getMessage().getContent();

                if (reply != null && reply.length() > 1000) {
                    int truncateLength = Math.min(1000, reply.length());
                    reply = reply.substring(0, truncateLength).replaceAll("[，.！？；；]$", "") + "…";
                }

                log.info("大模型调用成功，回复长度：{}", reply.length());
                return reply;
            } else {
                log.warn("大模型返回空结果");
                return "抱歉，我暂时无法回答这个问题。";
            }

        } catch (Exception e) {
            log.error("大模型调用失败：", e);
            if (e.getMessage().contains("AuthenticationError")) {
                return "API Key无效，请检查配置。";
            }
            return "AI服务暂时不可用：" + e.getMessage();
        }
    }

    @PreDestroy
    public void shutdown() {
        if (arkService != null) {
            arkService.shutdownExecutor();
            log.info("火山方舟客户端已关闭");
        }
    }
}