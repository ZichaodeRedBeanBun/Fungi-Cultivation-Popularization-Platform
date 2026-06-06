package cn.edu.seig.MhWeb.service.impl;


import cn.edu.seig.MhWeb.constant.JwtClaimsConstant;
import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.context.BaseContext;
import cn.edu.seig.MhWeb.enumeration.RoleEnum;
import cn.edu.seig.MhWeb.enumeration.UserStatusEnum;
import cn.edu.seig.MhWeb.enumeration.UserTypeEnum;
import cn.edu.seig.MhWeb.mapper.UserMapper;
import cn.edu.seig.MhWeb.model.dto.PopSciUser.PopsciApplyDTO;
import cn.edu.seig.MhWeb.model.dto.user.*;
import cn.edu.seig.MhWeb.model.entity.User;
import cn.edu.seig.MhWeb.model.vo.PopSciAuthorNameVO;
import cn.edu.seig.MhWeb.model.vo.UserManagementVO;
import cn.edu.seig.MhWeb.model.vo.UserVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.EmailService;
import cn.edu.seig.MhWeb.service.IUserService;
import cn.edu.seig.MhWeb.service.MinioService;
import cn.edu.seig.MhWeb.util.JwtUtil;
import cn.edu.seig.MhWeb.util.ThreadLocalUtil;
import cn.edu.seig.MhWeb.util.TypeConversionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 
 *
 * @author su
 */
@Service
@CacheConfig(cacheNames = "userCache")
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private EmailService emailService;
    @Autowired
    private MinioService minioService;

    /**
     * 发送验证码
     *
     * @param email 用户邮箱
     * @return 结果
     */
    @Override
    public Result sendVerificationCode(String email) {
        String verificationCode = emailService.sendVerificationCodeEmail(email);
        if (verificationCode == null) {
            return Result.error(MessageConstant.EMAIL_SEND_FAILED);
        }

        // 将验证码存储到Redis中，设置过期时间为5分钟
        stringRedisTemplate.opsForValue().set("verificationCode:" + email, verificationCode, 5, TimeUnit.MINUTES);
        return Result.success(MessageConstant.EMAIL_SEND_SUCCESS);
    }

    /**
     * 验证验证码
     *
     * @param email            用户邮箱
     * @param verificationCode 验证码
     * @return 验证结果
     */
    @Override
    public boolean verifyVerificationCode(String email, String verificationCode) {
        String storedCode = stringRedisTemplate.opsForValue().get("verificationCode:" + email);
        return storedCode != null && storedCode.equals(verificationCode);
    }

    /**
     * 用户注册
     *
     * @param userRegisterDTO 用户注册信息
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result register(@Valid UserRegisterDTO userRegisterDTO) {
        // 删除Redis中的验证码
        stringRedisTemplate.delete("verificationCode:" + userRegisterDTO.getEmail());
        //判断邮箱与用户账号是否唯一
        User userByUsername = userMapper.selectOne(new QueryWrapper<User>().eq("username", userRegisterDTO.getUsername()));
        if (userByUsername != null) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }

        User userByEmail = userMapper.selectOne(new QueryWrapper<User>().eq("email", userRegisterDTO.getEmail()));
        if (userByEmail != null) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
        }

        String passwordMD5 = DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes());
        //链式调用
        User user = new User();
        user.setUsername(userRegisterDTO.getUsername()).setPassword(passwordMD5).setEmail(userRegisterDTO.getEmail())
                .setCreateTime(LocalDateTime.now()).setUpdateTime(LocalDateTime.now())
                .setUserStatus(UserStatusEnum.ENABLE).setUserType(UserTypeEnum.ORDINARY_USER);

        if (userMapper.insert(user) == 0) {
            return Result.error(MessageConstant.REGISTER + MessageConstant.FAILED);
        }
        log.info("注册成功");
        return Result.success(MessageConstant.REGISTER + MessageConstant.SUCCESS);
    }

    /**
     * 用户登录
     *
     * @param userLoginDTO 用户登录信息
     * @return 结果
     */
    @Override
    public Result login(@Valid UserLoginDTO userLoginDTO) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", userLoginDTO.getEmail()));
        if (user == null) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ERROR);
        }
        if (user.getUserStatus() != UserStatusEnum.ENABLE) {
            return Result.error(MessageConstant.ACCOUNT_LOCKED);
        }

        if (DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes()).equals(user.getPassword())) {
            // 登录成功
            Map<String, Object> claims = new HashMap<>();
            // 核心：判断用户类型（1=普通用户，2=待认证科普号，3=已认证科普号）
            UserTypeEnum userType = user.getUserType(); // 确保User实体有user_type字段（Byte类型）
            if (userType.getCode() == 3) {
                // 已认证科普号 → 分配POPSCI_USER角色
                claims.put(JwtClaimsConstant.ROLE, RoleEnum.POPSCI_USER.getRole());
            } else {
                // 普通用户/待认证科普号 → 分配USER角色
                claims.put(JwtClaimsConstant.ROLE, RoleEnum.USER.getRole());
            }
            claims.put(JwtClaimsConstant.USER_ID, user.getUserId());
            claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
//            TODO 将邮箱放在jwt里没必要
            claims.put(JwtClaimsConstant.EMAIL, user.getEmail());
            String token = JwtUtil.generateToken(claims);
            log.info("生成的token：{}",token);
            // 将token存入redis
            stringRedisTemplate.opsForValue().set(token, token, 6, TimeUnit.HOURS);

            return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, token);
        }

        return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
    }

    /**
     * 用户信息
     *
     * @return 结果
     */
    @Override
    public Result<UserVO> userInfo() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);
        User user = userMapper.selectById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setUserType(user.getUserType().getCode());
        return Result.success(userVO);
    }

    /**
     * 用户->更新用户信息
     *
     * @param userSelfUpdateDTO 用户信息:姓名 手机 邮箱
     * @return 结果
     */
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUserInfo(@Valid UserSelfUpdateDTO userSelfUpdateDTO) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = BaseContext.getCurrentId();
        User userByUsername = userMapper.selectOne(new QueryWrapper<User>().eq("username", userSelfUpdateDTO.getUsername()));
        if (userByUsername != null && !userByUsername.getUserId().equals(userId)) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }
