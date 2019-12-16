package com.web.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.web.pojo.User;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author GQ.Yin
 * @version 1.0
 * @title
 * @date 2019/12/16 13:11
 */
@Repository
public interface UserMapper extends BaseMapper<User> {
    /**
     * 查询所有用户信息
     * @return list
     */
    List<User> selectAll();
    User selectById(String id);
    int updateUser(User user);
    int updateUserNoSafe(User user);

}
