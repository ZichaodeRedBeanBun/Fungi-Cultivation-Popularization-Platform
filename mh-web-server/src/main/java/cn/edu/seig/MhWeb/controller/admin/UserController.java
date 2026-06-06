package cn.edu.seig.MhWeb.controller.admin;

import cn.edu.seig.MhWeb.model.dto.user.UserAddDTO;
import cn.edu.seig.MhWeb.model.dto.user.UserAdminUpdateDTO;
import cn.edu.seig.MhWeb.model.dto.user.UserSearchDTO;
import cn.edu.seig.MhWeb.model.vo.UserManagementVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IUserService;
import cn.edu.seig.MhWeb.util.BindingResultUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员-用户管理操作
 */
@RestController("adminUserController") // 手动指定Bean名
@RequestMapping("/admin/user")
public class UserController {
    @Autowired
    private IUserService userService;
//    /**
//     * 获取所有用户数量
//     *
//     * @return 用户数量
//     */
//    @GetMapping("/getAllUsersCount")
//    public Result<Long> getAllUsersCount() {
//        return userService.getAllUsersCount();
//    }

    /**
     * 获取所有用户信息
     *
     * @param userSearchDTO 用户搜索条件
     * @return 结果
     */
    @PostMapping("/getAllUsers")
    public Result<PageResult<UserManagementVO>> getAllUsers(@RequestBody UserSearchDTO userSearchDTO) {
        return userService.getAllUsers(userSearchDTO);
    }

    /**
     * 新增用户
     *
     * @param userAddDTO 用户注册信息
     * @return 结果
     */
    @PostMapping("/addUser")
    public Result addUser(@RequestBody @Valid UserAddDTO userAddDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return userService.addUser(userAddDTO);
    }

    /**
     * 更新用户信息
     *
     * @param userAdminUpdateDTO 用户信息
     * @return 结果
     */
    @PutMapping("/updateUser")
    public Result updateUser(@RequestBody @Valid UserAdminUpdateDTO userAdminUpdateDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return userService.updateUser(userAdminUpdateDTO);
    }

    /**
     * 更新用户状态
     *
     * @param userId     用户id
     * @param userStatus 用户状态
     * @return 结果
     * @PatchMapping 部分资源更新请求
     */
    @PatchMapping("/updateUserStatus/{id}/{status}")
    public Result updateUserStatus(@PathVariable("id") Long userId, @PathVariable("status") Integer userStatus) {
        return userService.updateUserStatus(userId, userStatus);
    }

    /**
     * 删除用户
     *
     * @param userId 用户id
     * @return 结果
     */
    @DeleteMapping("/deleteUser/{id}")
    public Result deleteUser(@PathVariable("id") Long userId) {
        return userService.deleteUser(userId);
    }

    /**
     * 批量删除用户
     *
     * @param userIds 用户id列表
     * @return 结果
     */
    @DeleteMapping("/deleteUsers")
    public Result deleteUsers(@RequestBody List<Long> userIds) {
        return userService.deleteUsers(userIds);
    }

}
