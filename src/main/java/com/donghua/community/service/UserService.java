package com.donghua.community.service;

import com.donghua.community.dao.UserMapper;
import com.donghua.community.entity.User;
import com.donghua.community.util.CommunityUtil;
import com.donghua.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "用户名不能为空！");
            return map;
        }

        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        //  验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(user.getPassword() + user.getSalt());
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/actvation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;

    }
}
