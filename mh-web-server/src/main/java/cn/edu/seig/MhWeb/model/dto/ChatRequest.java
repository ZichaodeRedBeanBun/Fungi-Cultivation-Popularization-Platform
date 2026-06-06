package cn.edu.seig.MhWeb.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ChatRequest {
    private String message;            // 用户输入的文字
    private MultipartFile image;        // 可选的图片
}