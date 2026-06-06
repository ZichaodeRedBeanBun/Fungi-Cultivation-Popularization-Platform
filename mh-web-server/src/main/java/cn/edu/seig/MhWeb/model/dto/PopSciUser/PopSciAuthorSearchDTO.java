package cn.edu.seig.MhWeb.model.dto.PopSciUser;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 科普号搜索查询DTO
 */
@Data
public class PopSciAuthorSearchDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码（必填）
     */
    private Integer pageNum = 1;

    /**
     * 每页条数（必填）
     */
    private Integer pageSize = 10;

    /**
     * 科普号名称（模糊查询，可选）
     */
    private String username;

    /**
     * 科普号ID（模糊查询，可选）
     */
    private Long userId;
}