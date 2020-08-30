package com.donghua.community.service;

import com.donghua.community.dao.UserMapper;
import com.donghua.community.entity.LoginTicket;
import com.donghua.community.entity.User;
import com.donghua.community.util.CommunityConstant;
import com.donghua.community.util.CommunityUtil;
import com.donghua.community.util.MailClient;
import com.donghua.community.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
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
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;

    }

    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(user.getId(), 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String name, String password, long expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if(StringUtils.isBlank(name)){
            map.put("usernameMsg", "账号名不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        // 账号合理性
        // 账号是否存在
        User user = userMapper.selectByName(name);
        if (user == null){
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }

        // 账号是否激活
        if (user.getStatus() == 0){
            map.put("usernameMsg", "该账号还未激活！");
            return map;
        }

        // 验证密码
        String code = CommunityUtil.md5(password + user.getSalt());
        if (!code.equals(user.getPassword())){
            map.put("passwordMsg", "输入密码错误！");
            return map;
        }

        //  到了这一步，就说明没问题了，可以就开始生成登录凭证了
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setExpired(new Date(System.currentTimeMillis() + (expiredSeconds * 1000)));
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);


        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket){
//        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);

    }

    public void updateHeader(int userId, String headerUrl){
        userMapper.updateHeaderUrl(userId, headerUrl);
        clearCache(userId);
    }

    public User findUserByName(String name){
        return userMapper.selectByName(name);
    }

    // １．　优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2. 取不到时初始化数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3. 数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
