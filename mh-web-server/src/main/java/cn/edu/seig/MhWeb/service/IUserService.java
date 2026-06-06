package cn.edu.seig.MhWeb.service;


import cn.edu.seig.MhWeb.model.dto.PopSciUser.PopsciApplyDTO;
import cn.edu.seig.MhWeb.model.dto.user.*;
import cn.edu.seig.MhWeb.model.entity.User;
import cn.edu.seig.MhWeb.model.vo.UserManagementVO;
import cn.edu.seig.MhWeb.model.vo.UserVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IUserService extends IService<User> {

    // 发送验证码
    Result sendVerificationCode(String email);

    // 验证验证码
    boolean verifyVerificationCode(String email, String verificationCode);

    // 用户注册
    Result register(UserRegisterDTO userRegisterDTO);

    // 用户登录
    Result login(UserLoginDTO userLoginDTO);

    // 用户信息
    Result<UserVO> userInfo();

    // 更新用户信息
    Result updateUserInfo(UserSelfUpdateDTO userSelfUpdateDTO);

    // 更新用户头像
    Result updateUserAvatar(String avatarUrl);

    // 更新用户密码
    Result updateUserPassword(UserPasswordDTO userPasswordDTO, String token);

    // 重置用户密码
    Result resetUserPassword(UserResetPasswordDTO userResetPasswordDTO);

    // 退出登录
    Result logout(String token);

    // 注销账号
    Result deleteAccount();

    // 获取所有用户数量
    Result<Long> getAllUsersCount(Integer user_type);

    // 获取所有用户
    Result<PageResult<UserManagementVO>> getAllUsers(UserSearchDTO userSearchDTO);

    // 添加用户
    Result addUser(UserAddDTO userAddDTO);

    // 更新用户
    Result updateUser(UserAdminUpdateDTO userDTO);

    // 更新用户状态
    Result updateUserStatus(Long userId, Integer userStatus);

    // 删除用户
    Result deleteUser(Long userId);

    // 批量删除用户
    Result deleteUsers(List<Long> userIds);


    Result<UserManagementVO> getUserById(Long userId);
}
