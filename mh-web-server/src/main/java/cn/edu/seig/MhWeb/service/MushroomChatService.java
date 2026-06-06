package cn.edu.seig.MhWeb.service;

import cn.edu.seig.MhWeb.model.dto.ChatRequest;
import cn.edu.seig.MhWeb.model.dto.ChatResponse;
import cn.edu.seig.MhWeb.model.entity.MushroomAiResult;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MushroomChatService {

    private final MushroomAiService mushroomAiService;
    private final IAiKepuService aiKepuService;

    private static final String SYSTEM_PROMPT = "你是一个专业的蘑菇科普助手，可以识别蘑菇毒性、品种，并提供详细的科普知识。整体少于200字" +
            "如果用户询问与蘑菇无关的问题，请礼貌地引导用户询问蘑菇相关问题或上传蘑菇图片。";

    private static final double CONFIDENCE_THRESHOLD = 0.7;
        
    public ChatResponse processChat(ChatRequest request) {
        MultipartFile imageFile = request.getImage();
        String userText = request.getMessage();
    
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(SYSTEM_PROMPT).build());
    
        // 创建返回对象
        ChatResponse response = new ChatResponse();
            
        // 图片识别逻辑
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                BufferedImage image = ImageIO.read(imageFile.getInputStream());
                MushroomAiResult aiResult = mushroomAiService.recognize(image);
                    
                // 将识别结果填充到 response 中
                response.setMushroomName(aiResult.getMushroomName());
                response.setIsPoison(aiResult.getIsPoison());
                response.setConfidence(aiResult.getConfidence());
                response.setToxicity(aiResult.getToxicity());
    
                // 可食用/条件可食不调用AI，直接返回识别结果
                if ("edible".equals(aiResult.getToxicity()) || "conditionally_edible".equals(aiResult.getToxicity())) {
                    response.setReply("");
                    return response;
                }
                
                // 有毒蘑菇才需要构建上下文并调用 AI 生成科普内容
                StringBuilder context = new StringBuilder();
                context.append("该蘑菇有毒");
                if (aiResult.getMushroomName() != null && aiResult.getConfidence() >= CONFIDENCE_THRESHOLD) {
                    context.append("，品种：").append(aiResult.getMushroomName());
                }
                messages.add(ChatMessage.builder().role(ChatMessageRole.SYSTEM).content(context.toString()).build());
    
            } catch (Exception e) {
                e.printStackTrace();
                messages.add(ChatMessage.builder().role(ChatMessageRole.SYSTEM)
                        .content("图片识别失败").build());
            }
        }
    
        // 空输入处理
        if (userText != null && !userText.trim().isEmpty()) {
            messages.add(ChatMessage.builder().role(ChatMessageRole.USER).content(userText).build());
        } else if (imageFile == null || imageFile.isEmpty()) {
            response.setReply("请上传蘑菇图片或输入您的问题。");
            return response;
        }
    
        // 调用大模型生成回复
        String reply = aiKepuService.chat(messages);
        response.setReply(reply);
        return response;
    }
}