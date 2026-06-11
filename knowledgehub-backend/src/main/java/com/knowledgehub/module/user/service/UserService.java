package com.knowledgehub.module.user.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledgehub.module.user.dto.LoginDTO;
import com.knowledgehub.module.user.dto.RegisterDTO;
import com.knowledgehub.module.user.entity.User;
import com.knowledgehub.module.user.vo.UserVO;

/**
 * 用户 Service 接口
 */
public interface UserService extends IService<User> {

    /**
     *
     * @param dto 注册信息
     * @return 注册成功的用户信息
     * 
     **/
    UserVO register(RegisterDTO dto);

    /**
     * 用户登录
     *
     * @param dto 登录信息
     * @return 登录成功的用户信息（Day 9 将追加 token）
     */
    UserVO login(LoginDTO dto);
}



















