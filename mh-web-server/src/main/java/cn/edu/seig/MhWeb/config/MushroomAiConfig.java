package cn.edu.seig.MhWeb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 蘑菇AI识别配置类
 * 读取 application.yml 中的 ai.mushroom 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.mushroom")
public class MushroomAiConfig {
    private String pythonPath;
    private String scriptPath;
    private String toxicityModelPath;
    private String speciesModelPath;
    private double confidenceThreshold = 0.7;

    // getters and setters
    public String getPythonPath() { return pythonPath; }
    public void setPythonPath(String pythonPath) { this.pythonPath = pythonPath; }
    public String getScriptPath() { return scriptPath; }
    public void setScriptPath(String scriptPath) { this.scriptPath = scriptPath; }
    public String getToxicityModelPath() { return toxicityModelPath; }
    public void setToxicityModelPath(String toxicityModelPath) { this.toxicityModelPath = toxicityModelPath; }
    public String getSpeciesModelPath() { return speciesModelPath; }
    public void setSpeciesModelPath(String speciesModelPath) { this.speciesModelPath = speciesModelPath; }
    public double getConfidenceThreshold() { return confidenceThreshold; }
    public void setConfidenceThreshold(double confidenceThreshold) { this.confidenceThreshold = confidenceThreshold; }
}