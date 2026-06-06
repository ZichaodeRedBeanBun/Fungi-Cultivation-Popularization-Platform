package cn.edu.seig.MhWeb.service;

import cn.edu.seig.MhWeb.model.entity.Picture;
import cn.edu.seig.MhWeb.model.entity.User;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IPictureService extends IService<Picture> {

    Result<Long> getPicIdByUrl(Long mushroomId, String picUrl);
}
