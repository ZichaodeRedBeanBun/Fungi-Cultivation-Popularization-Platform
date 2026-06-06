package cn.edu.seig.MhWeb.controller.user;


import cn.edu.seig.MhWeb.model.dto.ChatRequest;
import cn.edu.seig.MhWeb.model.dto.ChatResponse;
import cn.edu.seig.MhWeb.model.entity.MushroomAiResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IAiKepuService;
import cn.edu.seig.MhWeb.service.MushroomChatService;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import io.reactivex.Flowable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/AIChat")
public class AIChatController {
    @Autowired
    private MushroomChatService chatService;
    @PostMapping(value = "/chat", consumes = {"multipart/form-data"})
    public Result<ChatResponse> chat(
            @RequestPart(value = "message", required = false) String message,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        request.setImage(image);

        ChatResponse response = chatService.processChat(request);
        return Result.success(response);
    }
}
