package cn.edu.seig.MhWeb.service;

import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public interface IAiKepuService {
    /**
     * 生成回复内容
     * @param userInput 用户输入的文字
     * @param context   额外的上下文信息（如图像识别结果），可为空
     * @return 模型生成的回复
     */
    String generateReply(String userInput, String context);
    /**
     * 直接聊天，传入消息列表，返回模型回复
     */
    String chat(List<ChatMessage> messages);

}
//    /**
//     * 根据用户输入生成回复
//     * @param userInput 用户输入的文本（可能是蘑菇名称、问题等）
//     * @param context 额外的上下文，如毒性信息，可以为空
//     * @return 生成的回复文本
//     */
//    String generateReply(String userInput, String context);
