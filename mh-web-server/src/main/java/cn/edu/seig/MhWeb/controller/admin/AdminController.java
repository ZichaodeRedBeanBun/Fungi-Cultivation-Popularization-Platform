package cn.edu.seig.MhWeb.controller.admin;

import cn.edu.seig.MhWeb.model.dto.admin.AdminDTO;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IAdminService;
import cn.edu.seig.MhWeb.util.BindingResultUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员-管理账号操作
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private IAdminService adminService;
    /**
     * 注册管理员
     *
     * @param adminDTO      管理员信息
     * @param bindingResult 绑定结果
     * @return 结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody @Valid AdminDTO adminDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return adminService.register(adminDTO);
    }

    /**
     * 登录管理员
     *
     * @param adminDTO      管理员信息
     * @param bindingResult 绑定结果
     * @return 结果
     */
    @PostMapping("/login")
    public Result login(@RequestBody @Valid AdminDTO adminDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return adminService.login(adminDTO);
    }

    /**
     * 登出
     *
     * @param token 认证token
     * @return 结果
     */
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        return adminService.logout(token);
    }


}
