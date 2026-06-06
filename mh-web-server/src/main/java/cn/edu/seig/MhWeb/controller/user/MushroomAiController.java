//package cn.edu.seig.MhWeb.controller.user;
//
//import cn.edu.seig.MhWeb.model.entity.MushroomAiResult;
//import cn.edu.seig.MhWeb.result.Result;
//import cn.edu.seig.MhWeb.service.MushroomAiService;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.File;
//
//@RestController
//@RequestMapping("/user/mushroomAI")
//public class MushroomAiController {
//    // TODO 可删 单独识别的
//    @Autowired
//    private MushroomAiService mushroomAiService;
//    @PostMapping("/recognize")
//    public Result<MushroomAiResult> recognize(
//            @RequestParam("file") MultipartFile file) {  // 必须上传图片
//        try {
//            if (file == null || file.isEmpty()) {
//                return Result.error("请上传图片");
//            }
//            BufferedImage image = ImageIO.read(file.getInputStream());
//            if (image == null) {
//                return Result.error("图片格式错误，仅支持 JPG/PNG");
//            }
//            // 调用图像识别方法（需要在 MushroomAiService 中实现 recognize 方法）
//            MushroomAiResult result = mushroomAiService.recognize(image);
//            return Result.success(result);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Result.error("识别失败：" + e.getMessage());
//        }
//    }
//}