////        不用加
//        if (userSelfUpdateDTO.getUserType() == null ) {
//            return Result.error(MessageConstant.USER_TYPE + MessageConstant.NOT_NULL);
//        }
        User userByEmail = userMapper.selectOne(new QueryWrapper<User>().eq("email", userSelfUpdateDTO.getEmail()));
        if (userByEmail!= null && !userByEmail.getUserId().equals(userId)) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
        }
        User user = new User();
        user.setUserId(userId);
        BeanUtils.copyProperties(userSelfUpdateDTO, user);
        user.setUpdateTime(LocalDateTime.now());

        if (userMapper.updateById(user) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新用户头像
     *
     * @param avatarUrl 用户头像
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUserAvatar(String avatarUrl) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);

        User user = userMapper.selectById(userId);
        String userAvatar = user.getAvatar();
        if (userAvatar != null && !userAvatar.isEmpty()) {
            minioService.deleteFile(userAvatar);
        }
        //仅设置需要更新的字段
        if (userMapper.update(new User().setAvatar(avatarUrl).setUpdateTime(LocalDateTime.now()),
                new QueryWrapper<User>().eq("user_id", userId)) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新用户密码
     *
     * @param userPasswordDTO 用户密码信息
     * @param token           认证token
     * @return 结果
     */
    @Override
    public Result updateUserPassword(@Valid UserPasswordDTO userPasswordDTO, String token) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);
        User user = userMapper.selectById(userId);
        if (!user.getPassword().equals(DigestUtils.md5DigestAsHex(userPasswordDTO.getOldPassword().getBytes()))) {
            return Result.error(MessageConstant.OLD_PASSWORD_ERROR);
        }

        if (user.getPassword().equals(DigestUtils.md5DigestAsHex(userPasswordDTO.getNewPassword().getBytes()))) {
            return Result.error(MessageConstant.NEW_PASSWORD_ERROR);
        }

        if (!userPasswordDTO.getRepeatPassword().equals(userPasswordDTO.getNewPassword())) {
            return Result.error(MessageConstant.PASSWORD_NOT_MATCH);
        }

        if (userMapper.update(new User().setPassword(DigestUtils.md5DigestAsHex(userPasswordDTO.getNewPassword().getBytes())).setUpdateTime(LocalDateTime.now()),
                new QueryWrapper<User>().eq("id", userId)) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        // 注销token
        stringRedisTemplate.delete(token);

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 重置用户密码【忘记密码场景】
     *
     * @param userResetPasswordDTO 用户密码信息
     * @return 结果
     */
    @Override
    public Result resetUserPassword(@Valid UserResetPasswordDTO userResetPasswordDTO) {
        // 删除Redis中的验证码 不用额外判断
        stringRedisTemplate.delete("verificationCode:" + userResetPasswordDTO.getEmail());

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", userResetPasswordDTO.getEmail()));
        if (user == null) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.NOT_EXIST);
        }
        //表单的二次验证
        if (!userResetPasswordDTO.getRepeatPassword().equals(userResetPasswordDTO.getNewPassword())) {
            return Result.error(MessageConstant.PASSWORD_NOT_MATCH);
        }

        if (userMapper.update(new User().setPassword(DigestUtils.md5DigestAsHex(userResetPasswordDTO.getNewPassword().getBytes())).setUpdateTime(LocalDateTime.now()),
                new QueryWrapper<User>().eq("id", user.getUserId())) == 0) {
            return Result.error(MessageConstant.PASSWORD + MessageConstant.RESET + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.PASSWORD + MessageConstant.RESET + MessageConstant.SUCCESS);
    }

    /**
     * 登出
     *
     * @param token 认证token
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = {"userCache", "userFavoriteCache", "songCache", "artistCache", "playlistCache"}, allEntries = true)
    public Result logout(String token) {
        // 注销token
        Boolean result = stringRedisTemplate.delete(token);
        if (result != null && result) {
            return Result.success(MessageConstant.LOGOUT + MessageConstant.SUCCESS);
        } else {
            return Result.error(MessageConstant.LOGOUT + MessageConstant.FAILED);
        }
    }

    /**
     * 注销账户
     *
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result deleteAccount() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);

        // 查询用户信息，获取头像 URL
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }

        // 删除头像
        String userAvatar = user.getAvatar();
        if (userAvatar != null && !userAvatar.isEmpty()) {
            minioService.deleteFile(userAvatar);
        }

        // 删除用户
        if (userMapper.deleteById(userId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 获取所有用户数量
     *
     * @return 用户数量
     */
    @Override
    public Result<Long> getAllUsersCount(Integer user_type) {
        // 1. 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 2. 如果传入了 user_type 参数，添加精确筛选条件
        if (user_type != null) {
            // 注意：确保数据库中用户类型字段名是 user_type（和参数名一致），如果字段名不同需调整
            queryWrapper.eq("user_type", user_type);
        }

        // 3. 统计数量（有筛选则统计对应类型，无筛选则统计总数）
        Long count = userMapper.selectCount(queryWrapper);

        // 4. 返回结果
        return Result.success(count);
    }

    /**
     * 分页查询所有用户
     *
     * @param userSearchDTO 用户查询条件
     * @return 用户分页信息
     */
    @Override
    @Cacheable(key = "#userSearchDTO.pageNum + '-' + #userSearchDTO.pageSize + '-' + #userSearchDTO.username + '-' + #userSearchDTO.userType + '-' + #userSearchDTO.userStatus")
    public Result<PageResult<UserManagementVO>> getAllUsers(UserSearchDTO userSearchDTO) {
        // 分页查询
        Page<User> page = new Page<>(userSearchDTO.getPageNum(), userSearchDTO.getPageSize());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (userSearchDTO.getUserId() != null) {
            queryWrapper.like("user_id", userSearchDTO.getUserId());
        }
        // 根据 userSearchDTO 的条件构建查询条件
        if (userSearchDTO.getUsername() != null) {
            queryWrapper.like("username", userSearchDTO.getUsername());
        }
        if (userSearchDTO.getUserStatus() != null) {
            queryWrapper.eq("status", userSearchDTO.getUserStatus().getId());//获取id 字段，作为查询参数值 where
        }
//        if (userSearchDTO.getUserType() != null) {
//            queryWrapper.eq("user_type", UserTypeEnum.userSearchDTO.getUserType());//获取id 字段，作为查询参数值 where
//        }
        // 倒序排序
        queryWrapper.orderByDesc("create_time");

        IPage<User> userPage = userMapper.selectPage(page, queryWrapper);
        if (userPage.getRecords().size() == 0) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }
        //  流式处理：将每个 User 转换为 UserManagementVO
        List<UserManagementVO> userVOList = userPage.getRecords().stream()
                .map(user -> {
                    UserManagementVO userVO = new UserManagementVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                }).toList();

        return Result.success(new PageResult<>(userPage.getTotal(), userVOList));
    }

    /**
     * 添加用户
     *
     * @param userAddDTO 用户信息
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result addUser(UserAddDTO userAddDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userAddDTO.getUsername())
                .or()
                .eq("email", userAddDTO.getEmail());

        List<User> existingUsers = userMapper.selectList(queryWrapper);
        //如果都具唯一性的信息都存在 报错
        if (existingUsers != null && !existingUsers.isEmpty()) {
            for (User user : existingUsers) {
                if (user.getUsername().equals(userAddDTO.getUsername())) {
                    return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
                }
                if (user.getEmail().equals(userAddDTO.getEmail())) {
                    return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
                }
            }
        }

        String passwordMD5 = DigestUtils.md5DigestAsHex(userAddDTO.getPassword().getBytes());
        User user = new User();
        user.setUsername(userAddDTO.getUsername()).setPassword(passwordMD5).setUserType(userAddDTO.getUserType())
                .setEmail(userAddDTO.getEmail()).setIntroduction(userAddDTO.getIntroduction())
                .setCreateTime(LocalDateTime.now()).setUpdateTime(LocalDateTime.now());

        // 前端传递的用户状态（1：启用，0：禁用）需反转
        if (userAddDTO.getUserStatus().getId() == 1) {
            user.setUserStatus(UserStatusEnum.ENABLE);  // 数据库（0：启用）
        } else if (userAddDTO.getUserStatus().getId() == 0) {
            user.setUserStatus(UserStatusEnum.DISABLE);    // 数据库（1：禁用）
        }
        UserTypeEnum userType = userAddDTO.getUserType();
        // 定义科普号类型（2=待认证科普号，3=已认证科普号，根据你的业务调整）
        boolean isPopSciUser = userType.getCode() == 2 || userType.getCode() == 3;
        if (isPopSciUser) {
            user.setCertificateNum(userAddDTO.getCertificateNum() == null ? "" : userAddDTO.getCertificateNum().trim());
            user.setAffiliateUnit(userAddDTO.getAffiliateUnit() == null ? "" : userAddDTO.getAffiliateUnit().trim());
            user.setCertAuditTime(LocalDateTime.now());
            // 仅已认证科普号（3）填充审核信息
            if (userType.getCode() == 3) {
                user.setCertAuditTime(LocalDateTime.now());
                // 审核管理员ID：从ThreadLocal获取当前登录管理员ID
                Map<String, Object> adminMap = ThreadLocalUtil.get();
                Long adminId = TypeConversionUtil.toLong(adminMap.get(JwtClaimsConstant.ADMIN_ID));
                user.setCertAuditorId(adminId);
            }
        }
        //插入数据
        if (userMapper.insert(user) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 管理端-》更新用户
     *
     * @param userAdminUpdateDTO 用户信息
     * @return 结果
     */
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUser(UserAdminUpdateDTO userAdminUpdateDTO) {
        Long userId=userAdminUpdateDTO.getUserId();
        // 1. 校验 userId 非空（兜底）
        if (userId== null) {
            return Result.error(MessageConstant.USER_ID+MessageConstant.NOT_NULL);
        }

        // 2. 先查询用户是否存在（避免更新不存在的用户）
        User existUser = userMapper.selectById(userAdminUpdateDTO.getUserId());
        if (existUser == null) {
            return Result.error(MessageConstant.USER+MessageConstant.NOT_EXIST);
        }
        User userByUsername = userMapper.selectOne(new QueryWrapper<User>().eq("username", userAdminUpdateDTO.getUsername()));
        if (userByUsername != null && !userByUsername.getUserId().equals(userId)) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }

        User userByEmail = userMapper.selectOne(new QueryWrapper<User>().eq("email", userAdminUpdateDTO.getEmail()));
        if (userByEmail != null && !userByEmail.getUserId().equals(userId)) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
        }

        User user = new User();
        BeanUtils.copyProperties(userAdminUpdateDTO, user);//已包含科普号字段
        user.setUpdateTime(LocalDateTime.now());
        // 补充：若修改为已认证科普号，自动填充审核时间/管理员ID
        if (userAdminUpdateDTO.getUserType() != null
                && userAdminUpdateDTO.getUserType() == UserTypeEnum.AUTHED_POPSCI) {
            user.setCertAuditTime(LocalDateTime.now());
            // 取当前登录管理员ID
            Map<String, Object> adminMap = ThreadLocalUtil.get();
            Long adminId = TypeConversionUtil.toLong(adminMap.get(JwtClaimsConstant.ADMIN_ID));
            user.setCertAuditorId(adminId);
        }
        if (userMapper.updateById(user) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新用户状态
     *
     * @param userId     用户id
     * @param userStatus 状态
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUserStatus(Long userId, Integer userStatus) {
        // 确保用户状态有效
        UserStatusEnum statusEnum;
        if (userStatus == 0) {
            statusEnum = UserStatusEnum.ENABLE;
        } else if (userStatus == 1) {
            statusEnum = UserStatusEnum.DISABLE;
        } else {
            return Result.error(MessageConstant.USER_STATUS_INVALID);
        }

        // 更新用户状态
        User user = new User();
        user.setUserStatus(statusEnum).setUpdateTime(LocalDateTime.now());

        int rows = userMapper.update(user, new QueryWrapper<User>().eq("user_id", userId));
        if (rows == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 删除用户
     *
     * @param userId 用户id
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result deleteUser(Long userId) {
        if (userMapper.deleteById(userId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 批量删除用户
     *
     * @param userIds 用户id数组
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result deleteUsers(List<Long> userIds) {
        if (userMapper.deleteByIds(userIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    @Override
    @Cacheable(key = "#userId") // 新增：根据用户ID缓存
    public Result<UserManagementVO> getUserById(Long userId) {
        // 1. 基础校验
        if (userId == null) {
            return Result.error(MessageConstant.USER_ID + MessageConstant.NOT_NULL);
        }

        // 2. 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }

        // 3. 转换为 UserManagementVO
        UserManagementVO userVO = new UserManagementVO();
        BeanUtils.copyProperties(user, userVO);

        return Result.success(userVO);
    }

    /**
     * 新增：用户提交科普号认证申请（普通用户→待认证科普号）
     */
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result applyPopsciAuth(@Valid PopsciApplyDTO popsciApplyDTO) {
        // 1. 获取当前登录用户ID
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = TypeConversionUtil.toInteger(map.get(JwtClaimsConstant.USER_ID));

        // 2. 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }

        // 3. 校验：仅普通用户（user_type=1）可提交认证
        if (user.getUserType().getCode() != 1) {
            String typeDesc = user.getUserType().getCode() == 2 ? UserTypeEnum.PENDING_POPSCI.getDesc() : UserTypeEnum.AUTHED_POPSCI.getDesc();
            return Result.error("仅普通用户可提交科普号认证，当前身份：" + typeDesc);
        }

        // 4. 更新用户信息：改为待认证科普号，填充认证字段
        user.setUserType(UserTypeEnum.PENDING_POPSCI)
                .setCertificateNum(popsciApplyDTO.getCertificateNum())
                .setAffiliateUnit(popsciApplyDTO.getAffiliateUnit())
                .setIntroduction(popsciApplyDTO.getIntroduction())
                .setCertApplyTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());

        if (userMapper.updateById(user) == 0) {
            return Result.error(MessageConstant.APPLY + MessageConstant.FAILED);
        }
        return Result.success("科普号认证申请提交成功，请等待管理员审核");
    }

}
