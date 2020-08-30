package com.donghua.community.util;

import com.donghua.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，代替用户对象
 */
@Component
public class HostHolder {
    ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
