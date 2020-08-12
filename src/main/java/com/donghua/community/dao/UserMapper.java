package com.donghua.community.dao;

import com.donghua.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component(value = "userMapper")
public interface UserMapper {
    User selectById(int id);

    User selectByName(String name);

    User selectByEmail(String mail);

    int insertUser(User user);

    int updateStatus(int id, int status);

    int updateHeaderUrl(int id, String header);

    int updatePassword(int id, String password);
}
