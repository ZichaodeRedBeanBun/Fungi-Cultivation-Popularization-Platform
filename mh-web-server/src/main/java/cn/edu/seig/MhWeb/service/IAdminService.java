package cn.edu.seig.MhWeb.service;

import cn.edu.seig.MhWeb.model.dto.admin.AdminDTO;
import cn.edu.seig.MhWeb.model.entity.Admin;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 服务类
 *
 * @author su
 */
public interface IAdminService extends IService<Admin> {

    // 管理员注册
    Result register(AdminDTO adminDTO);

    // 管理员登录
    Result login(AdminDTO adminDTO);

    // 退出登录
    Result logout(String token);
}
