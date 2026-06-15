package com.knowledgehub.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.knowledgehub.module.user.dto.LoginDTO;
import com.knowledgehub.module.user.dto.RegisterDTO;
import com.knowledgehub.module.user.entity.User;
import com.knowledgehub.module.user.vo.LoginVO;
import com.knowledgehub.module.user.vo.UserVO;

public interface UserService extends IService<User> {

    UserVO register(RegisterDTO dto);

    /**
     * 用户登录，返回 token + 用户信息
     */
    LoginVO login(LoginDTO dto);
}
