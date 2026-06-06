package cn.edu.seig.MhWeb.service.impl;

import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.mapper.PictureMapper;
import cn.edu.seig.MhWeb.model.entity.Picture;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPictureService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements IPictureService {

    @Override
    public Result<Long> getPicIdByUrl(Long mushroomId, String picUrl) {
        // 1. 校验参数
        if (mushroomId == null || picUrl == null || picUrl.trim().isEmpty()) {
            return Result.error(MessageConstant.MUSHROOM_IDS+MessageConstant.PIC_URL+MessageConstant.NOT_NULL); // 替换为你的参数错误常量
        }
        // 2. 构建查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mushroom_id", mushroomId)
                    .eq("pic_url", picUrl.trim());
        // 3. 查询图片
        Picture picture = this.baseMapper.selectOne(queryWrapper);
        // 4. 结果处理
        if (picture == null) {
            return Result.error(MessageConstant.PIC_URL + MessageConstant.NOT_EXIST);
        }
        return Result.success(picture.getId());
    }
}