package cn.edu.seig.MhWeb.service.impl;

import cn.edu.seig.MhWeb.constant.JwtClaimsConstant;
import cn.edu.seig.MhWeb.constant.MessageConstant;
import cn.edu.seig.MhWeb.enumeration.UserStatusEnum;
import cn.edu.seig.MhWeb.enumeration.UserTypeEnum;
import cn.edu.seig.MhWeb.mapper.UserMapper;
import cn.edu.seig.MhWeb.model.dto.PopSciUser.*;
import cn.edu.seig.MhWeb.model.entity.User;
import cn.edu.seig.MhWeb.model.vo.PopSciAuthorDetailVO;
import cn.edu.seig.MhWeb.model.vo.PopSciAuthorNameVO;
import cn.edu.seig.MhWeb.model.vo.PopSciAuthorVO;
import cn.edu.seig.MhWeb.result.PageResult;
import cn.edu.seig.MhWeb.result.Result;
import cn.edu.seig.MhWeb.service.IPopSciAuthorService;
import cn.edu.seig.MhWeb.service.MinioService;
import cn.edu.seig.MhWeb.util.ThreadLocalUtil;
import cn.edu.seig.MhWeb.util.TypeConversionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 科普号服务实现类
 * @author su
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "popSciAuthorCache")
public class PopSciAuthorServiceImpl extends ServiceImpl<UserMapper, User> implements IPopSciAuthorService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MinioService minioService;


    /**
     * 分页查询科普号列表（仅筛选user_type=2/3的科普号）
     */
    @Cacheable(key = "#popSciAuthorDTO.pageNum + '-' + #popSciAuthorDTO.pageSize + '-' + #popSciAuthorDTO.authorName + '-' + #popSciAuthorDTO.userType + '-' + #popSciAuthorDTO.certificateNum ")
    public Result<PageResult<PopSciAuthorVO>> getAllPopSciAuthors(PopSciAuthorDTO popSciAuthorDTO) {
        // 1. 构建分页对象
        Page<User> page = new Page<>(popSciAuthorDTO.getPageNum(), popSciAuthorDTO.getPageSize());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 核心筛选：仅查科普号（user_type=2/3）
        queryWrapper.in("user_type", 2, 3);

        // 2. 构建查询条件
        if (popSciAuthorDTO.getAuthorName()!=null) {
            queryWrapper.like("username", popSciAuthorDTO.getAuthorName());
        }

        if (popSciAuthorDTO.getUserType() != null) {
            queryWrapper.eq("user_type", popSciAuthorDTO.getUserType());
        }
        if (popSciAuthorDTO.getCertificateNum() != null) {
            queryWrapper.like("certificate_num", popSciAuthorDTO.getCertificateNum());
        }

        // 倒序排序
        queryWrapper.orderByDesc("user_id");

        // 3. 执行分页查询
        IPage<User> userPage = userMapper.selectPage(page, queryWrapper);
        if (userPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        // 4. 转换为VO
        List<PopSciAuthorVO> voList = userPage.getRecords().stream()
                .map(user -> {
                    PopSciAuthorVO vo = new PopSciAuthorVO();
                    BeanUtils.copyProperties(user, vo);
                    vo.setUserStatus(user.getUserStatus()!= null ? user.getUserStatus() : null);
                    return vo;
                }).collect(Collectors.toList());

        return Result.success(new PageResult<>(userPage.getTotal(), voList));
    }

    /**
     * 获取所有科普号ID和名称（仅正常状态的科普号）
     */
    @Override
    @Cacheable(key = "'allPopSciAuthorNames'")
    public Result<List<PopSciAuthorNameVO>> getAllPopSciAuthorNames() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 仅查正常状态的科普号
        queryWrapper.in("user_type", 2, 3)
                   .eq("status", UserStatusEnum.ENABLE.getId())
                   .select("user_id", "username")
                   .orderByDesc("user_id");

        List<User> userList = userMapper.selectList(queryWrapper);
        if (userList.isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, null);
        }

        // 转换为ID+名称VO
        List<PopSciAuthorNameVO> voList = userList.stream()
                .map(user -> {
                    PopSciAuthorNameVO vo = new PopSciAuthorNameVO();
                    vo.setUserId(user.getUserId());
                    vo.setAuthorName(user.getUsername());
                    return vo;
                }).collect(Collectors.toList());

        return Result.success(MessageConstant.POPSCI_AUTHOR,voList);
    }

    /**
     * 获取随机6个科普号（原需求10个改为6个，从符合条件的全量列表中随机挑）
     */
    @Override
    public Result<List<PopSciAuthorVO>> getRandomPopSciAuthors() {
        // 步骤1：查询所有符合条件的科普号（去掉随机和分页，查全量）
        List<User> allUserList = userMapper.selectList(
                new QueryWrapper<User>()
                        .eq("user_type", 3) // 仅已认证科普号
                        .eq("status", UserStatusEnum.ENABLE.getId()) // 仅启用状态
        );

        // 关键日志：打印全量符合条件的数量
        log.info("符合条件的科普号全量数量：{}", allUserList.size());

        // 步骤2：判断全量列表是否为空
        if (allUserList.isEmpty()) {
            log.warn("未查询到符合条件的科普号");
            return Result.success(MessageConstant.DATA_NOT_FOUND, null);
        }

        // 步骤3：从全量列表中随机挑选6个（不足6个则取全部）
        List<User> randomUserList = new ArrayList<>();
        int pickCount = Math.min(6, allUserList.size()); // 要挑选的数量（最多6个）

        // 方式1：用Collections.shuffle打乱后截取（简单易读）
        Collections.shuffle(allUserList); // 随机打乱列表
        randomUserList = allUserList.subList(0, pickCount); // 截取前6个

        // 方式2：随机索引挑选（避免打乱原列表，可选）
        // Random random = new Random();
        // Set<Integer> pickedIndex = new HashSet<>();
        // while (pickedIndex.size() < pickCount) {
        //     pickedIndex.add(random.nextInt(allUserList.size()));
        // }
        // for (Integer index : pickedIndex) {
        //     randomUserList.add(allUserList.get(index));
        // }

        // 关键日志：打印随机挑选后的数量
        log.info("随机挑选的科普号数量：{}", randomUserList.size());

        // 步骤4：转换为VO（保留原有手动映射逻辑，避免枚举拷贝问题）
        List<PopSciAuthorVO> voList = randomUserList.stream()
                .map(user -> {
                    PopSciAuthorVO vo = new PopSciAuthorVO();
                    vo.setUserId(user.getUserId());
                    vo.setUsername(user.getUsername());
                    vo.setAvatar(user.getAvatar());
                    vo.setCertificateNum(user.getCertificateNum());
                    vo.setUserType(user.getUserType());
                    vo.setAffiliateUnit(user.getAffiliateUnit());
                    vo.setUserStatus(user.getUserStatus());
                    vo.setCreateTime(user.getCreateTime());
                    return vo;
                }).collect(Collectors.toList());

        // 关键日志：打印VO列表
        log.info("随机科普号VO列表：{}", voList);

        // 返回结果（确保data字段是挑选后的VO列表）
        return Result.success(voList);
    }

    /**
     * 获取科普号详情
     */
    @Override
    @Cacheable(key = "#userId")
    public Result<PopSciAuthorDetailVO> getPopSciAuthorDetail(Long userId) {
        // 1. 查询科普号信息
        User user = userMapper.selectById(userId);
        if (user == null || !List.of(2, 3).contains(user.getUserType())) {
            return Result.error(MessageConstant.POPSCI_AUTHOR + MessageConstant.NOT_FOUND);
        }

        // 2. 转换为详情VO
        PopSciAuthorDetailVO detailVO = new PopSciAuthorDetailVO();
        BeanUtils.copyProperties(user, detailVO);
        detailVO.setIntroduction(user.getIntroduction());
        detailVO.setUserStatus(user.getUserStatus().getId());

        return Result.success(detailVO);
    }
 /**
     * 新增/更新科普号（用户名存在则更新，不存在则创建）
     */
     @CacheEvict(cacheNames = "popSciAuthorCache", allEntries = true)
     public Result addPopSciAuthor(PopSciAuthorAddDTO addDTO) {
         // 1. 校验用户名是否存在
         QueryWrapper<User> queryWrapper = new QueryWrapper<>();
         queryWrapper.eq("username", addDTO.getUsername());
         User existUser = userMapper.selectOne(queryWrapper);
         Map<String, Object> map = ThreadLocalUtil.get();
         Long adminId = TypeConversionUtil.toLong(map.get(JwtClaimsConstant.ADMIN_ID));

         if (existUser != null) {
             // 2. 用户名已存在：更新科普号信息（BeanUtils简化字段赋值）
             BeanUtils.copyProperties(addDTO, existUser); // 复制DTO中所有匹配的字段（userType/certificateNum/affiliateUnit等）
             existUser.setCertApplyTime(LocalDateTime.now()); // 认证申请时间
             existUser.setUpdateTime(LocalDateTime.now());    // 更新时间

             // 若目标身份是3（已认证科普号），补充审核信息
             if (UserTypeEnum.AUTHED_POPSCI.getCode().equals(addDTO.getUserType())) {
                 existUser.setCertAuditTime(LocalDateTime.now());
                 existUser.setCertAuditorId(adminId);
             }

             userMapper.updateById(existUser);
             return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
         } else {
             // 3. 用户名不存在：创建新科普号用户（BeanUtils简化字段赋值）
             User user = new User();
             BeanUtils.copyProperties(addDTO, user); // 复制DTO中所有匹配的字段（username/userType等核心字段）

             // 固定配置：密码加密（abc123456 MD5加密）
//             user.setPassword(DigestUtils.md5DigestAsHex("abc123456".getBytes()));
////           默认邮箱
//             user.setEmail("92929292@163.com");
             // 固定配置：账号状态正常（对应数据库status=1）
             user.setUserStatus(UserStatusEnum.ENABLE);
             // 基础配置：创建时间、认证申请时间
             user.setCreateTime(LocalDateTime.now());
             user.setCertApplyTime(LocalDateTime.now());

             // 若创建的是已认证科普号，补充审核信息
             if (UserTypeEnum.AUTHED_POPSCI.getCode().equals(addDTO.getUserType())) {
                 user.setCertAuditTime(LocalDateTime.now());
                 user.setCertAuditorId(adminId);
             }

             userMapper.insert(user);
             return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
         }
     }
    /**
     * 更新科普号信息
     */
    @Override
    @CacheEvict(cacheNames = "popSciAuthorCache", allEntries = true)
    public Result updatePopSciAuthor(PopSciAuthorUpdateDTO updateDTO) {
        Long userId = updateDTO.getUserId();
        // 1. 校验科普号是否存在
        User existUser = userMapper.selectById(userId);
        if (existUser == null || !List.of(2, 3).contains(existUser.getUserType().getCode())) {
            return Result.error(MessageConstant.POPSCI_AUTHOR + MessageConstant.NOT_FOUND);
        }

        // 2. 校验用户名是否重复（排除自身）
        if (StringUtils.hasText(updateDTO.getUsername())) {
            User userByUsername = userMapper.selectOne(new QueryWrapper<User>().eq("username", updateDTO.getUsername()));
            if (userByUsername != null && !userByUsername.getUserId().equals(userId)) {
                return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
            }
        }

        // 3. 转换为实体
        User user = new User();
        BeanUtils.copyProperties(updateDTO, user);
        user.setUpdateTime(LocalDateTime.now());

        // 4. 执行更新
        if (userMapper.updateById(user) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新科普号头像（参考菌种封面更新逻辑重构）
     * @param userId 科普号ID
     * @param file 头像文件（MultipartFile）
     * @return 操作结果
     */
    @Override
    @CacheEvict(cacheNames = "popSciAuthorCache", allEntries = true)
    public Result updatePopSciAuthorAvatar(Long userId, MultipartFile file) {
        // 1. 基础校验：文件不能为空
        if (file.isEmpty()) {
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.NOT_NULL);
        }

        // 2. 校验科普号是否存在（仅待认证/已认证科普号可更新）
        User user = userMapper.selectById(userId);
        if (user == null || !List.of(2, 3).contains(user.getUserType())) {
            return Result.error(MessageConstant.POPSCI_AUTHOR + MessageConstant.NOT_FOUND);
        }

        try {
            // 3. 上传新头像到MinIO，指定存储目录（区分其他文件）
            String newAvatarUrl = minioService.uploadFile(file, "popsci_avatar");

            // 4. 处理旧头像：删除MinIO中的旧文件
            String oldAvatarUrl = user.getAvatar();
            if (StringUtils.hasText(oldAvatarUrl)) {
                minioService.deleteFile(oldAvatarUrl);
            }

            // 5. 更新用户表的头像URL + 修改时间
            user.setAvatar(newAvatarUrl);
            user.setUpdateTime(LocalDateTime.now());
            if (userMapper.updateById(user) == 0) {
                return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
            }

            return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
        } catch (Exception e) {
            // 6. 异常捕获+日志记录（便于排查上传/更新失败原因）
            log.error("更新科普号头像失败（userId={}）：", userId, e);
            return Result.error(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED);
        }
    }

    /**
     * 删除单个科普号
     */
    @Override
    @CacheEvict(cacheNames = "popSciAuthorCache", allEntries = true)
    public Result deletePopSciAuthor(Long userId) {
        // 1. 校验科普号是否存在
        User user = userMapper.selectById(userId);
        if (user == null || !List.of(2, 3).contains(user.getUserType())) {
            return Result.error(MessageConstant.POPSCI_AUTHOR + MessageConstant.NOT_FOUND);
        }

        // 2. 删除头像文件
        String avatar = user.getAvatar();
        if (StringUtils.hasText(avatar)) {
            minioService.deleteFile(avatar);
        }

        // 3. 删除数据库记录
        if (userMapper.deleteById(userId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 批量删除科普号
     */
    @Override
    @CacheEvict(cacheNames = {"popSciAuthorCache", "popsciCache"}, allEntries = true)
    public Result deletePopSciAuthors(List<Long> userIds) {
        // 1. 校验科普号是否存在
        List<User> userList = userMapper.selectBatchIds(userIds);
        if (userList.isEmpty()) {
            return Result.error(MessageConstant.POPSCI_AUTHOR + MessageConstant.NOT_FOUND);
        }

        // 2. 批量删除头像文件
        List<String> avatarList = userList.stream()
                .filter(user -> List.of(2, 3).contains(user.getUserType()))
                .map(User::getAvatar)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        for (String avatar : avatarList) {
            minioService.deleteFile(avatar);
        }

        // 3. 批量删除数据库记录
        if (userMapper.deleteBatchIds(userIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }
    /**
     * 用户：提交科普号认证————更新用户科普号信息
     */
    public Result applyPopSciAuthor(PopsciApplyDTO applyDTO) {
        try {
            Map<String, Object> map = ThreadLocalUtil.get();
            Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
            Long userId = TypeConversionUtil.toLong(userIdObj);
            // 2. 查询当前用户信息，校验是否为普通用户（user_type=1）
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error(MessageConstant.USER+MessageConstant.NOT_EXIST);
            }
            if (user.getUserType().getCode() != 1) { // 1=普通用户，2=待认证，3=已认证
                return Result.error(MessageConstant.USER_NOT_COMMON+MessageConstant.ALREADY_EXISTS);
            }

            // 3. 更新用户认证资料+申请时间+用户类型（改为待认证）
            user.setCertificateNum(applyDTO.getCertificateNum());
            user.setAffiliateUnit(applyDTO.getAffiliateUnit());
            user.setIntroduction(applyDTO.getIntroduction());
            user.setCertApplyTime(LocalDateTime.now());
            user.setUserType(UserTypeEnum.PENDING_POPSCI); // 改为待认证科普号
            user.setUpdateTime(LocalDateTime.now());

            userMapper.updateById(user);
            return Result.success(MessageConstant.APPLY+MessageConstant.SUCCESS);
        } catch (Exception e) {
            log.error("科普号认证申请失败：", e);
            return Result.error(MessageConstant.APPLY+MessageConstant.FAILED);
        }
    }
    @Override
    @CacheEvict(cacheNames = "popSciAuthorCache", allEntries = true)
    public Result auditPopSciAuthor(PopsciAuditDTO auditDTO) {
        try {
            // 1. 校验审核状态（仅支持1=通过，0=驳回）
            if (auditDTO.getUserType() == null || !(auditDTO.getUserType().getCode()== 2) ) {
                return Result.error(MessageConstant.USER_NOT_COMMON+MessageConstant.INVALID);
            }
            Map<String, Object> map = ThreadLocalUtil.get();
            Object adminIdObj = map.get(JwtClaimsConstant.ADMIN_ID);
            Long adminId = TypeConversionUtil.toLong(adminIdObj);
            // 3. 查询待审核用户，校验是否为待认证科普号（user_type=2）
            User user = userMapper.selectById(auditDTO.getUserId());
            if (user == null) {
                return Result.error(MessageConstant.USER+MessageConstant.NOT_EXIST);
            }
            if (user.getUserType().getCode() != 2) { // 仅待认证用户可审核
                return Result.error(MessageConstant.USER_NOT_COMMON+MessageConstant.INVALID);
            }

            // 4. 根据审核状态更新用户类型
            if (auditDTO.getAuditStatus() == 1) {
                // 审核通过：改为已认证科普号（user_type=3）
                user.setUserType(UserTypeEnum.AUTHED_POPSCI);
            } else {
                // 审核驳回：改回普通用户（user_type=1），清空认证资料
                user.setUserType(UserTypeEnum.ORDINARY_USER);
                user.setCertificateNum(null);
                user.setAffiliateUnit(null);
                // 简介可保留，无需清空
            }

            // 5. 更新审核信息
            user.setCertAuditTime(LocalDateTime.now());
            user.setCertAuditorId(adminId);
            user.setUpdateTime(LocalDateTime.now());
            // 保存审核备注（可选）
            if (StringUtils.hasText(auditDTO.getAuditRemark())) {
                // 若User实体无审核备注字段，需新增：user.setAuditRemark(auditDTO.getAuditRemark());
            }

            userMapper.updateById(user);
            return Result.success(MessageConstant.AUDIT+MessageConstant.SUCCESS);
        } catch (Exception e) {
            log.error("科普号认证审核失败：", e);
            return Result.error(MessageConstant.AUDIT+MessageConstant.FAILED);
        }
    }
    /**
     * 返回科普号列表
     */
    /**
     * 分页搜索科普号列表
     * 核心逻辑：仅查询 user_type=3（已认证科普号），支持 username/userId 模糊查询
     */
    @Override
    public Result<PageResult<PopSciAuthorNameVO>> searchPopSciAuthors(PopSciAuthorSearchDTO searchDTO) {
        // 1. 构建分页对象
        Page<User> page = new Page<>(searchDTO.getPageNum(), searchDTO.getPageSize());

        // 2. 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 核心：仅查询已认证科普号（user_type=3）
        queryWrapper.eq("user_type", 3);
        // 账号状态正常（status=1）
        queryWrapper.eq("status", 1);
        // 模糊查询：科普号名称
        if (searchDTO.getUsername() != null && !searchDTO.getUsername().trim().isEmpty()) {
            queryWrapper.like("username", searchDTO.getUsername());
        }
        // 模糊查询：科普号ID
        if (searchDTO.getUserId() != null) {
            queryWrapper.like("user_id", searchDTO.getUserId());
        }
        // 排序：按创建时间倒序
        queryWrapper.orderByDesc("create_time");

        // 3. 执行分页查询
        IPage<User> userPage = userMapper.selectPage(page, queryWrapper);
        if (userPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        // 4. 流式处理：将 User 转换为 PopSciAuthorNameVO（注意字段映射）
        List<PopSciAuthorNameVO> authorVOList = userPage.getRecords().stream()
                .map(user -> {
                    PopSciAuthorNameVO authorVO = new PopSciAuthorNameVO();
                    // 手动映射字段（因为字段名不一致）
                    authorVO.setUserId(user.getUserId().longValue()); // int -> Long
                    authorVO.setAuthorName(user.getUsername()); // username -> authorName
                    authorVO.setAvatar(user.getAvatar()); // 直接映射
                    return authorVO;
                }).toList();

        // 5. 返回分页结果
        return Result.success(new PageResult<>(userPage.getTotal(), authorVOList));
    }
}