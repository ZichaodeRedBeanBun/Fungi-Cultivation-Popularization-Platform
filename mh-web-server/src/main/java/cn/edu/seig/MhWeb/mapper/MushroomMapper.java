package cn.edu.seig.MhWeb.mapper;


import cn.edu.seig.MhWeb.model.entity.MushroomInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  Mapper 接口
 *
 * @author su
 */
@Mapper
public interface MushroomMapper extends BaseMapper<MushroomInfo> {

    /**
     * 根据菌种名称查询菌种信息
     * @param mushroomName 菌种名称
     * @return 菌种列表（正常应为唯一记录）
     */
    List<MushroomInfo> selectByMushroomName(@Param("mushroomName") String mushroomName);
}
