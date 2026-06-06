package cn.edu.seig.MhWeb.controller.user;


import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.model.dto.PopSciUser.PopsciApplyDTO;
import cn.edu.seig.MhWeb.model.dto.user.*;
import cn.edu.seig.MhWeb.model.dto.user.*;
import cn.edu.seig.MhWeb.model.vo.UserManagementVO;
import cn.edu.seig.MhWeb.model.vo.UserVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IFeedbackService;
import cn.edu.seig.MhWeb.service.IPopSciAuthorService;
import cn.edu.seig.MhWeb.service.IUserService;
import cn.edu.seig.MhWeb.service.MinioService;
import cn.edu.seig.MhWeb.util.BindingResultUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 前端控制器
 *
 * @author su
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private MinioService minioService;
    @Autowired
    private IPopSciAuthorService popSciAuthorService;
    @Autowired
    private IFeedbackService feedbackService;

    /**
     * 发送验证码
     *
     * @param email 邮箱
     * @return 结果
     */
    @GetMapping("/sendVerificationCode")
    public Result sendVerificationCode(@RequestParam @Email String email) {//验证 email 参数是否符合邮箱格式
        log.info("发送验证码 to {}",email);
        return userService.sendVerificationCode(email);
    }

    /**
     * 注册
     *
     * @param userRegisterDTO 用户注册信息
     * @param bindingResult   绑定结果
     * @return 结果
     */
//    @Valid：触发对UserRegisterDTO对象中定义的校验注解@NotBlank、@Pattern的验证。
    @PostMapping("/register")
    public Result register(@RequestBody @Valid UserRegisterDTO userRegisterDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息 bindingResult：用于接收@Valid注解的校验结果
        log.info("注册用户信息：{}",userRegisterDTO);
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        // 验证验证码是否正确
        boolean isCodeValid = userService.verifyVerificationCode(userRegisterDTO.getEmail(), userRegisterDTO.getVerificationCode());
        if (!isCodeValid) {
            return Result.error(MessageConstant.VERIFICATION_CODE + MessageConstant.INVALID);
        }
        return userService.register(userRegisterDTO);
    }

    /**
     * 登录
     *
     * @param userLoginDTO  用户登录信息
     * @param bindingResult 绑定结果
     * @return 结果
     */
    @PostMapping("/login")
    public Result login(@RequestBody @Valid UserLoginDTO userLoginDTO, BindingResult bindingResult) {
        log.info("登录用户信息:{}",userLoginDTO);
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }
        return userService.login(userLoginDTO);
    }

    /**
     * 获取用户信息
     *
     * @return 结果
     */
    @GetMapping("/getUserInfo")
    public Result<UserVO> getUserInfo() {
        return userService.userInfo();
    }

    /**
     * 更新用户基础信息
     *
     * @param userSelfUpdateDTO 用户信息
     * @return 结果
     */
    @PutMapping("/updateUserInfo")
    public Result updateUserInfo(@RequestBody @Valid UserSelfUpdateDTO userSelfUpdateDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            log.info("这里错误");
            return Result.error(errorMessage);
        }

        return userService.updateUserInfo(userSelfUpdateDTO);
    }

    /**
     * 更新用户头像
     *
     * @param avatar 头像
     * @return 结果
     */
    @PatchMapping("/updateUserAvatar")
    public Result updateUserAvatar(@RequestParam("avatar") MultipartFile avatar) {
        String avatarUrl = minioService.uploadFile(avatar, "users");  // 上传到 users 目录
        return userService.updateUserAvatar(avatarUrl);
    }

    /**
     * 更新用户密码
     *
     * @param userPasswordDTO 用户密码信息
     * @param token           认证token
     * @return 结果
     */
    @PatchMapping("/updateUserPassword")
    public Result updateUserPassword(@RequestBody @Valid UserPasswordDTO userPasswordDTO,
                                     @RequestHeader("Authorization") String token, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        return userService.updateUserPassword(userPasswordDTO, token);
    }

    /**
     * 重置用户密码
     *
     * @param userResetPasswordDTO 用户密码信息
     * @return 结果
     */
    @PatchMapping("/resetUserPassword")
    public Result resetUserPassword(@RequestBody @Valid UserResetPasswordDTO userResetPasswordDTO, BindingResult bindingResult) {
        // 校验失败时，返回错误信息
        String errorMessage = BindingResultUtil.handleBindingResultErrors(bindingResult);
        if (errorMessage != null) {
            return Result.error(errorMessage);
        }

        // 验证验证码是否正确
        boolean isCodeValid = userService.verifyVerificationCode(userResetPasswordDTO.getEmail(), userResetPasswordDTO.getVerificationCode());
        if (!isCodeValid) {
            return Result.error(MessageConstant.VERIFICATION_CODE + MessageConstant.INVALID);
        }

        return userService.resetUserPassword(userResetPasswordDTO);
    }

    /**
     * 登出
     *
     * @param token 认证token
     * @return 结果
     */
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        return userService.logout(token);
    }

    /**
     * 注销账号
     *
     * @return 结果
     */
    @DeleteMapping("/deleteAccount")
    public Result deleteAccount() {
        return userService.deleteAccount();
    }
    /**
     * 提交科普号认证申请
     *
     * @param popsciApplyDTO 科普号认证申请信息
     * @return 结果
     */
    @PostMapping("/applyPopsciAuth")
    public Result applyPopsciAuth(@RequestBody @Valid PopsciApplyDTO popsciApplyDTO) {
        log.info("提交科普号认证申请：{}", popsciApplyDTO);
        return popSciAuthorService.applyPopSciAuthor(popsciApplyDTO);
    }
    /**
     * 添加反馈
     *
     * @param content 反馈内容
     * @return 结果
     */
    @PostMapping("/addFeedback")
    public Result addFeedback(@RequestParam(value = "content") String content) {
        return feedbackService.addFeedback(content);
    }
    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 结果
     */
    @GetMapping("/getUserById")
    public Result<UserManagementVO> getUserById(@RequestParam Long userId) {
        return userService.getUserById(userId);
    }
}
