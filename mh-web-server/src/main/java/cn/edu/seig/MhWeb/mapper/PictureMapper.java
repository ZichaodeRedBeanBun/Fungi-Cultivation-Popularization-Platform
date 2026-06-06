package cn.edu.seig.MhWeb.mapper;

import cn.edu.seig.MhWeb.model.dto.PictureDTO;
import cn.edu.seig.MhWeb.model.entity.Admin;
import cn.edu.seig.MhWeb.model.entity.Picture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 *  Mapper 接口
 *
 * @author su
 */
@Mapper
public interface PictureMapper extends BaseMapper<Picture> {
    /**
     * 根据删除条件查询图片列表
     */
    List<Picture> selectByDeleteDTO(PictureDTO deletePictureDTO);

    /**
     * 根据删除条件删除图片
     */
    int deleteByDeleteDTO(PictureDTO deletePictureDTO);
}
