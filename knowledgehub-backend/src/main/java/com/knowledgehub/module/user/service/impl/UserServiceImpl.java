package com.knowledgehub.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledgehub.common.ErrorCode;
import com.knowledgehub.exception.BusinessException;
import com.knowledgehub.module.user.dto.LoginDTO;
import com.knowledgehub.module.user.dto.RegisterDTO;
import com.knowledgehub.module.user.entity.User;
import com.knowledgehub.module.user.mapper.UserMapper;
import com.knowledgehub.module.user.service.UserService;
import com.knowledgehub.module.user.vo.UserVO;
import com.knowledgehub.utils.PasswordUtils;
import org.springframework.stereotype.Service;

/**
 * 用户 Service 实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * TODO: 请你手敲完成用户注册逻辑。
     *
     * 学习目标：
     * 1. 理解注册流程：校验 → 查重 → 加密 → 入库
     * 2. 理解 LambdaQueryWrapper 的用法
     * 3. 理解 BCrypt 密码加密的必要性
     *i
     * 参考实现（请不要直接复制，建议手敲）：
     */


     /* // 2. 密码加密
     * String encodedPassword = PasswordUtils.encode(dto.getPassword());
     *
     * // 3. 构造 User 对象并入库
     * User user = new User();
     * user.setUsername(dto.getUsername());
     * user.setPassword(encodedPassword);
     * user.setEmail(dto.getEmail());
     * user.setStatus(1);  // 默认正常状态
     * baseMapper.insert(user);
     *
     * // 4. 返回 UserVO
     * return UserVO.builder()
     *         .id(user.getId())
     *         .username(user.getUsername())
     *         .email(user.getEmail())
     *         .status(user.getStatus())
     *         .createTime(user.getCreateTime())
     *         .build();
     */


    @Override
    public UserVO register(RegisterDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,dto.getUsername());
        User exitUser = baseMapper.selectOne(wrapper);
        if(exitUser != null){
            throw new BusinessException(ErrorCode.USER_EXISTS);
        }

        String encodedPassword = PasswordUtils.encode(dto.getPassword());
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(dto.getEmail());
        user.setStatus(1);
        baseMapper.insert(user);

        return UserVO.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .status(user.getStatus())
        .createTime(user.getCreateTime())
        .build();

    }

    /**
     * 学习目标：
     * 1. 理解登录流程：查用户 → 验状态 → 验密码 → 返回
     * 2. 理解为什么先查用户再验密码（安全：不让攻击者知道用户名是否存在）
     *    提示：先查用户 → 不存在抛 PASSWORD_ERROR（而不是 USER_NOT_FOUND）
     * 3. 理解 BCrypt 密码校验的原理
     */
    @Override
    public UserVO login(LoginDTO dto) {
        //实现登录逻辑
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,dto.getUsername());
        User user = baseMapper.selectOne(wrapper);
        if(user == null){
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        if(user.getStatus() == 0){
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        if(!PasswordUtils.matches(dto.getPassword(),user.getPassword())){
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        return UserVO.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .status(user.getStatus())
        .createTime(user.getCreateTime())
        .build();


    }
}
