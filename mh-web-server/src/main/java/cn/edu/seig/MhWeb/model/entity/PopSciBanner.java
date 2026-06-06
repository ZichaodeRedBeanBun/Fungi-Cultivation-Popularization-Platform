package cn.edu.seig.MhWeb.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mh_popsci_banner")
public class PopSciBanner implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id; // 轮播图ID
    private Long contentId; // 关联科普内容ID
    @TableField("sort")
    private Integer sort; // 排序优先级
    private Integer status; // 状态：1-启用，0-禁用
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    // 在PopSciBanner类中添加
}