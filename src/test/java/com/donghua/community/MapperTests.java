package com.donghua.community;

import com.donghua.community.dao.DiscussPostMapper;
import com.donghua.community.dao.LoginTicketMapper;
import com.donghua.community.dao.UserMapper;
import com.donghua.community.entity.DiscussPost;
import com.donghua.community.entity.LoginTicket;
import com.donghua.community.entity.User;
import com.donghua.community.util.CommunityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        User liubei = userMapper.selectByName("liubei");
        System.out.println(liubei);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public  void testInsertUser(){
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeaderUrl(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }

    @Test
    public void testSelectDisscussPost() {
        List<DiscussPost> list = discussPostMapper.selectDisscussPosts(0, 0, 10);
        for (DiscussPost post :
                list) {
            System.out.println(post);
        }

        int i = discussPostMapper.selectDisscussPossRows(0);
        System.out.println(i);
    }

    @Test
    public void testInserLoginTicket(){
        LoginTicket ticket = new LoginTicket();
        ticket.setStatus(1);
        ticket.setTicket(CommunityUtil.generateUUID());
        ticket.setUserId(100);
        ticket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(ticket);
    }

    @Test
    public void testSelectLoginTicket(){
        LoginTicket ticket = loginTicketMapper.selectByTicket("9ef3d19c1612432ababfc6de2df238d5");
        System.out.println(ticket);
    }

    @Test
    public void testUpdateTicketStatus(){
        int i = loginTicketMapper.updateStatus("9ef3d19c1612432ababfc6de2df238d5", 0);
        System.out.println(i);
    }
}
