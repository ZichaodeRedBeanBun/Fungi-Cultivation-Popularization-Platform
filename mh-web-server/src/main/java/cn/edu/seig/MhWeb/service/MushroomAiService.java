package cn.edu.seig.MhWeb.service;

import cn.edu.seig.MhWeb.config.MushroomAiConfig;
import cn.edu.seig.MhWeb.model.entity.MushroomAiResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Map;

@Service
public class MushroomAiService {

    private final MushroomAiConfig aiConfig;

    private final String[] toxicityClasses;
    private final String[] speciesClasses;

    public MushroomAiService(MushroomAiConfig aiConfig) throws Exception {
        this.aiConfig = aiConfig;

        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = getClass().getResourceAsStream("/class_names.json")) {
            toxicityClasses = mapper.readValue(is, String[].class);
        }
        try (InputStream is = getClass().getResourceAsStream("/toxic_species_classes.json")) {
            speciesClasses = mapper.readValue(is, String[].class);
        }
    }

    /**
     * 图像识别（调用Python脚本）
     */
    public MushroomAiResult recognize(BufferedImage image) throws Exception {
        File temp = null;
        try {
            temp = File.createTempFile("mushroom_", ".png");
            ImageIO.write(image, "png", temp);

            Process process = new ProcessBuilder(
                    aiConfig.getPythonPath(),
                    aiConfig.getScriptPath(),
                    temp.getAbsolutePath(),
                    aiConfig.getToxicityModelPath(),
                    aiConfig.getSpeciesModelPath()
            ).start();

            String jsonOutput = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Python 脚本执行失败，退出码: " + exitCode + "，输出: " + jsonOutput);
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> resultMap = mapper.readValue(jsonOutput, Map.class);
            if (resultMap.containsKey("error")) {
                throw new RuntimeException("Python 错误: " + resultMap.get("error"));
            }

            String toxicity = (String) resultMap.get("toxicity");
            double toxConf = (double) resultMap.get("toxicity_conf");
            String species = (String) resultMap.get("species");
            double speciesConf = (double) resultMap.get("species_conf");

            MushroomAiResult result = new MushroomAiResult();
            result.setIsPoison("poisonous".equals(toxicity) ? 1 : 0);
            result.setToxicity(toxicity);
            result.setConfidence(speciesConf);
            if (species != null && speciesConf >= aiConfig.getConfidenceThreshold()) {
                result.setMushroomName(species);
            } else {
                result.setMushroomName(null);
            }
            return result;
        } finally {
            if (temp != null && temp.exists()) {
                temp.delete();
            }
        }
    }

    /**
     * 将毒性英文标签映射为中文显示名称（可选）
     */
    private String mapToxicityToChinese(String toxicity) {
        switch (toxicity) {
            case "poisonous":
                return "有毒蘑菇";
            case "edible":
                return "可食用蘑菇";
            case "conditionally_edible":
                return "条件可食蘑菇";
            default:
                return toxicity;
        }
    }

}